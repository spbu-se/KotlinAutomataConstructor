package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock

interface AutomatonViewContext {
    val fileController: FileController
    val layoutController: LayoutController
    fun getAutomatonView(automaton: Automaton): AutomatonView
    fun onBuildingBlockDoubleClicked(buildingBlock: BuildingBlock)
}
