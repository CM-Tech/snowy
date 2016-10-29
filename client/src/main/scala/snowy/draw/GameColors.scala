package snowy.draw

case class Color(r: Int, g: Int, b: Int) {
  override def toString = s"rgb($r, $g, $b)"

  def *(p: Double) = Color((r * p).toInt, (g * p).toInt, (b * p).toInt)
}

object GameColors {
  val lineColors = Color(100, 100, 100)
  val clearColor = Color(255, 255, 255)

  object Tree {
    val leaves = Color(94, 153, 105).toString
    val trunk = Color(56, 85, 58).toString
  }

  object Sled {
    val bodyGreen = Color(120, 201, 44).toString
    val bodyRed = "rgb(241, 78, 84)"
  }

  val snowball = Color(208, 242, 237).toString
}
