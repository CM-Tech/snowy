package snowy.server

import scala.collection.mutable
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.util.ByteString
import boopickle.Default._
import com.typesafe.scalalogging.StrictLogging
import snowy.Awards._
import snowy.GameClientProtocol._
import snowy.GameConstants._
import snowy.GameServerProtocol._
import snowy.playfield.GameMotion._
import snowy.playfield.PlayId.{BallId, SledId}
import snowy.playfield.{Sled, _}
import snowy.robot.StationaryRobot
import snowy.server.GameSeeding.randomSpot
import snowy.util.{MeasurementRecorder, Span}
import snowy.util.Span.time
import socketserve._
import vector.Vec2d

class GameControl(api: AppHostApi)(implicit system: ActorSystem,
                                   measurementRecorder: MeasurementRecorder)
    extends AppController with GameState with StrictLogging {
  override val turnPeriod = 20 milliseconds
  val messageIO           = new MessageIO(api)
  val connections         = mutable.Map[ConnectionId, ClientConnection]()
  val gameTurns           = new GameTurn(this, turnPeriod)
  val robots              = new RobotHost(this)

  import gameStateImplicits._
  import gameTurns.gameTime
  import messageIO.sendMessage

  robotSleds()

  /** a new player has connected */
  override def open(id: ConnectionId): Unit = {
    logger.trace(s"open $id")
    connections(id) = new ClientConnection(id, messageIO)
    val clientPlayfield = Playfield(playfield.x.toInt, playfield.y.toInt)
    sendMessage(clientPlayfield, id)
    sendMessage(Trees(trees.toSeq), id)
  }

  /** Called when a connection is dropped */
  override def gone(connectionId: ConnectionId): Unit = {
    logger.info(s"gone $connectionId")
    for {
      sledId <- sledMap.get(connectionId)
      sled   <- sledId.sled
    } {
      sled.remove()
    }
    users.remove(connectionId)
    pendingControls.commands.remove(connectionId)
    connections.remove(connectionId)
  }

  /** decode received binary message then pass on to handler */
  override def message(id: ConnectionId, msg: ByteString): Unit = {
    handleMessage(id, Unpickle[GameServerMessage].fromBytes(msg.asByteBuffer))
  }

  /** Run the next game turn. (called on a periodic timer) */
  override def turn(): Unit = {
    Span.root("GameControl.turn").timeSpan { implicit span =>
      val deltaSeconds = gameTurns.nextTurn()
      time("GameControl.robotsTurn"){ robots.robotsTurn() }
      applyDrive(deltaSeconds)
      applyCommands(deltaSeconds)
      val turnDeaths = gameTurns.turn(deltaSeconds)
      reapDead(turnDeaths.deadSleds)
      reportExpiredSnowballs(turnDeaths.deadSnowBalls)
      time("sendUpdates") {
        sendUpdates()
      }
    }
  }

  /** Process a GameServerMessage from a game client browser or robot */
  def handleMessage(id: ClientId, msg: GameServerMessage): Unit = {
    logger.trace(s"handleMessage: $msg received from client $id")
    msg match {
      case Join(name, sledKind, skiColor) =>
        userJoin(id, name.slice(0, 15), sledKind, skiColor)
      case TurretAngle(angle) => rotateTurret(id, angle)
      case TargetAngle(angle) => targetDirection(id, angle)
      case Shoot(time)        => id.sled.foreach(shootSnowball(_))
      case Start(cmd, time)   => startControl(id, cmd, time)
      case Stop(cmd, time)    => stopControl(id, cmd, time)
      case Boost(time)        => id.sled.foreach(boostSled(_, time))
      case Pong               => optNetId(id).foreach(pong(_))
      case ReJoin             => rejoin(id)
      case ClientPing         => optNetId(id).foreach(sendMessage(ClientPong, _))
      case TestDie            => reapSled(sledMap(id))
      case RequestGameTime(clientTime) =>
        optNetId(id).foreach(reportGameTime(_, clientTime))
    }
  }

  /** client has started to operate a sled control. e.g. shooting, braking */
  private def startControl(id: ClientId, cmd: StartStopControl, time: Long): Unit = {
    cmd match {
      case persistentControl: PersistentControl =>
        pendingControls.startCommand(id, persistentControl, time)
      case driveControl: DriveControl =>
        for (sled <- id.sled) {
          driveControl match {
            case Coasting => sled.driveMode.driveMode(SledDrive.Coasting)
            case Slowing  => sled.driveMode.driveMode(SledDrive.Braking)
          }
        }
    }
  }

  /** client has stopped a sled control. e.g. shooting, braking */
  private def stopControl(id: ClientId, cmd: StartStopControl, time: Long): Unit = {
    cmd match {
      case persistentControl: PersistentControl =>
        pendingControls.stopCommand(id, persistentControl, time)
      case driveControl: DriveControl =>
        for (sled <- id.sled) {
          sled.driveMode.driveMode(SledDrive.Driving)
        }
    }
  }

  private def optNetId(id: ClientId): Option[ConnectionId] = {
    id match {
      case netId: ConnectionId => Some(netId)
      case i: RobotId          => None
    }
  }

  private def pong(netId: ConnectionId): Unit = {
    connections.get(netId).foreach(_.pongReceived())
  }

  /** Add some autonomous players to the game */
  private def robotSleds(): Unit = {
    // TODO what should this be in the real game?
    // one idea: start with ~20 robots but don't rejoin them when the number of users
    // exceeds a threshold?
    (1 to 4).foreach { _ =>
      robots.createRobot(StationaryRobot.apply)
    }
  }

  private def sendUpdates(): Unit = {
    val state = currentState()
    connections.keys.foreach { connectionId =>
      sendMessage(state, connectionId)
    }
    sendScores()
  }

  /** Send the current score to the clients */
  private def sendScores(): Unit = {
    val scores = {
      val rawScores = users.values.map { user =>
        Score(user.name, user.score)
      }.toSeq
      val sorted = rawScores.sortWith { (a, b) =>
        a.score > b.score
      }
      sorted.take(10)
    }
    users.collect {
      case (id: ConnectionId, user) if user.timeToSendScore(gameTime) =>
        user.scoreSent(gameTime)
        val scoreboard = Scoreboard(user.score, scores)
        sendMessage(scoreboard, id)
    }
  }

  private def reportGameTime(netId: ConnectionId, clientTime: Long): Unit = {
    logger.trace {
      val clientTimeDelta = clientTime - System.currentTimeMillis()
      s"client $netId time vs server time: $clientTimeDelta"
    }

    connections.get(netId) match {
      case Some(connection) => reportGameTime(connection.roundTripTime)
      case None             => logger.warn(s"reportGameTime: connection $netId not fouud")
    }

    def reportGameTime(rtt: Long): Unit = {
      val msg = GameTime(System.currentTimeMillis(), (rtt / 2).toInt)
      messageIO.sendMessage(msg, netId)
    }
  }

  private def applyTurn(deltaSeconds: Double): Unit = {
    for (sled <- sleds.items) {
      val tau             = math.Pi * 2
      val distanceBetween = (sled.targetRotation - sled.rotation) % tau
      val wrapping        = (distanceBetween % tau + (math.Pi * 3)) % tau - math.Pi
      val dir             = math.round(wrapping * 10).signum // precision of when to stop
      sled.rotation += deltaSeconds * dir * sled.kind.rotationSpeed
    }
  }

  /** apply any pending but not yet cancelled commands from user actions,
    * e.g. turning or slowing */
  private def applyDrive(deltaSeconds: Double): Unit = {
    for (sled <- sleds.items) {
      sled.driveMode.driveSled(sled, deltaSeconds)
    }
  }

  /** apply any pending but not yet cancelled commands from user actions,
    * e.g. turning or slowing */
  private def applyCommands(deltaSeconds: Double): Unit = {

    pendingControls.foreachCommand { (id, command, time) =>
      id.sled.foreach { sled =>
        command match {
          case Left  => turnSled(sled, LeftTurn, deltaSeconds)
          case Right => turnSled(sled, RightTurn, deltaSeconds)
          case TurretLeft =>
            id.sled.foreach(_.turretRotation -= (math.Pi / turnTime) * deltaSeconds)
          case TurretRight =>
            id.sled.foreach(_.turretRotation += (math.Pi / turnTime) * deltaSeconds)
          case Shooting => shootSnowball(sled)
        }
      }
    }
  }

  private def reportExpiredSnowballs(balls: Traversable[BallId]): Unit = {
    if (balls.nonEmpty) {
      val deaths = SnowballDeaths(balls.toSeq)
      connections.keys.foreach(sendMessage(deaths, _))
    }
  }

  /** apply a push to a sled */
  private def boostSled(sled: Sled, clientTime: Long): Unit = {
    if (gameTime - sled.lastBoostTime >= sled.boostRecoveryTime * 1000) {
      sled.lastBoostTime = gameTime
      SledDrive.accelerate(sled, sled.boostAcceleration)
    }
  }

  private def shootSnowball(sled: Sled): Unit = {
    if (sled.lastShotTime + sled.minRechargeTime < gameTime) {
      val launchAngle = sled.turretRotation + sled.bulletLaunchAngle
      val launchPos   = sled.bulletLaunchPosition.rotate(-sled.turretRotation)
      val direction   = Vec2d.fromRotation(-launchAngle)
      val ball = Snowball(
        ownerId = sled.id,
        _position = wrapInPlayfield(sled.pos + launchPos), // TODO don't use _position
        speed = sled.speed + (direction * sled.bulletSpeed),
        radius = sled.bulletRadius,
        mass = sled.bulletMass,
        spawned = gameTime,
        impactDamage = sled.bulletImpactFactor,
        health = sled.bulletHealth,
        lifetime = sled.bulletLifetime
      )
      snowballs = snowballs.add(ball)

      val recoilForce = direction * -sled.bulletRecoil
      sled.speed = sled.speed + recoilForce
      sled.lastShotTime = gameTime
    }
  }

  // TODO Notify clients who they kill, and who killed them
  /** Notify clients about sleds that have been killed, remove sleds from the game */
  private def reapDead(dead: Traversable[SledDied]): Unit = {
    val deadSleds =
      dead.map {
        case SledDied(sledId) => sledId
      }.toSeq

    if (deadSleds.nonEmpty) {
      val deaths = SledDeaths(deadSleds)
      connections.keys.foreach(sendMessage(deaths, _))

      for { sledId <- deadSleds; sled <- sledId.sled } {
        sendDied(sledId)
        val connectIdStr= sledId.connectionId.map(id => s"(connection: $id) ").getOrElse("")
        logger.info(s"sled ${sledId.id} killed $connectIdStr sledCount:${sledMap.size}")
        sled.remove()
      }
    }
  }

  private def reapSled(sledId: SledId): Unit = {
    sendDied(sledId)
    sledId.sled.foreach(_.remove())
  }

  private def sendDied(sledId: SledId): Unit = {
    sledId.connectionId match {
      case Some(netId: ConnectionId) => sendMessage(Died, netId)
      case Some(robotId: RobotId)    => robots.died(robotId)
      case None                      => logger.warn(s"reapSled connection not found for sled: $sledId")
    }
  }

  private def reportJoinedSled(connectionId: ClientId, sledId: SledId): Unit = {
    connectionId match {
      case robotId: RobotId    => robots.joined(robotId, sledId)
      case netId: ConnectionId => sendMessage(MySled(sledId), netId)
    }
  }

  private def newRandomSled(userName: String,
                            sledKind: SledKind,
                            color: SkiColor): Sled = {
    // TODO what if sled is initialized atop a tree?
    Sled(
      userName = userName,
      initialPosition = randomSpot(),
      kind = sledKind,
      color = color
    )
  }

  /** Called when a user sends her name and starts in the game */
  private def userJoin(id: ClientId,
                       userName: String,
                       sledKind: SledKind,
                       skiColor: SkiColor): Unit = {
    logger.info(
      s"user joined: $userName  id: $id  kind: $sledKind  userCount:${users.size}"
    )
    val user =
      new User(userName, createTime = gameTime, sledKind = sledKind, skiColor = skiColor)
    users(id) = user
    val sled = createSled(id, user, sledKind)
    reportJoinedSled(id, sled.id)
  }

  private def rejoin(id: ClientId): Unit = {
    users.get(id) match {
      case Some(user) =>
        logger.info(s"user rejoined: ${user.name}")
        val sled = createSled(id, user, user.sledKind)
        reportJoinedSled(id, sled.id)
      case None =>
        logger.warn(s"user not found to rejoin: $id")
    }
  }

  private def createSled(connectionId: ClientId, user: User, sledKind: SledKind): Sled = {
    val sled = newRandomSled(user.name, sledKind, user.skiColor)
    sleds = sleds.add(sled)
    sledMap(connectionId) = sled.id
    sled
  }

  /** Rotate the turret on a sled */
  private def rotateTurret(id: ClientId, angle: Double): Unit = {
    id.sled.foreach(_.turretRotation = angle)
  }

  /** Point the sled in this direction */
  private def targetDirection(id: ClientId, angle: Double): Unit = {
    id.sled.foreach { sled =>
      sled.targetRotation = -angle
    }
  }

}
