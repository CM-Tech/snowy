package snowy.draw

import minithree.THREE
import minithree.THREE.{Mesh, MeshLambertMaterialParameters}
import snowy.GameConstants

import scala.collection.mutable
import scala.scalajs.js.Dynamic

object CreateGrid {
  private val detail = 6

  // Plus one to include bottom and right borders
  val gridColumns1 = math.pow(2, detail).toInt
  val gridRows1    = math.pow(2, detail).toInt

  val gridColumns = gridColumns1 - 1
  val gridRows    = gridRows1 - 1

  def fractalizeGrid(grid: Array[Array[Double]], scale: Double): Array[Array[Double]] = {
    val newGri = Array.ofDim[Double](grid.length * 2, grid(0).length * 2)
    for {
      i <- grid.indices
      j <- grid(i).indices
    } {
      newGri(i * 2)(j * 2) = grid(i)(j)
      newGri(i * 2 + 1)(j * 2) = (grid(i)(j) + grid((i + 1) % grid.length)(j)) / 2
      newGri(i * 2)(j * 2 + 1) = (grid(i)(j) + grid(i)((j + 1) % grid(i).length)) / 2
      newGri(i * 2 + 1)(j * 2 + 1) = (newGri(i * 2 + 1)(j * 2) + newGri(i * 2)(
        j * 2 + 1
      )) / 2
    }
    val r = new scala.util.Random(10324)
    for {
      i <- newGri.indices
      j <- newGri(i).indices
    } {
      newGri(i)(j) = newGri(i)(j) + (r.nextDouble - 0.5) * scale * 2.0
    }
    newGri
  }

  val heightMap: Seq[Double] = {
    //TODO: Replace this with a fold
    var theGrid     = Array(Array(1.0))
    val heightScale = 700.0
    for (i <- 0 until detail) {
      theGrid = fractalizeGrid(theGrid, heightScale / math.pow(2, i))
    }

    val randomHeights = mutable.Seq(theGrid.flatten.map(_ - 100): _*)

    // align heights at top and bottom so the seam isn't obvious when we wrap
    val lastRow = randomHeights.view(gridColumns1 * gridRows, randomHeights.size)
    for (i <- 0 until gridColumns1) {
      randomHeights(i) = lastRow(i)
    }

    // align heights on left and right so the seam isn't obvious when we wrap
    for (i <- randomHeights.indices by gridColumns1) {
      randomHeights(i) = randomHeights(i + gridColumns)
    }

    randomHeights
  }

  /** a larger heightMap with nine copies of the base map */
  private val heightMap3x3: Seq[Double] = {

    /** @return one row, three times wider than the original row */
    def wideRow(index: Int): Seq[Double] = {
      val row        = heightMap.view(index, index + gridColumns)
      val endElement = heightMap(index + gridColumns)
      (row ++ row ++ row) :+ endElement
    }

    /** height map 3 times as wide as original */
    val rowIndices                = 0 until (gridColumns1 * gridRows) by gridColumns1
    val heightMap3x1: Seq[Double] = rowIndices.flatMap(wideRow)

    /** height map 3 times as wide and three times as tall as original */
    val endRow = wideRow(gridColumns1 * gridRows)
    (heightMap3x1 ++ heightMap3x1 ++ heightMap3x1) ++ endRow
  }

  def newGrid(): Mesh = {
    val material = new THREE.MeshLambertMaterial(
      Dynamic
        .literal(color = 0xe7f1fd, side = THREE.DoubleSide)
        .asInstanceOf[MeshLambertMaterialParameters]
    )

    val grid = new THREE.PlaneGeometry(
      GameConstants.oldPlayfieldSize.x * 3,
      GameConstants.oldPlayfieldSize.y * 3,
      gridColumns * 3,
      gridRows * 3
    )

    for (i <- grid.vertices.indices) {
      grid.vertices(i).z = heightMap3x3(i)
    }

    grid.computeFaceNormals()
    grid.computeFlatVertexNormals()

    val mesh = new THREE.Mesh(grid, material)
    mesh.rotation.x = 1.5 * math.Pi
    mesh.position.x = GameConstants.oldPlayfieldSize.x / 2
    mesh.position.z = GameConstants.oldPlayfieldSize.y / 2
    mesh
  }
}
