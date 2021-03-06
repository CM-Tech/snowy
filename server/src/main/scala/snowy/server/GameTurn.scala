package snowy.server

import com.typesafe.scalalogging.StrictLogging
import snowy.Achievements.IceStreak
import snowy.Awards._
import snowy.Achievements.Achievement
import snowy.GameConstants._
import snowy.collision._
import snowy.playfield.PlayId.{BallId, PowerUpId}
import snowy.playfield.{Sled, _}
import snowy.server.GameTurn._
import snowy.measures.Span.timeSpan
import snowy.measures.Span
import socketserve.ClientId
import scala.concurrent.duration._
import snowy.measures.{Gauged, Span}

class GameTurn(state: GameState, tickDelta: FiniteDuration) extends StrictLogging {
  var gameTime           = System.currentTimeMillis()
  var lastGameTime       = gameTime - tickDelta.toMillis
  val gameHealth         = new GameHealth(state)
  val gameStateImplicits = new GameStateImplicits(state)
  import gameStateImplicits._

  /** advance to the next game time
    * @return seconds since the last turn
    */
  def nextTurn()(implicit parentSpan: Span): Double = {
    val deltaSeconds = nextTimeSlice()
    recordTurnJitter(deltaSeconds)
    deltaSeconds
  }

  /** Called to update game state on a regular timer */
  def turn(deltaSeconds: Double)(implicit parentSpan: Span): TurnResults =
    timeSpan("GameTurn.turn") { turnSpan =>
      gameHealth.recoverHealth(deltaSeconds)
      val expiredBalls = gameHealth.expireSnowballs()

      turnSpan.time("moveSnowballs") {
        state.motion.moveSnowballs(state.snowballs.items, deltaSeconds)
      }

      val moveAwards = turnSpan.time("moveSleds") {
        state.motion.moveSleds(state.sleds.items, deltaSeconds)
      }

      val usedPowerUps = turnSpan.time("applyPowerUps") {
        applyPowerUps(state.sleds, state.powerUps)
      }

      val collided = turnSpan.time("checkCollisions") {
        checkCollisions()
      }

      val achievements = turnSpan.time("sledAchievements") {
        sledAchievements(collided.killedSleds)
      }

      val died = gameHealth.collectDead()

      val newPowerUps = state.powerUps.refresh(gameTime)

      TurnResults(
        died,
        expiredBalls ++ collided.killedSnowballs,
        usedPowerUps,
        newPowerUps,
        collided.killedSleds,
        achievements
      )
    }

  case class CollisionResult(killedSleds: Traversable[SledKill],
                             killedSnowballs: Traversable[BallId])

  private def applyPowerUps(sleds: Sleds, powerUps: PowerUps): Iterable[PowerUpId] = {
    val winners =
      for {
        powerUp <- powerUps.items
        sled    <- sleds.grid.inside(powerUp.boundingBox)
        if Collisions.circularCollide(powerUp, sled)
      } yield {
        (powerUp, sled)
      }

    for { (powerUp, sled) <- winners } yield {
      powerUp.powerUpSled(sled)
      powerUps.removePowerUp(powerUp, gameTime)
      powerUp.id
    }
  }

  /** check for collisions between the sled and trees or snowballs */
  private def checkCollisions()(implicit snowballTracker: PlayfieldTracker[Snowball],
                                sledTracker: PlayfieldTracker[Sled]): CollisionResult = {
    import snowy.collision.GameCollide.snowballTrees

    // collide snowballs with sleds
    val sledSnowballDeaths: DeathList[Sled, Snowball] =
      CollideThings.collideWithGrid(
        state.sleds.items,
        state.snowballs.grid
      )

    val deadBalls: Traversable[Snowball] =
      for { Death(snowball: Snowball, _) <- sledSnowballDeaths.b } yield { snowball }
    val uniqueDeadBalls = deadBalls.toSet
    uniqueDeadBalls.foreach(_.remove())

    // collide snowballs with trees
    val snowballTreeDeaths =
      for {
        snowball <- state.snowballs.items
        nearTrees = state.trees.grid.inside(snowball.boundingBox)
        if snowballTrees(snowball, nearTrees)
      } yield snowball

    for (ball <- snowballTreeDeaths) {
      ball.remove()
    }

    // collide snowballs with each other
    val snowballDeaths =
      CollideThings
        .collideCollection(state.snowballs.items, state.snowballs.grid)
        .map(_.killed)
        .toSet

    for (snowball <- snowballDeaths) { snowball.remove() }

    // collide sleds with trees
    for {
      sled <- state.sleds.items
      nearTrees = state.trees.grid.inside(sled.boundingBox)
    } state.sledTree.collide(sled, nearTrees)

    // collide sleds with each other
    val sledDeaths = CollideThings.collideCollection(state.sleds.items, state.sleds.grid)

    // accumulate awards
    val snowballAwards =
      for (Death(killed: Sled, killer: Snowball) <- sledSnowballDeaths.a)
        yield SledKill(killer.ownerId, killed.id)

    val sledAwards = sledDeaths.map {
      case Death(killed: Sled, killer: Sled) => SledKill(killer.id, killed.id)
    }

    val deadSnowballs = {
      val bySled =
        for (Death(killed: Snowball, killer: Sled) <- sledSnowballDeaths.b)
          yield killed.id

      snowballDeaths.map(_.id) ++ bySled ++ snowballTreeDeaths.map(_.id)
    }

    CollisionResult(snowballAwards ++ sledAwards, deadSnowballs)
  }

  private def sledAchievements(
        sledKills: Traversable[SledKill]
  ): Traversable[Achievement] = {
    val streaks = for {
      SledKill(killerSledId, _) <- sledKills
      killerSled                <- killerSledId.sled
    } yield {
      killerSled.achievements.kills += 1
      if (gameTime - killerSled.achievements.lastKill < 100000) {
        killerSled.achievements.killStreak += 1
        killerSled.achievements.lastKill = gameTime

        logger.warn(
          s"sled $killerSled kill streak ${killerSled.achievements.killStreak}"
        )

        Some(IceStreak(killerSledId, killerSled.achievements.killStreak))
      } else {
        killerSled.achievements.killStreak = 1
        killerSled.achievements.lastKill = gameTime

        None
      }
    }

    streaks.flatten
  }

  /** update the score based on sled travel distance, sleds killed, etc. */
  private def updateScore(awards: Seq[Award]): Unit = {
    awards.foreach {
      case SledKill(winnerId, loserId) =>
        for {
          winner <- winnerId.user
          loser  <- loserId.user
        } {
          val points = loser.score * Points.sledKill
          winner.score += points
        }
      case Travel(sledId, distance) =>
      case SnowballHit(winnerId)    =>
      case SledDied(loserId) =>
        for { user <- loserId.user } {
          user.score = math.max(user.score * Points.sledLoss, Points.minPoints)
        }
    }
  }

  /** Advance to the next game simulation state
    *
    * @return the time since the last time slice, in seconds
    */
  private def nextTimeSlice(): Double = {
    val currentTime  = System.currentTimeMillis()
    val deltaSeconds = (currentTime - gameTime) / 1000.0
    lastGameTime = gameTime
    gameTime = currentTime
    deltaSeconds
  }

  private def recordTurnJitter(deltaSeconds: Double)(implicit parentSpan: Span): Unit = {
    val secondsToMicros = 1000000
    val deltaMicros     = (deltaSeconds * secondsToMicros).toLong
    Gauged("deltaMicroseconds", deltaMicros)
  }

}

object GameTurn {
  case class TurnResults(deadSleds: Traversable[SledDied],
                         deadSnowBalls: Traversable[BallId],
                         usedPowerUps: Traversable[PowerUpId],
                         newPowerUps: Traversable[PowerUp],
                         sledKills: Traversable[SledKill],
                         sledAchievements: Traversable[Achievement])
}
