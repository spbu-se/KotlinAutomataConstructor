package automaton.constructor.view

import automaton.constructor.controller.TestsController
import automaton.constructor.model.memory.Test
import automaton.constructor.utils.*
import javafx.geometry.Insets
import tornadofx.*
import javafx.scene.control.Button
import javafx.scene.control.ListCell

class TestCell(val controller: TestsController): ListCell<Test>() {
    private var currentMemoryDescriptors = controller.openedAutomaton.memoryDescriptors.map { it.copy() }
    private var inputEditor = SettingGroupEditor(SettingGroup(
        I18N.messages.getString("MemoryView.InputData").toProperty(),
        currentMemoryDescriptors.createSettings()
    ))

    override fun updateItem(item: Test?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            currentMemoryDescriptors = item.input
            inputEditor = SettingGroupEditor(SettingGroup(
                I18N.messages.getString("MemoryView.InputData").toProperty(),
                currentMemoryDescriptors.createSettings()
            ))
            controller.wereTestsModified = true
            borderpane {
                left = checkbox().apply {
                    action {
                        if (isSelected) {
                            controller.selectedTests.add(item)
                        } else {
                            controller.selectedTests.remove(item)
                        }
                    }
                    padding = Insets(3.0, 8.0, 0.0, 0.0)
                }
                center = inputEditor
            }
        } else {
            null
        }
    }
}

class TestsView: View() {
    val controller: TestsController by param()
    val tests = mutableListOf<Test>().asObservable()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            if (!controller.suggestSavingChanges(tests, this))
                it.consume()
            else
                tests.clear()
        }
    }

    override val root = vbox {
        minWidth = 500.0

        hbox(5) {
            button(I18N.messages.getString("TestsFragment.Add")).action {
                tests.add(Test(controller.openedAutomaton.memoryDescriptors.map { it.copy() }))
            }
            button(I18N.messages.getString("TestsFragment.Delete")).action {
                tests.removeAll(controller.selectedTests)
                controller.selectedTests.clear()
            }
            button(I18N.messages.getString("TestsFragment.Save")).action {
                controller.saveTests(tests, this@TestsView)
            }
            button(I18N.messages.getString("TestsFragment.Load")).action {
                controller.openTests(this@TestsView)
            }
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
        val testsListView = listview(tests)
        hbox {
            button(I18N.messages.getString("TestsFragment.Run")) {
                action {
                    controller.runOnTests(tests)
                }
            }
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }

        testsListView.setCellFactory { TestCell(controller) }
    }
}
