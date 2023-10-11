package automaton.constructor.view

import automaton.constructor.controller.Test
import automaton.constructor.controller.TestsController
import automaton.constructor.model.data.MemoryUnitDescriptorData
import automaton.constructor.model.data.serializersModule
import automaton.constructor.utils.*
import javafx.beans.property.Property
import tornadofx.*
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

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

class TestsFragment: Fragment() {
    val controller: TestsController by param()
    private val tests = mutableListOf<Test>().asObservable()
    init {
        /*someKindOfProperty.addListener(ChangeListener { _, _, _ ->
            suggest to save changes and close
        })*/
    }
    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            if (!controller.suggestSavingChanges(tests, this))
                it.consume()
        }
    }
    override val root = vbox {
        val addButton = Button("Add")
        val deleteButton = Button("Delete")
        val saveButton = Button("Save")
        val loadButton = Button("Load")
        hbox {
            add(addButton)
            add(deleteButton)
        }
        hbox {
            add(saveButton)
            add(loadButton)
        }
        val testsListView = listview(tests)
        button("Run") {
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
            controller.saveTests(tests, this@TestsFragment)
        }
        loadButton.setOnAction {
            val openedTests = controller.openTests(this@TestsFragment)
            openedTests.forEach { test -> tests.add(test) }
        }
    }
}
