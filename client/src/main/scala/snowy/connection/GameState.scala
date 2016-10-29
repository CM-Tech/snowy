package snowy.connection

import snowy.GameClientProtocol._
import snowy.client.ClientDraw._
import snowy.playfield._
import vector.Vec2d

object GameState {
  var gPlayField = Vec2d(0, 0) // A playfield dummy until the game receives a different one
  var scoreboard = Scoreboard(0, Seq())

  // TODO add a gametime timestamp to these, and organize together into a class
  var serverTrees = Store[Tree]()
  var serverSnowballs = Store[Snowball]()
  var serverSleds = Store[Sled]()
  var serverMySled = Sled.dummy
  var serverTracks = Seq[Seq[Track]]()

  //When the client receives the state of canvas, draw all sleds
  def receivedState(state: State): Unit = {
    clearScreen()
    serverSnowballs = Store(state.snowballs)
    serverSleds = Store(state.sleds)
    serverMySled = state.mySled
    serverTracks = state.tracks
    drawState(serverSnowballs, serverSleds, serverMySled, serverTracks, serverTrees, gPlayField, scoreboard)
  }
}
