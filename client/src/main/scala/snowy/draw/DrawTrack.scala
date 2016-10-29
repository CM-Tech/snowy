package snowy.draw

import snowy.draw.GameColors.clearColor
import snowy.client.ClientDraw.ctx
import snowy.playfield.Track
import vector.Vec2d

class DrawTrack(tracks: Seq[Track], size: Double) {
  ctx.strokeStyle = (clearColor * 0.9).toString
  ctx.lineWidth = size * 9 / 55
  ctx.lineCap = "round"

  var leftTracks = Seq[Vec2d]()
  var rightTracks = Seq[Vec2d]()
  val portalScale = size / 35
  tracks.sortWith(_.spawned > _.spawned).foreach { track =>
    rightTracks = rightTracks :+ track.rightSki * portalScale + track.pos
    leftTracks = leftTracks :+ track.leftSki * portalScale + track.pos
  }
  if(rightTracks.nonEmpty) {
    bzCurve(rightTracks, 0.25, 1)
    bzCurve(leftTracks, 0.25, 1)
  }
  //Todo: Prevent massive loops
  def bzCurve(points: Seq[Vec2d], f: Double, t: Double) {
    def gradient(a: Vec2d, b: Vec2d): Double = {
      (b.y - a.y) / (b.x - a.x)
    }
    ctx.beginPath()
    ctx.moveTo(points.head.x, points.head.y)
    var dx1 = 0.0
    var dy1 = 0.0
    var preP = points.head
    points.zipWithIndex.foreach { case (curP, i) =>
      var dx2 = 0.0
      var dy2 = 0.0
      if (points.length > (i + 2)) {
        val nexP = points(i + 1)
        val m = gradient(preP, nexP)
        dx2 = (nexP.x - curP.x) * -f
        dy2 = dx2 * m * t
      } else {
        dx2 = 0
        dy2 = 0
      }
      ctx.bezierCurveTo(preP.x - dx1, preP.y - dy1, curP.x + dx2, curP.y + dy2, curP.x, curP.y)
      dx1 = dx2
      dy1 = dy2
      preP = curP
    }
    ctx.stroke()
  }
}
