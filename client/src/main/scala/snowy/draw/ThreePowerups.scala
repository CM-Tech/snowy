package snowy.draw

import minithree.THREE
import minithree.THREE.{MeshLambertMaterialParameters, Object3D, Vector3}
import snowy.client.DrawPlayfield.removeDeaths
import snowy.client.{DrawPlayfield, UpdateGroup}
import snowy.playfield.PlayId.PowerUpId
import snowy.playfield.{HealthPowerUp, PowerUp, SpeedPowerUp}
import vector.Vec2d

import scala.scalajs.js.Dynamic

class ThreePowerups(powerupGeo: THREE.Geometry) {

  val powerupGroup = new UpdateGroup[PowerUp](new THREE.Object3D())

  def wrapThreePowerups(myPos: Vector3): Unit = {
    powerupGroup.map.foreach {
      case (_, powerup) =>
        DrawPlayfield.playfieldWrap(
          powerup,
          Vec2d(powerup.position.x, powerup.position.z),
          myPos
        )
    }
  }

  def createMaterial(color: Int) = new THREE.MeshLambertMaterial(
    Dynamic
      .literal(color = color)
      .asInstanceOf[MeshLambertMaterialParameters]
  )

  def addPowerup(powerup: PowerUp): Unit = {
    val powerupMat = powerup match {
      case _: HealthPowerUp => createMaterial(0x00ff00)
      case _: SpeedPowerUp  => createMaterial(0xffff00)
    }

    val newPowerup: Object3D = new THREE.Mesh(powerupGeo, powerupMat)
    newPowerup.scale.set(
      powerup.radius,
      powerup.radius,
      powerup.radius
    )

    DrawPlayfield.setThreePosition2(newPowerup, powerup.position, 0)

    newPowerup.name = powerup.id.id.toString
    powerupGroup.add(newPowerup)
  }

  def removePowerup(deaths: Seq[PowerUpId]): Unit =
    removeDeaths[PowerUp](powerupGroup, deaths)
}
