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

    init {
        controller.isSelectionModeOn.addListener { _, _, newValue ->
            if (item != null) {
                graphic = if (newValue) {
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
                    inputEditor
                }
            }
        }
    }

    override fun updateItem(item: Test?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            currentMemoryDescriptors = item.input
            inputEditor = SettingGroupEditor(SettingGroup(
                I18N.messages.getString("MemoryView.InputData").toProperty(),
                currentMemoryDescriptors.createSettings()
            ))
            controller.wereTestsModified = true
            inputEditor
        } else {
            null
        }
    }
}

class TestsView: View() {
    val controller: TestsController by param()
    val tests = mutableListOf<Test>().asObservable()
    val deleteButton = Button(I18N.messages.getString("TestsFragment.Delete")).apply { isVisible = false }
    val cancelButton = Button(I18N.messages.getString("TestsFragment.Cancel")).apply { isVisible = false }

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
            button(I18N.messages.getString("TestsFragment.Select")).action {
                controller.isSelectionModeOn.set(true)
            }
            button(I18N.messages.getString("TestsFragment.Save")).action {
                controller.saveTests(tests, this@TestsView)
            }
            button(I18N.messages.getString("TestsFragment.Load")).action {
                controller.openTests(this@TestsView)
            }
            add(deleteButton)
            add(cancelButton)
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

        deleteButton.setOnAction {
            tests.removeAll(controller.selectedTests)
            controller.selectedTests.clear()
            controller.isSelectionModeOn.set(false)
        }

        cancelButton.setOnAction {
            controller.selectedTests.clear()
            controller.isSelectionModeOn.set(false)
        }
    }
}
