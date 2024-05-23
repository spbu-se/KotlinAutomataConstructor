package automaton.constructor.view.algorithms

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.factory.FiniteAutomatonFactory
import automaton.constructor.utils.I18N
import automaton.constructor.view.AutomatonView
import automaton.constructor.view.AutomatonViewContext
import javafx.beans.property.SimpleDoubleProperty
import javafx.stage.Window
import tornadofx.*

class HellingsAlgoGraphView: Fragment(), AutomatonViewContext {
    val hellingsAlgoController: HellingsAlgoController by param()
    override val uiComponent = this
    override val fileController: FileController by param()
    override val layoutController: LayoutController by param()
    override val tablePrefWidth = SimpleDoubleProperty()
    override val tablePrefHeight = SimpleDoubleProperty()
    override val root = vbox {
        val graph = FiniteAutomatonFactory().createAutomaton()
        val automatonView = AutomatonView(graph, this@HellingsAlgoGraphView)
        add(automatonView)
        automatonView.fitToParentSize()
        button(I18N.messages.getString("HellingsAlgorithm.Graph.Run")).action {
            hellingsAlgoController.execute(graph)
            close()
        }
        minWidth = 1000.0
        minHeight = 500.0
    }
    override fun getAutomatonView(automaton: Automaton): AutomatonView {
        TODO()
    }
    override fun onBuildingBlockDoubleClicked(buildingBlock: BuildingBlock) {

    }
    override fun openInNewWindow(automaton: Automaton): Window? {
        TODO()
    }
}