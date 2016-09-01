import socketserve.ConnectionId

/** Moving objects in each game time slice */
trait GameMotion {
  self: GameControl =>

  /** update sleds and snowballs speeds and positions */
  protected def moveStuff(deltaSeconds: Double): Unit = {
    applyGravity(deltaSeconds)
    skidToRotate(deltaSeconds)
    moveObjects(deltaSeconds)
    checkCollisions()
  }

  /** Increase the speed of sleds due to gravity */
  private def applyGravity(deltaSeconds: Double): Unit = {
    val gravity = 100.0
    val gravityFactor = gravity * deltaSeconds
    mapSleds { sled =>
      val delta = Vec2d(
        x = 0,
        y = math.cos(sled.rotation) * gravityFactor
      )
      val newSpeed = (sled.speed + delta).min(maxSpeed)
      sled.copy(speed = newSpeed)
    }
  }

  /** Adjust the speed based on the current rotation */
  private def skidToRotate(deltaSeconds: Double): Unit = {
    val skidSpeed = .75 // seconds to complete a skid
    val skidFactor = math.min(1.0, deltaSeconds * skidSpeed)
    mapSleds { sled =>
      val speed = sled.speed.length
      val current = sled.speed / speed
      val target = Vec2d(
        x = math.sin(sled.rotation),
        y = math.cos(sled.rotation)
      )
      val skidVector = current + ((target - current) * skidFactor)
      val newSpeed = skidVector * speed
      sled.copy(speed = newSpeed)
    }
  }

  /** Run a function that replaces each sled */
  private def mapSleds(fn: SledState => SledState): Unit = {
    sleds = sleds.map { case (id, sled) =>
      id -> fn(sled)
    }
  }

  private def slowFriction(sled: SledState): SledState = ???

  /** Constrain a value between 0 and a max value.
    * values past one border of the range are wrapped to the other side
    *
    * @return the wrapped value */
  private def wrapBorder(value: Double, max: Double): Double = {
    if (value > max * 2.0)
      max
    else if (value > max)
      max - value
    else if (value < max * -2.0)
      0
    else if (value < 0)
      max - value
    else
      value
  }

  /** constrain a position to be within the playfield */
  private def wrapInPlayfield(pos: Vec2d): Vec2d = {
    Vec2d(
      wrapBorder(pos.x, playField.width),
      wrapBorder(pos.y, playField.height)
    )
  }

  /** move movable objects to their new location for this time period */
  private def moveObjects(deltaSeconds: Double): Unit = {
    mapSleds { sled =>
      val moved = sled.pos + (sled.speed * deltaSeconds)
      val wrapped = wrapInPlayfield(moved)
      sled.copy(pos = wrapped)
    }
  }

  private def checkCollisions(): Unit = {
    // TODO
  }

}