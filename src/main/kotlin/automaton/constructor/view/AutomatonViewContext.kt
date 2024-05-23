package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import javafx.beans.property.SimpleDoubleProperty
import javafx.stage.Window
import tornadofx.UIComponent

interface AutomatonViewContext {
    val uiComponent: UIComponent
    val fileController: FileController
    val layoutController: LayoutController
    val tablePrefWidth: SimpleDoubleProperty
    val tablePrefHeight: SimpleDoubleProperty
    fun getAutomatonView(automaton: Automaton): AutomatonView
    fun onBuildingBlockDoubleClicked(buildingBlock: BuildingBlock)
    fun openInNewWindow(automaton: Automaton): Window?
}
