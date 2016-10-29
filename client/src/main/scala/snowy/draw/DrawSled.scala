package snowy.draw

import snowy.draw.GameColors.lineColors
import snowy.client.ClientDraw.ctx
import vector.Vec2d

class DrawSled(name: String, pos: Vec2d, size: Double, health: Double, cannonRotation: Double, rotation: Double, color: String) {
  val x = pos.x
  val y = pos.y
  //val size = 35.0

  //Global strokeStyle
  ctx.strokeStyle = lineColors.toString

  //Draw two skis
  ctx.lineCap = "round"
  ctx.lineWidth = size * 9 / 55
  ctx.translate(x, y)
  ctx.rotate(-rotation)
  ctx.beginPath()
  ctx.moveTo(size * 5 / 11, -size * 25 / 22)
  ctx.lineTo(size * 5 / 11, size * 25 / 22)
  ctx.stroke()
  ctx.beginPath()
  ctx.moveTo(-size * 5 / 11, -size * 25 / 22)
  ctx.lineTo(-size * 5 / 11, size * 25 / 22)
  ctx.stroke()
  ctx.setTransform(1, 0, 0, 1, 0, 0)

  //Draw the barrel for snowballs
  ctx.translate(x + size / 55, y)
  ctx.rotate(cannonRotation)
  ctx.lineWidth = size / 20
  ctx.fillStyle = "rgb(153, 153, 153)"
  ctx.rect(-size * 3 / 22, 0, size * 3 / 11, size * 9 / 10)
  ctx.fill()
  ctx.stroke()
  ctx.setTransform(1, 0, 0, 1, 0, 0)

  //Set the color for you vs other players
  ctx.fillStyle = color

  //Draw the main body
  ctx.beginPath()
  ctx.arc(x, y, size / 2, 0, 2 * Math.PI)
  ctx.fill()
  ctx.stroke()

  //Draw the name
  ctx.font = (size * 3 / 11) + "px Arial"
  ctx.fillText(name, x - (ctx.measureText(name).width / 2), y - size * 27 / 22)

  if (health < 1) {
    ctx.lineWidth = size * 3 / 25
    ctx.strokeStyle = "rgb(85, 85, 85)"
    ctx.beginPath()
    ctx.moveTo(x - size * 2 / 5, y + size * 5 / 5)
    ctx.lineTo(x + size * 2 / 5, y + size * 5 / 5)
    ctx.stroke()

    ctx.lineWidth = size * 2 / 25
    ctx.strokeStyle = "rgb(134, 198, 128)"
    ctx.beginPath()
    ctx.moveTo(x - size * 2 / 5, y + size * 5 / 5)
    ctx.lineTo(x - size * 2 / 5 + size * 4 / 5 * health, y + size * 5 / 5)
    ctx.stroke()
  }
}
