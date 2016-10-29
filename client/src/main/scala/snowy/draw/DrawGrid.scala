package snowy.draw

import snowy.draw.GameColors.lineColors
import snowy.client.ClientDraw._
import vector.Vec2d


class DrawGrid(sled: Vec2d, scale: Double) {
  ctx.strokeStyle = (lineColors * 2.4).toString
  val lineGap = (10 * scale).toInt
  ctx.lineWidth = 1
  for (i <- 0 to size.x.toInt / lineGap) {
    ctx.beginPath()
    ctx.moveTo(i * lineGap - sled.x % lineGap, 0)
    ctx.lineTo(i * lineGap - sled.x % lineGap, size.y)
    ctx.stroke()
  }
  for (i <- 0 to size.y.toInt / lineGap) {
    ctx.beginPath()
    ctx.moveTo(0, i * lineGap - sled.y % lineGap)
    ctx.lineTo(size.x, i * lineGap - sled.y % lineGap)
    ctx.stroke()
  }
}