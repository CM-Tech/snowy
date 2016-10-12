import GameConstants.Collision.snowballCost

object SledSnowball {
  /** Intersect the sled with all potentially overlapping snowballs on the playfield.
    * If a snowball collides with the sled, remove the snowball from the playfield
    *
    * @return a damaged sled if it intersects with a snowball */
  def collide(sled:SledState, snowballs:Store[SnowballState])
      : Option[(SledState, Store[SnowballState])] = {
    val collisions =
      snowballs.items.filter{snowball =>
        snowball.ownerId != sled.id && snowballCollide(sled, snowball)
      }

    val newBalls = collisions.foldLeft(snowballs){(balls, snowball) =>
      balls.remove(snowball)
    }

    collisions.headOption.map { snowball =>
      (snowballDamaged(sled, snowball), newBalls)
    }
  }

  /** return true if the snowball intersects the sled body */
  private def snowballCollide(sled:SledState, snowball: SnowballState): Boolean = {
    val centerDistance = (sled.pos - snowball.pos).length
    val radiiSum = sled.size / 2 + snowball.size / 2
    centerDistance < radiiSum
  }

  /** return a damaged version of the sled after impacting with a snowball */
  private def snowballDamaged(sled:SledState, snowball: SnowballState): SledState = {
    val health = math.max(sled.health - snowballCost, 0)
    val stopped = sled.speed + snowball.speed * 30
    sled.copy(health = health, speed = stopped)
  }
}