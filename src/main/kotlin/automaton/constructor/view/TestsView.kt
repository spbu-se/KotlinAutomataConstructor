package automaton.constructor.view

import automaton.constructor.controller.TestsController
import automaton.constructor.model.memory.Test
import automaton.constructor.utils.*
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
            inputEditor
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
        val addButton = Button(I18N.messages.getString("TestsFragment.Add"))
        val deleteButton = Button(I18N.messages.getString("TestsFragment.Delete"))
        val saveButton = Button(I18N.messages.getString("TestsFragment.Save"))
        val loadButton = Button(I18N.messages.getString("TestsFragment.Load"))
        minWidth = 500.0 // just for testing

        hbox {
            add(addButton)
            add(deleteButton)
            add(saveButton)
            add(loadButton)
        }
        val testsListView = listview(tests)
        button(I18N.messages.getString("TestsFragment.Run")) {
            action {
                controller.runOnTests(tests)
            }
        }

        testsListView.setCellFactory { TestCell(controller) }

        addButton.setOnAction {
            tests.add(Test(controller.openedAutomaton.memoryDescriptors.map { it.copy() }))
        }

        deleteButton.setOnAction {
            val selectedCellIndex = testsListView.selectionModel.selectedIndex
            if (selectedCellIndex != -1) {
                tests.removeAt(selectedCellIndex)
            }
        }

        saveButton.setOnAction {
            controller.saveTests(tests, this@TestsView)
        }

        loadButton.setOnAction {
            controller.openTests(this@TestsView)
        }
    }
}
