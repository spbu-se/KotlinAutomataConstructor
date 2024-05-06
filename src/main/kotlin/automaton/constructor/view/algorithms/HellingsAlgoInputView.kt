package automaton.constructor.view.algorithms

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.factory.FiniteAutomatonFactory
import automaton.constructor.view.AutomatonView
import automaton.constructor.view.AutomatonViewContext
import javafx.stage.Window
import tornadofx.*

class HellingsAlgoInputView: View(), AutomatonViewContext {
    val hellingsAlgoController: HellingsAlgoController by param()
    override val uiComponent = this
    override val fileController: FileController by param()
    override val layoutController: LayoutController by param()
    override val root = vbox {
        val graph = FiniteAutomatonFactory().createAutomaton()
        val automatonView = AutomatonView(graph, this@HellingsAlgoInputView)
        add(automatonView)
        automatonView.fitToParentSize()
        button("Run").action {
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