package automaton.constructor.utils

import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent

fun Node.localToAncestor(ancestor: Parent, point: ObservableValue<Point2D>): ObservableValue<Point2D> {
    val nodes = generateSequence(this) { it.parent }.takeWhile { it !== ancestor }.toList()
    return point.nonNullObjectBinding(*nodes.map { it.localToParentTransformProperty() }.toTypedArray()) { p ->
        nodes.fold(p) { acc, node -> node.localToParentTransform.transform(acc) }
    }
}
