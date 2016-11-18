package snowy.connection

import org.scalajs.dom._
import snowy.GameClientProtocol._
import snowy.client.ClientDraw._
import snowy.playfield._
import vector.Vec2d
import snowy.playfield.GameMotion._

object GameState {
  var gPlayField = Vec2d(0, 0) // A playfield dummy until the game receives a different one
  var scoreboard = Scoreboard(0, Seq())

  // TODO add a gametime timestamp to these, and organize together into a class
  var serverTrees             = Store[Tree]()
  private var serverSnowballs = Store[Snowball]()
  private var serverSleds     = Store[Sled]()
  private var serverMySled    = Sled.dummy
  var gameTime                = 0L

  private var gameLoop: Option[Int]            = None
  private var turning: Turning                 = NoTurn
  var serverGameClock: Option[ServerGameClock] = None // HACK! TODO make GameState an instance

  /** set the client state to the state from the server
    * @param state the state sent from the server */
  def receivedState(state: State): Unit = {
    serverSnowballs = Store(state.snowballs)
    serverSleds = Store(state.sleds)
    serverMySled = state.mySled
    gameTime = state.gameTime
  }

  def startTurn(direction: Turning): Unit = turning = direction

  def startRedraw(): Unit = {
    stopRedraw()
    val loop = window.setInterval(() => {
      val deltaSeconds = nextTimeSlice()
      refresh(math.max(deltaSeconds, 0))
    }, 20)
    gameLoop = Some(loop)
  }

  def stopRedraw(): Unit = gameLoop.foreach(id => window.clearInterval(id))

  private def applyTurn(sled: Sled, deltaSeconds: Double): Unit = {
    turning match {
      case RightTurn => GameMotion.turnSled(sled, RightTurn, deltaSeconds)
      case LeftTurn  => GameMotion.turnSled(sled, LeftTurn, deltaSeconds)
      case NoTurn    => None
    }
  }

  private def moveOneSled(sled: Sled, deltaSeconds: Double): Unit = {
    moveSleds(List(sled), deltaSeconds)
  }

  private def nextState(deltaSeconds: Double): Unit = {
    moveSnowballs(serverSnowballs.items, deltaSeconds)
    applyTurn(serverMySled, deltaSeconds)
    moveOneSled(serverMySled, deltaSeconds)
    moveSleds(serverSleds.items, deltaSeconds)
  }

  /** Advance to the next game simulation frame.
    *
    * @return the time in seconds since the last frame */
  private def nextTimeSlice(): Double = {
    val newTurn =
      serverGameClock.map(_.serverGameTime).getOrElse(System.currentTimeMillis())
    val deltaSeconds = (newTurn - gameTime) / 1000.0
    gameTime = newTurn
    deltaSeconds
  }

  private def refresh(deltaSeconds: Double): Unit = {
    nextState(deltaSeconds)

    clearScreen()
    drawState(
      serverSnowballs,
      serverSleds,
      serverMySled,
      serverTrees,
      gPlayField,
      scoreboard)
  }

}
