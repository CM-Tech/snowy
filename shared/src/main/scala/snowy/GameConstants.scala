package snowy

import vector.Vec2d

object GameConstants {

  /** max speed of any sled in pixels per second */
  val absoluteMaxSpeed = 500

//  /** The width and height of the playfield / map */
  val oldPlayfieldSize = Vec2d(1024 * 2 * 4, 1024 * 2 * 4) // LATER remove this

  /** max seconds for a normal weight sled to complete a skid at full speed */
  val maxSkidTime = 1.6

  /** point up on the screen, towards smaller Y values */
  val downhillRotation = math.Pi

  /** Time to turn skis 180 degrees, in seconds */
  val turnTime = .75

  /** size of the tree trunk in pixels */
  val treeSize = Vec2d(10, 10)

  object Bullet {
    val averageRadius = 5

    val baseImpactFactor = 4
  }

  object Points {

    /** Minimum number of points a sled can have (also amount of points on join) */
    val minPoints = 10

    /** points earned per pixel travelled */
    val travel = .0002

    /** % of the other users points earned from killing a sled */
    val sledKill = .5

    /** % of points lost if the sled is killed */
    val sledLoss = .5
  }

  object Friction {

    /** friction from ski angled away from direction of travel in pixels / second / second */
    val maxFriction = 250.0

    /** friction from ski angled along direction of travel in pixels / second / second */
    val minFriction = 25.0

    /** higher means ski friction is mostly when skis are perpendicular to current direction */
    val brakeSteepness = .8
  }

  object Collision {

    /** space in pixels to teleport away from tree after colliding */
    val treePadding = 2

    /** health cost from colliding into a tree at maximum speed */
    val maxTreeCost = .25

    /** minimum health after a tree collision */
    val treeMinHealth = .1

    /** at speeds less than this value (in pixels/sec), the sled takes no damage from hitting a tree.  */
    val safeSpeed = 50
  }

}
