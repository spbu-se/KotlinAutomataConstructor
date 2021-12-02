package automaton.constructor.controller

import automaton.constructor.model.Automaton
import automaton.constructor.model.factory.getAllAutomatonFactories
import javafx.geometry.Pos
import tornadofx.*

class OpenedAutomatonController(val view: View) {
    val openedAutomatonProperty = getAllAutomatonFactories().first().createAutomaton().toProperty()
    var openedAutomaton: Automaton by openedAutomatonProperty

    fun onNewClicked() {
        view.dialog("New automaton") {
            clear()
            stage.x = 100.0
            stage.y = 100.0
            stage.isResizable = false
            vbox(10.0) {
                label("Select automaton type")
                val listview = listview(getAllAutomatonFactories().toObservable()) {
                    prefHeight = 200.0
                }
                borderpane {
                    centerProperty().bind(listview.selectionModel.selectedItemProperty().objectBinding {
                        it?.createEditor()
                    })
                    centerProperty().onChange { stage.sizeToScene() }
                }
                hbox(10.0) {
                    alignment = Pos.CENTER_RIGHT
                    button("OK") {
                        enableWhen(listview.selectionModel.selectedItemProperty().isNotNull)
                        action {
                            listview.selectedItem?.let { openedAutomaton = it.createAutomaton() }
                            close()
                        }
                    }
                    button("Cancel") { action { close() } }
                }
            }
        }
    }
}
