package snowy.draw

import minithree.THREE
import minithree.THREE.{
  MeshBasicMaterialParameters,
  MeshLambertMaterialParameters,
  Object3D,
  Vector3
}
import org.scalajs.dom.{document, html, CanvasRenderingContext2D}
import snowy.client.DrawPlayfield._
import snowy.client.{DrawPlayfield, UpdateGroup}
import snowy.playfield.PlayId.SledId
import snowy.playfield._

import scala.scalajs.js.Dynamic

class ThreeSleds(bodyGeo: THREE.Geometry, skisGeo: THREE.Geometry) {
  val sledGroup = new UpdateGroup[Sled](new THREE.Object3D())

  /** Revise position, etc. of a three js sled to match a playfield sled */
  def updateSled(playfieldSled: Sled, threeSled: Object3D, myPos: Vector3): Unit = {
    DrawPlayfield.setThreePosition(
      threeSled,
      playfieldSled.position,
      myPos,
      playfieldSled.radius * 2.5
    )

    val threeSledBody   = threeSled.children(0)
    val threeSledSkis   = threeSled.children(1)
    val threeSledHealth = threeSled.children(2)

    threeSledSkis.rotation.y = playfieldSled.rotation - math.Pi / 2
    threeSledBody.rotation.y = playfieldSled.turretRotation - math.Pi / 2

    threeSledSkis.scale.set(
      playfieldSled.radius,
      playfieldSled.radius,
      playfieldSled.radius
    )
    threeSledBody.scale.set(
      playfieldSled.radius,
      playfieldSled.radius,
      playfieldSled.radius
    )

    threeSledHealth.scale.x = playfieldSled.health / playfieldSled.maxHealth
  }

  def updateThreeSleds(sleds: Set[Sled], mySled: Sled): Unit = {
    val myPos = new Vector3(mySled.position.x, 0, mySled.position.y)

    sleds.foreach { sled1 =>
      sledGroup.map.get(sled1.id) match {
        case Some(sled) => updateSled(sled1, sled, myPos)
        case None       => addSled(sled1, sled1.id == mySled.id)
      }
    }
  }

  def addSled(sled: Sled, friendly: Boolean): Unit =
    sledGroup.add(createSled(sled, friendly))

  /** @return a threejs sled containing body, skis, health, and name */
  def createSled(sled: Sled, friendly: Boolean): Object3D = {
    val newSled = new THREE.Object3D()

    val skiMat = new THREE.MeshLambertMaterial(
      Dynamic
        .literal(color = sled.skiColor.color.to0x())
        .asInstanceOf[MeshLambertMaterialParameters]
    )

    val bodyColor = sled match {
      case _: BasicSled     => 0x00FFFF
      case _: TankSled      => 0xFF0000
      case _: GunnerSled    => 0x0000FF
      case _: SpeedySled    => 0x00FF00
      case _: SpikySled     => 0x888888
      case _: PrototypeSled => 0xFFFF00
      case _                => 0xFFFFFF
    }
    val bodyMat = new THREE.MeshLambertMaterial(
      Dynamic
        .literal(color = bodyColor)
        .asInstanceOf[MeshLambertMaterialParameters]
    )

    val body = new THREE.Mesh(bodyGeo, bodyMat)
    val skis = new THREE.Mesh(skisGeo, skiMat)

    val health =
      new THREE.Mesh(
        new THREE.PlaneGeometry(64, 16),
        if (friendly) createHealthMaterial(0x59B224) else createHealthMaterial(0xF43131)
      )

    health.position.z = -50
    health.rotation.x = 1.5 * math.Pi

    body.scale.set(sled.radius, sled.radius, sled.radius)
    skis.scale.set(sled.radius, sled.radius, sled.radius)

    newSled.add(body)
    newSled.add(skis)
    newSled.add(health)

    if (sled.userName != "") {
      val text = createText(sled.userName, 16)
      text.position.z = -70
      newSled.add(text)
    }

    DrawPlayfield.setThreePosition2(newSled, sled.position, sled.radius * 2.5)

    newSled.name = sled.id.id.toString
    newSled
  }

  def createHealthMaterial(color: Double): THREE.MeshBasicMaterial = {
    new THREE.MeshBasicMaterial(
      Dynamic
        .literal(
          color = color,
          transparent = true,
          depthTest = false
        )
        .asInstanceOf[MeshBasicMaterialParameters]
    )
  }

  def removeSleds(deaths: Seq[SledId]): Unit = removeDeaths[Sled](sledGroup, deaths)

  def nearestPow2(num: Double): Int =
    math.pow(2, math.round(math.log(num) / math.log(2))).toInt

  def getFixedCanvas(canvas: html.Canvas): html.Canvas = {
    val newCanvas = document.createElement("canvas").asInstanceOf[html.Canvas]
    newCanvas.width = nearestPow2(canvas.width)
    newCanvas.height = nearestPow2(canvas.height)
    val newCtx = newCanvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    newCtx.drawImage(canvas, 0, 0, newCanvas.width, newCanvas.height)
    newCanvas
  }

  def createText(text: String, scale: Double): Object3D = {
    val canvas = document.createElement("canvas").asInstanceOf[html.Canvas]
    val ctx    = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    val detail: Double = 4
    val height         = scale * detail
    ctx.font = height + "px Arial"
    val width = ctx.measureText(text).width

    canvas.width = width.toInt
    canvas.height = height.toInt

    ctx.clearRect(0, 0, width, height)
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"
    ctx.font = height + "px Arial"
    ctx.fillStyle = "black"
    ctx.fillText(text, width / 2, height / 2)

    val texture = new THREE.Texture(getFixedCanvas(canvas))
    texture.wrapS = THREE.RepeatWrapping
    texture.wrapT = THREE.RepeatWrapping

    val textPlane = new THREE.PlaneGeometry(width, height)
    val material = new THREE.MeshBasicMaterial(
      Dynamic
        .literal(map = texture, transparent = true, depthTest = false)
        .asInstanceOf[MeshBasicMaterialParameters]
    )
    val mesh = new THREE.Mesh(textPlane, material)

    mesh.rotation.x = 1.5 * math.Pi
    mesh.scale.set(1 / detail, 1 / detail, 1 / detail)
    texture.needsUpdate = true

    mesh
  }
}
