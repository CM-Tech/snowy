package snowy.robot

import java.util.concurrent.ThreadLocalRandom

import snowy.GameServerProtocol._
import snowy.playfield._
import vector.Vec2d

object StationaryRobot {
  private var id = 0

  def apply(api: RobotApi): StationaryRobot = {
    id = id + 1
    new StationaryRobot(api, s"Stay$id")
  }
}

class StationaryRobot(api: RobotApi, name: String) extends Robot {
  val allSkis = Seq(BasicSkis, GreenSkis, RedSkis, YellowSkis)
  val mySkis  = allSkis(ThreadLocalRandom.current.nextInt(allSkis.length))

  val allTypes = Seq(BasicSled, GunnerSled, TankSled, SpeedySled, SpikySled)
  val myType   = allTypes(ThreadLocalRandom.current.nextInt(allTypes.length))

  var mySled = api.join(name, myType, mySkis)

  def refresh(state: RobotState): Unit = {
    val gameTime = System.currentTimeMillis()
    val random   = ThreadLocalRandom.current.nextDouble()
    val commands = random match {
      case _ if random < .005 => Seq(Start(Left, gameTime))
      case _ if random < .010 => Seq(Start(Right, gameTime))
      case _ if random < .030 => Seq(Stop(Right, gameTime), Stop(Left, gameTime))
      case _ if random < .040 => Seq(Start(Pushing, gameTime))
      case _ if random < .060 => Seq(Stop(Pushing, gameTime))
      case _ if random < .070 => Seq(Start(Shooting, gameTime))
      case _ if random < .090 => Seq(Stop(Shooting, gameTime))
      case _ if random < .190 =>
        Seq(TurretAngle(aimAtNearest(state.sleds, state.snowballs)))
      case _ => Seq()
    }
    commands.foreach { command =>
      api.sendToServer(command)
    }
  }

  def aimAtNearest(sleds: Traversable[Sled], snowballs: Traversable[Snowball]): Double = {
    /*val failedDistance = Sled.dummy.copy(_position = mySled._position - Vec2d(0, 1000))
    val closest: Sled = sleds.filterNot(sled => sled == mySled).fold(failedDistance) {
      case (closest: Sled, next: Sled) =>
        val distance        = next._position - mySled._position
        val closestDistance = closest._position - mySled._position
        if (distance.length <= closestDistance.length) {
          next
        } else closest
      case _ => failedDistance
    }
    -(closest._position - mySled._position).angle(Vec2d.unitUp)*/

    var closestBall = 10.0
    var ballAngle   = 0.0
    val closeSnowballs = snowballs
      .filterNot(ball => ball.ownerId == mySled.id)
      .filter(
        ball => (mySled._position - ball._position).length < closestBall
      )
    if (closeSnowballs.nonEmpty) {
      snowballs.foreach { ball =>
        val distance = ball._position - mySled._position
        if (distance.length <= closestBall) {
          closestBall = distance.length
          ballAngle = distance.angle(Vec2d.unitUp)
        }
      }
      -ballAngle
    } else {
      var closest = 1000.0
      var angle   = 0.0
      sleds.filterNot(sled => sled == mySled).foreach { sled =>
        val distance = sled._position - mySled._position
        if (distance.length <= closest) {
          closest = distance.length
          angle = distance.angle(Vec2d.unitUp)
        }
      }
      -angle
    }
  }

  //TODO: Call this
  def killed(): Unit = {
    mySled = api.rejoin()
  }
}