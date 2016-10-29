package snowy.client

import org.scalajs.dom
import org.scalajs.dom._
import snowy.GameClientProtocol.Scoreboard
import snowy.draw.GameColors.Sled._
import snowy.draw.GameColors.clearColor
import snowy.draw._
import snowy.playfield._
import vector.Vec2d

object ClientDraw {
  var size = Vec2d(window.innerWidth, window.innerHeight)
  val gameCanvas = document.getElementById("game-c").asInstanceOf[html.Canvas]
  gameCanvas.width = size.x.toInt
  gameCanvas.height = size.y.toInt
  val ctx = gameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def drawState(snowballs: Store[Snowball], sleds: Store[Sled], mySled: Sled, tracks: Seq[Seq[Track]],trees: Store[Tree], border: Vec2d, scoreboard: Scoreboard): Unit = {
    clearScreen()

    var portal = new Portal(
      Rect(
        mySled.pos - Vec2d(1000, 500),
        Vec2d(2000, 1000)
      )
    )(sleds.items, snowballs.items, trees.items, tracks)

    portal = portal.convertToScreen(size, border)
    new DrawGrid(mySled.pos * portal.scale, portal.scale)

    portal.snowballs.foreach { snowball =>
      new DrawSnowball(snowball.pos, snowball.size / 2 * portal.scale)
    }
    portal.tracks.foreach { sledTracks =>
      new DrawTrack(sledTracks, 35 * portal.scale)
    }
    portal.sleds.foreach { sled =>
      new DrawSled(sled.userName, sled.pos, 35 * portal.scale, sled.healthPercent, sled.turretRotation, sled.rotation, bodyRed)
    }
    new DrawSled(mySled.userName, size / 2, 35 * portal.scale, mySled.healthPercent, mySled.turretRotation, mySled.rotation, bodyGreen)

    portal.trees.foreach { tree =>
      new DrawTree(tree.pos, 100 * portal.scale)
    }

    val rawminimap = new Portal(
      Rect(
        Vec2d(0, 0),
        border
      )
    )(Set(mySled), Set(), trees.items, Seq())
    val miniscale = Math.max(border.x / size.x, border.y / size.y) * 2
    val minimap = rawminimap.convertToScreen(border / miniscale, border)
    val miniscaled = border / miniscale
    val minipos = size * .99 - miniscaled
    ctx.beginPath()
    ctx.fillStyle = "rgba(255, 255, 255, 0.5)"
    ctx.fillRect(minipos.x, minipos.y, miniscaled.x, miniscaled.y)
    minimap.trees.foreach { tree =>
      val newPos = tree.pos + minipos
      new DrawTree(newPos, 50 * minimap.scale)
    }
    minimap.sleds.foreach { sled =>
      val newPos = sled.pos + minipos
      new DrawSled(sled.userName, newPos, 70 * minimap.scale, sled.healthPercent, sled.turretRotation, sled.rotation, bodyRed)
    }

    val scorescale = Math.max(210 / size.x, 255 / size.y) * 2
    val scorescaled = Vec2d(210, 225) / scorescale
    val scorepos = Vec2d(size.x * .99 - scorescaled.x, size.y * .01)
    ctx.fillStyle = "rgba(255, 255, 255, 0.5)"
    ctx.fillRect(scorepos.x, scorepos.y, scorescaled.x, scorescaled.y)

    ctx.font = "10px Arial"
    ctx.fillStyle = "rgb(0, 0, 0)"
    for ((score, index) <- scoreboard.scores.sortWith(_.score>_.score).zipWithIndex) {
      ctx.fillText(score.userName, scorepos.x + 30, scorepos.y + 50 + 10 * index)
      ctx.fillText(score.score.toInt.toString, scorepos.x + scorescaled.x - 50, scorepos.y + 50 + 10 * index)
    }
    ctx.fillText(mySled.userName, scorepos.x + 30, scorepos.y + scorescaled.y - 10)
    ctx.fillText(scoreboard.myScore.toInt.toString, scorepos.x + scorescaled.x - 50, scorepos.y + scorescaled.y - 10)
  }

  def clearScreen(): Unit = {
    ctx.fillStyle = clearColor.toString
    ctx.fillRect(0, 0, size.x, size.y)
  }

  case class Size(width: Int, height: Int)

  window.onresize = (_: UIEvent) => {
    size = Vec2d(window.innerWidth, window.innerHeight)
    gameCanvas.width = size.x.toInt
    gameCanvas.height = size.y.toInt

    clearScreen()
  }
}
