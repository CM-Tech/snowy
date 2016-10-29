package snowy.server

import snowy.GameClientProtocol._
import snowy.playfield.PlayId.SledId
import snowy.playfield._
import snowy.server.GameSeeding.randomTrees
import socketserve.ConnectionId

import scala.collection.mutable


/** Records the current state of sleds, trees, snowballs etc. */
trait GameState {
  self: GameControl =>

  val gridSpacing = 100.0
  var sleds = Store[Sled]()
  var snowballs = Store[Snowball]()
  val trees: Set[Tree] = randomTrees()
  var tracks = Map[SledId, List[Track]]()
  val users = mutable.Map[ConnectionId, User]()
  val sledMap = mutable.Map[ConnectionId, SledId]()
  var gameTime = System.currentTimeMillis()
  var lastTracks = gameTime
  val commands = new PendingCommands

  /** Package the relevant state to communicate to the client */
  protected def currentState(): Iterable[(ConnectionId, State)] = {
    val clientSnowballs = snowballs.items.toSeq
    val clientTracks = tracks.values.toSeq

    (for {
      mySled <- sleds.items
      connectionId <- mySled.connectionId
    } yield {
      val otherSleds = sleds.items.filter(_.id != mySled.id).toSeq
      connectionId -> State(mySled, otherSleds, clientSnowballs, clientTracks)
    }).toSeq
  }

  implicit class SledIdOps(id: SledId) {
    def sled: Option[Sled] = {
      sleds.items.find(_.id == id)
    }

    def user: Option[User] = id.connectionId.map(users(_))

    def connectionId: Option[ConnectionId] = {
      sledMap.collectFirst {
        case (connectionId, sledId) if id == sledId =>
          connectionId
      }
    }
  }

  implicit class ConnectionIdOps(id:ConnectionId) {
    def sled:Option[Sled] = sledMap.get(id).flatMap(_.sled)
  }

  implicit class SledIndices(sled: Sled) {
    def connectionId: Option[ConnectionId] = {
      sledMap.collectFirst {
        case (connectionId, sledId) if sled.id == sledId =>
          connectionId
      }
    }

    def remove(): Unit = {
      sled.id.connectionId.foreach(sledMap.remove(_))
      sleds = sleds.remove(sled)
    }
  }

}
