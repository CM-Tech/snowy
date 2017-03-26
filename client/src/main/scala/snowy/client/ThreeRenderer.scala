package snowy.client

import minithree.THREE
import minithree.THREE.WebGLRendererParameters
import org.scalajs.dom.raw.Event
import org.scalajs.dom.{document, window}

import scala.scalajs.js.Dynamic

// TODO comment.
// TODO global singleton object that runs code is a concern. Make this an instance (class) instead?
// TODO Are you certain that the code initialization will run at the right time compared to other classes and objects?
object ThreeRenderer {
  private var width  = window.innerWidth
  private var height = window.innerHeight

  def getWidth(): Double  = width
  def getHeight(): Double = height

  val renderer = new THREE.WebGLRenderer(
    Dynamic
      .literal(antialias = false, canvas = document.getElementById("game-c"))
      .asInstanceOf[WebGLRendererParameters]
  )
  renderer.setClearColor(new THREE.Color(0xe7f1fd), 1)
  renderer.setPixelRatio(
    if (!window.devicePixelRatio.isNaN) window.devicePixelRatio else 1
  )
  renderer.setSize(getWidth(), getHeight())

  def resize(): Unit = {
    width = window.innerWidth
    height = window.innerHeight

    renderer.setSize(getWidth(), getHeight())
    renderer.setViewport(0, 0, getWidth(), getHeight())
  }

  window.addEventListener("resize", { _: Event =>
    resize()
  }, false)
}