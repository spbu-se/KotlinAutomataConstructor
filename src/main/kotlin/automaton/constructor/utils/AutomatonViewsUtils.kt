package automaton.constructor.utils

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.addContent
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.view.AutomatonBasicVertexView
import automaton.constructor.view.AutomatonViewContext
import javafx.geometry.Point2D
import javafx.scene.layout.Pane
import tornadofx.FileChooserMode
import tornadofx.add
import tornadofx.div
import tornadofx.fitToParentSize
import kotlin.random.Random

fun copyBuildingBlock(
    automaton: Automaton, automatonViewContext: AutomatonViewContext,
    point2D: Point2D = Point2D(
        500_000.0 + Random.nextDouble(-500.0, 500.0),
        500_000.0 + Random.nextDouble(-500.0, 500.0))
) {
    if (!automaton.allowsModificationsByUser) return
    val file = automatonViewContext.fileController.chooseFile(
        I18N.messages.getString("MainView.File.Open"),
        FileChooserMode.Single
    ) ?: return
    automatonViewContext.fileController.loadAsync(file) addOnSuccess { (type, vertices, transitions, edges) ->
        if (type != automaton.getTypeData()) tornadofx.error(
            I18N.messages.getString("AutomatonGraphController.BuildingBlockLoadingFailed"),
            I18N.messages.getString("AutomatonGraphController.IncompatibleAutomatonType"),
            owner = automatonViewContext.uiComponent.currentWindow
        )
        else {
            automaton.addBuildingBlock(position = point2D).apply {
                subAutomaton.addContent(vertices, transitions, edges)
                name = file.nameWithoutExtension
            }
        }
    }
}

fun setBuildingBlockToolTip(
    vertex: BuildingBlock, vertexView: AutomatonBasicVertexView, automatonViewContext: AutomatonViewContext,
    width: Double, height: Double
) {
    vertexView.hoverableTooltip(stopManagingOnInteraction = true) {
        Pane().apply {
            minWidth = width / 1.5
            minHeight = height / 1.5
            maxWidth = width / 1.5
            maxHeight = height / 1.5
            val subAutomatonView = automatonViewContext.getAutomatonView(vertex.subAutomaton)
            subAutomatonView.tablePrefWidth.bind(automatonViewContext.tablePrefWidthByContext / 1.4)
            subAutomatonView.tablePrefHeight.bind(automatonViewContext.tablePrefHeightByContext)
            add(subAutomatonView)
            subAutomatonView.fitToParentSize()
        }
    }
}
