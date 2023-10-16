package automaton.constructor.view

import automaton.constructor.controller.Test
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.createSettings
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.Fragment
import tornadofx.*
import tornadofx.toProperty

data class TestAndResult(val test: Test, val result: String, val graphic: SettingGroupEditor?)

class TestTableCell: TableCell<TestAndResult, Test>() {
    override fun updateItem(item: Test?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            val inputEditor = SettingGroupEditor(SettingGroup(
                I18N.messages.getString("MemoryView.InputData").toProperty(),
                item.input.createSettings()
            ))
            inputEditor.editingDisabled = true
            HBox(inputEditor)
        } else {
            null
        }
    }
}

class TestsResultsFragment: Fragment() {
    val testsAndResults: List<TestAndResult> by param()
    override val root = tableview(testsAndResults.asObservable()) {
        minWidth = 630.0 // just for testing
    }
    private val testColumn = TableColumn<TestAndResult, Test>(I18N.messages.getString(
        "TestsResultsFragment.Test"))
    private val resultColumn = TableColumn<TestAndResult, String>(I18N.messages.getString(
        "TestsResultsFragment.Result"))
    private val descriptionColumn = TableColumn<TestAndResult, SettingGroupEditor?>(I18N.messages.getString(
        "TestsResultsFragment.Description"))

    init {
        testColumn.cellValueFactory = PropertyValueFactory("test")
        testColumn.setCellFactory { TestTableCell() }
        resultColumn.cellValueFactory = PropertyValueFactory("result")
        descriptionColumn.cellValueFactory = PropertyValueFactory<TestAndResult, SettingGroupEditor?>("graphic")
        root.columns.add(testColumn)
        root.columns.add(resultColumn)
        root.columns.add(descriptionColumn)
    }
}