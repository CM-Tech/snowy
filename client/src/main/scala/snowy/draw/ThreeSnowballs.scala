package snowy.draw

import minithree.THREE
import minithree.THREE.{MeshLambertMaterialParameters, Object3D, Vector3}
import snowy.client.DrawPlayfield.removeDeaths
import snowy.client.{DrawPlayfield, UpdateGroup}
import snowy.playfield.PlayId.BallId
import snowy.playfield.{Sled, Snowball}
import vector.Vec2d

import scala.scalajs.js.Dynamic

class ThreeSnowballs(snowballGeo: THREE.Geometry) {
  val snowballMat = new THREE.MeshLambertMaterial(
    Dynamic
      .literal(color = 0x1878f0)
      .asInstanceOf[MeshLambertMaterialParameters]
  )

  val snowballGroup = new UpdateGroup[Snowball](new THREE.Object3D())

  def updateSnowball(playfieldSnowball: Snowball,
                     threeSnowball: Object3D,
                     myPos: Vector3): Unit = {
    DrawPlayfield.setThreePosition(
      threeSnowball,
      playfieldSnowball.position,
      myPos,
      Sled.basicRadius * 2
    )

    threeSnowball.rotation.y = playfieldSnowball.speed.unit.angle(Vec2d.unitUp)
  }

  def updateThreeSnowballs(snowballs: Set[Snowball], myPos: Vector3): Unit = {
    snowballs.foreach { snowball1 =>
      snowballGroup.map.get(snowball1.id) match {
        case Some(snowball) => updateSnowball(snowball1, snowball, myPos)
        case None           => addSnowball(snowball1)
      }
    }
  }

  def addSnowball(snowball: Snowball): Unit = snowballGroup.add(createSnowball(snowball))

  def createSnowball(snowball: Snowball): Object3D = {
    val newSnowball: Object3D = new THREE.Mesh(snowballGeo, snowballMat)
    newSnowball.scale.set(
      snowball.radius,
      snowball.radius,
      snowball.radius
    )

    DrawPlayfield.setThreePosition2(newSnowball, snowball.position, Sled.basicRadius * 2)

    newSnowball.name = snowball.id.id.toString
    newSnowball
  }

  def removeSnowballs(deaths: Seq[BallId]): Unit =
    removeDeaths[Snowball](snowballGroup, deaths)
}
