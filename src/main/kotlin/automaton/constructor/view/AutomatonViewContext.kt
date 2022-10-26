package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock

interface AutomatonViewContext {
    val fileController: FileController
    fun getAutomatonView(automaton: Automaton): AutomatonTabView
    fun onBuildingBlockDoubleClicked(buildingBlock: BuildingBlock)
}
