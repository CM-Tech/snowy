package snowy.client

import scala.scalajs.js.JSApp
import org.scalajs.dom._
import snowy.draw.LoginScreen

object ClientMain extends JSApp {
  val loginScreen = new LoginScreen()
  var connected: Option[Connection] = None
  def main(): Unit = {
    loginScreen.setup()
  }

  //When the users sends the login form, send it as a username to the server
  document.getElementById("login-form").asInstanceOf[html.Form].onsubmit = { event: Event =>
    //Connect to the WebSocket server
    // TODO use
    //   case Some(connection) =>
    //   case None =>
    connected match {
      case x if x.isEmpty => connected = Some(new Connection(document.getElementById("username").asInstanceOf[html.Input].value))
      case x if x.isDefined => connected.get.reSpawn()
      case _ =>
    }

    //Stop drawing the snow as a background
    loginScreen.stop()

    //Do not redirect
    false
  }
}
