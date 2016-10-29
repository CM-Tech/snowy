package snowy

import snowy.playfield._

/** messages sent to the server */
object GameServerProtocol {

  sealed trait GameServerMessage

  sealed trait StartStopCommand

  case class Join(userName: String, sledKind: SledKind = SpikySled) extends GameServerMessage

  case class ReJoin(sledKind: SledKind = BasicSled) extends GameServerMessage

  case class TurretAngle(angle: Double) extends GameServerMessage

  case object Shoot extends GameServerMessage

  case class Start(cmd: StartStopCommand) extends GameServerMessage

  case class Stop(cmd: StartStopCommand) extends GameServerMessage

  case object Left extends StartStopCommand

  case object Right extends StartStopCommand

  case object Slow extends StartStopCommand

  case object Push extends StartStopCommand

  case object Pong extends GameServerMessage

  case object TestDie extends GameServerMessage

}

/** messages sent to the web client */
object GameClientProtocol {

  sealed abstract class GameClientMessage

  case class State(mySled: Sled, sleds: Seq[Sled], snowballs: Seq[Snowball], tracks: Seq[Seq[Track]]) extends GameClientMessage

  case class Playfield(width: Int, height: Int) extends GameClientMessage

  case class Trees(trees: Seq[Tree]) extends GameClientMessage

  case object Died extends GameClientMessage

  case class Score(userName: String, score: Double)

  case class Scoreboard(myScore: Double, scores: Seq[Score]) extends GameClientMessage

  case object Ping extends GameClientMessage

  case class GameTime(millis: Long, oneWayDelay: Int) extends GameClientMessage

}
