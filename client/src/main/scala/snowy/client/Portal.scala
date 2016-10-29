package snowy.client

import snowy.playfield._
import vector.Vec2d

class Portal(portalRect: Rect) {
  var sleds = Seq[Sled]()
  var trees = Seq[Tree]()
  var snowballs = Seq[Snowball]()
  var tracks = Seq[Seq[Track]]()
  var scale = 0.0

  def apply(tsleds: Set[Sled], tsnowballs: Set[Snowball], ttrees: Set[Tree], ttracks: Seq[Seq[Track]]): Portal = {
    sleds = tsleds.toSeq
    snowballs = tsnowballs.toSeq
    trees = ttrees.toSeq
    tracks = ttracks
    this
  }


  // TODO comment
  private def wrapInPlayfield(pos: Vec2d, padding: Vec2d, wrapRange: Vec2d): Option[Vec2d] = {

    /** TODO take min, max, wrapSize instead of pad? */
    def wrap(value: Double, max: Double, pad: Double, wrapSize: Double): Option[Double] = {
      val wrapped =
        if (value > pad + max) value - wrapSize
        else if (value < -pad) value + wrapSize
        else value

      if (wrapped > -pad && wrapped < pad + max)
        Some(wrapped)
      else {
        None
      }
    }
    for {
      x <- wrap(pos.x, portalRect.size.x, padding.x, wrapRange.x)
      y <- wrap(pos.y, portalRect.size.y, padding.y, wrapRange.y)
    } yield {
      Vec2d(x, y)
    }
  }

  def convertToScreen(screenSize: Vec2d, border: Vec2d): Portal = {
    val scaleX = screenSize.x / portalRect.size.x
    val scaleY = screenSize.y / portalRect.size.y
    scale = math.max(scaleX, scaleY)

    val treeBBox = Vec2d(100, 200)

    val portalToScreenOffset = {
      val portalScaled = portalRect.size * scale
      (screenSize - portalScaled) / 2
    }


    /** @param playfieldObject a sled snowball or tree
      * @return playfieldObject translated, wrapped, and filtered */
    def transformToScreen[A <: PlayfieldObject](playfieldObject: A): Option[playfieldObject.MyType] = {
      val portalPos = playfieldObject.pos - portalRect.pos

      for {
        portalPosition <- wrapInPlayfield(portalPos, treeBBox, border)
      } yield {
        val newPos = portalPosition * scale + portalToScreenOffset
        playfieldObject.updatePos(newPos)
      }
    }

    trees = trees.flatMap(transformToScreen(_))
    sleds = sleds.flatMap(transformToScreen(_))
    snowballs = snowballs.flatMap(transformToScreen(_))
    tracks = tracks.map(_.flatMap(transformToScreen(_)))

    this
  }
}
