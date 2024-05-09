package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.MemoryUnitDescriptorData
import automaton.constructor.model.data.serializersModule
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.addOnSuccess
import automaton.constructor.utils.runAsyncWithDialog
import automaton.constructor.view.TestAndResult
import automaton.constructor.view.TestsView
import automaton.constructor.view.TestsResultsFragment
import automaton.constructor.view.module.executor.executionLeafView
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import tornadofx.*
import java.io.File

data class Test(val input: List<MemoryUnitDescriptor>)

@Serializable
data class TestsForSerializing(val tests: List<List<MemoryUnitDescriptorData>>, val automatonType: String)

class TestsController(val openedAutomaton: Automaton) : Controller() {
    var wereTestsModified = false
    private val formatForSerializing = Json {
        serializersModule = SerializersModule {
            prettyPrint = true
            include(MemoryUnitDescriptorData.serializersModule)
        }
    }
    fun saveTests(tests: List<Test>, uiComponent: UIComponent) {
        val file = chooseFile(FileChooserMode.Save) ?: return
        saveAsync(tests, uiComponent, file)
    }
    private fun saveAsync(tests: List<Test>, uiComponent: UIComponent, file: File): Task<Unit> {
        return uiComponent.runAsyncWithDialog(
            I18N.messages.getString("TestsController.Saving"),
            daemon = false
        ) {
            val testsForSerializing = TestsForSerializing(tests.map { test -> test.input.map { it.getData() } },
                openedAutomaton::class.simpleName!!)
            file.writeText(formatForSerializing.encodeToString(testsForSerializing))
        } addOnSuccess {
            wereTestsModified = false
        }
    }
    fun createTests() {
        val testsWindow = find<TestsView>(mapOf(TestsView::controller to this))
        testsWindow.title = "Test"
        testsWindow.openWindow()
    }
    private fun chooseFile(mode: FileChooserMode): File? =
        chooseFile(
            filters = arrayOf(FileChooser.ExtensionFilter("1", "*.json")),
            mode = mode
        ).firstOrNull()
    fun openTests(uiComponent: UIComponent): List<Test> {
        val file = chooseFile(FileChooserMode.Single) ?: return listOf()
        return openAsync(uiComponent, file).get()
    }
    private fun openAsync(uiComponent: UIComponent, file: File): Task<List<Test>> {
        return uiComponent.runAsyncWithDialog(
            I18N.messages.getString("TestsController.Opening"),
            daemon = false
        ) {
            val deserializedTests =
                formatForSerializing.decodeFromString<TestsForSerializing>(file.readText())
            if (deserializedTests.automatonType != openedAutomaton::class.simpleName!!) {
                throw RuntimeException(
                    I18N.messages.getString("TestsController.UnableToOpen")
                )
            }
            deserializedTests.tests.map { test -> Test(test.map { it.createDescriptor() }) }
        } addOnSuccess {
            wereTestsModified = false
        }
    }
    fun runOnTests(tests: List<Test>) {
        val testsAndResults = mutableListOf<TestAndResult>()
        tests.forEach { test ->
            val memory = openedAutomaton.memoryDescriptors.zip(test.input).map {
                    (descriptor, content) -> descriptor.createMemoryUnit(content)
            }
            val executor = Executor(openedAutomaton)
            executor.start(memory)
            executor.runFor()
            val executionResult = when (executor.status) {
                ExecutionStatus.ACCEPTED -> I18N.messages.getString("ExecutorController.Executor.Status.Accepted")
                ExecutionStatus.REJECTED -> I18N.messages.getString("ExecutorController.Executor.Status.Rejected")
                ExecutionStatus.FROZEN -> I18N.messages.getString("ExecutorController.Executor.Status.Frozen")
                ExecutionStatus.RUNNING -> I18N.messages.getString("ExecutorController.Executor.Status.Running")
            }
            val graphic = executor.acceptedExeStates.firstOrNull()?.let { executionLeafView(it) }
            testsAndResults.add(TestAndResult(test, executionResult, graphic))
        }
        val testsResultsWindow = find<TestsResultsFragment>(mapOf(TestsResultsFragment::testsAndResults to testsAndResults))
        testsResultsWindow.title = "Tests results"
        testsResultsWindow.openWindow()
    }

    fun suggestSavingChanges(tests: List<Test>, uiComponent: UIComponent): Boolean {
        if (!wereTestsModified) return true
        val result = alert(
            Alert.AlertType.CONFIRMATION,
            I18N.messages.getString("TestsController.SuggestToSave"),
            null,
            ButtonType(I18N.messages.getString("Dialog.yes.button"), ButtonType.YES.buttonData),
            ButtonType(I18N.messages.getString("Dialog.no.button"), ButtonType.NO.buttonData),
            ButtonType(I18N.messages.getString("Dialog.cancel.button"), ButtonType.CANCEL.buttonData),
            owner = uiComponent.currentWindow,
            title = I18N.messages.getString("Dialog.confirmation")
        ).result
        if (result.buttonData == ButtonType.YES.buttonData)
            saveTests(tests, uiComponent)
        return result.buttonData != ButtonType.CANCEL.buttonData
    }
}
