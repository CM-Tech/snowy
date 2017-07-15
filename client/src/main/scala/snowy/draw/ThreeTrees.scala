package snowy.draw

import minithree.THREE
import minithree.THREE.{Object3D, Vector3}
import snowy.client.{CreateTree, DrawPlayfield, UpdateGroup}
import snowy.playfield.Tree

object ThreeTrees {
  val treeGroup = new UpdateGroup[Tree](new THREE.Object3D())
  def updateThreeTrees(trees: Set[Tree], myPos: Vector3): Unit = {
    trees.foreach { tree1 =>
      treeGroup.map.get(tree1.id) match {
        case Some(tree) => DrawPlayfield.playfieldWrap(tree, tree1.position, myPos)
        case None       => treeGroup.add(createTree(tree1, myPos))
      }
    }
  }

  def createTree(tree: Tree, myPos: Vector3): Object3D = {
    val newTree: Object3D = CreateTree.randomTree()

    DrawPlayfield.setThreePosition(newTree, tree.position, myPos, 0)

    newTree.name = tree.id.id.toString
    newTree
  }
}
