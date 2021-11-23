package automaton.constructor.utils

import javafx.beans.binding.Binding
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.shape.Line
import tornadofx.*

class Arrow(
    val line: Line,
    val length: Double,
    val width: Double
) : Group() {
    val normalizedVectorBinding: Binding<Point2D> = nonNullObjectBinding(
        line.startXProperty(), line.startYProperty(),
        line.endXProperty(), line.endYProperty()
    ) {
        Vector2D(line.startX - line.endX, line.startY - line.endY).normalize()
    }
    val normalizedVector: Vector2D by normalizedVectorBinding

    val arrowPart1 = line {
        fillProperty().bind(line.fillProperty())
        startXProperty().bind(doubleBinding(line.endXProperty(), normalizedVectorBinding) {
            value + normalizedVector.x * length - normalizedVector.y * width
        })
        startYProperty().bind(doubleBinding(line.endYProperty(), normalizedVectorBinding) {
            value + normalizedVector.y * length + normalizedVector.x * width
        })
        endXProperty().bind(line.endXProperty())
        endYProperty().bind(line.endYProperty())
    }
    val arrowPart2 = line {
        fillProperty().bind(line.fillProperty())
        startXProperty().bind(doubleBinding(line.endXProperty(), normalizedVectorBinding) {
            value + normalizedVector.x * length + normalizedVector.y * width
        })
        startYProperty().bind(doubleBinding(line.endYProperty(), normalizedVectorBinding) {
            value + normalizedVector.y * length - normalizedVector.x * width
        })
        endXProperty().bind(line.endXProperty())
        endYProperty().bind(line.endYProperty())
    }

    init {
        add(line)
    }
}
