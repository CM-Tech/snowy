package snowy.draw

import minithree.THREE
import minithree.THREE.Object3D
import minithree.raw.Vector3
import snowy.GameConstants
import snowy.client.DrawState
import snowy.client.DrawState.{Geos, Groups, Mats}
import snowy.playfield.PlayId.BallId
import snowy.playfield.Snowball

object UpdateSnowballs {
  def updateCsnowballs(snowballs: Set[Snowball], myPos: Vector3): Unit = {
    snowballs.foreach { snowball1 =>
      var idExists = false
      Groups.threeSnowballs.children.zipWithIndex.foreach {
        case (aSnowball, index) =>
          if (aSnowball.name == snowball1.id.id.toString) {
            idExists = true
            val csnowball = Groups.threeSnowballs.children(index)
            val newPos = DrawState.playfieldWrap(
              new Vector3(snowball1._position.x, 0, snowball1._position.y),
              myPos,
              new Vector3(GameConstants.playfield.x, 0, GameConstants.playfield.y)
            )
            csnowball.position.x = newPos.x
            csnowball.position.z = newPos.z
          }
      }
      if (!idExists) {
        Groups.threeSnowballs.add(createSnowball(snowball1, myPos))
      }
    }
  }

  def createSnowball(snowball: Snowball, myPos: Vector3): Object3D = {
    val newSnowball: Object3D = new THREE.Mesh(Geos.snowball, Mats.snowball)
    newSnowball.scale.set(
      snowball.radius,
      snowball.radius,
      snowball.radius
    )
    val newPos = DrawState.playfieldWrap(
      new Vector3(snowball._position.x, 0, snowball._position.y),
      myPos,
      new Vector3(GameConstants.playfield.x, 0, GameConstants.playfield.y)
    )
    newSnowball.position.x = newPos.x
    newSnowball.position.z = newPos.z
    newSnowball.name = snowball.id.id.toString
    newSnowball
  }

  def removeSnowballs(deaths: Seq[BallId]): Unit = {
    val ids = deaths.map(_.id.toString)
    Groups.threeSnowballs.children = Groups.threeSnowballs.children.filter { snowball =>
      !ids.contains(snowball.name)
    }
  }
}
