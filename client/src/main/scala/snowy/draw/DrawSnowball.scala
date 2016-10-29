package snowy.draw

import snowy.draw.GameColors._
import snowy.client.ClientDraw.ctx
import vector.Vec2d

class DrawSnowball(pos: Vec2d, size: Double) {
  ctx.strokeStyle = lineColors.toString
  ctx.fillStyle = snowball
  ctx.lineWidth = size / 10
  ctx.beginPath()
  ctx.arc(pos.x, pos.y, size, 0, 2 * Math.PI)
  ctx.fill()
  ctx.stroke()
}