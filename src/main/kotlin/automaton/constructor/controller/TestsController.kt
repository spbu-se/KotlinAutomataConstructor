package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.MemoryUnitDescriptorData
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.data.serializersModule
import automaton.constructor.model.memory.Test
import automaton.constructor.model.memory.TestsForSerializing
import automaton.constructor.utils.*
import automaton.constructor.utils.addOnSuccess
import automaton.constructor.view.tests.TestAndResult
import automaton.constructor.view.tests.TestsView
import automaton.constructor.view.tests.TestsResultsFragment
import javafx.beans.property.SimpleBooleanProperty
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import tornadofx.*
import java.io.File
import java.text.MessageFormat

class TestsController(val openedAutomaton: Automaton) : Controller() {
    var wereTestsModified = false
    private val testsWindow = find<TestsView>(mapOf(TestsView::controller to this)).apply {
        this.title = I18N.messages.getString("TestsFragment.Title")
    }
    val isSelectionModeOn = SimpleBooleanProperty(false).apply {
        addListener { _, _, newValue ->
            testsWindow.deleteButton.isVisible = newValue
            testsWindow.cancelButton.isVisible = newValue
        }
    }
    val selectedTests = mutableListOf<Test>()

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
        } addOnFail {
            throw RuntimeException(
                MessageFormat.format(
                    I18N.messages.getString("TestsController.UnableToSave"),
                    file
                ), it
            )
        }
    }

    fun createTests() {
        testsWindow.openWindow()
    }

    private fun chooseFile(mode: FileChooserMode): File? =
        chooseFile(
            filters = arrayOf(FileChooser.ExtensionFilter("1", "*.json")),
            mode = mode
        ).firstOrNull()

    fun openTests(uiComponent: UIComponent) {
        val file = chooseFile(FileChooserMode.Single) ?: return
        openAsync(uiComponent, file) addOnSuccess {
            if (it == null) {
                error(I18N.messages.getString("TestsController.UnableToOpen"))
            } else {
                testsWindow.tests.addAll(it)
            }
        }
    }

    private fun openAsync(uiComponent: UIComponent, file: File): Task<List<Test>?> {
        return uiComponent.runAsyncWithDialog(
            I18N.messages.getString("TestsController.Opening"),
            daemon = false
        ) {
            val deserializedTests =
                formatForSerializing.decodeFromString<TestsForSerializing>(file.readText())
            val descriptors = deserializedTests.tests.map { test -> Test(test.map { it.createDescriptor() }) }
            if (deserializedTests.automatonType != openedAutomaton::class.simpleName!! ||
                !openedAutomaton.canUseTheseDescriptors(descriptors[0].input)) {
                null
            } else {
                descriptors
            }
        } addOnSuccess {
            wereTestsModified = false
        } addOnFail {
            throw RuntimeException(
                MessageFormat.format(
                    I18N.messages.getString("TestsController.UnableToOpen"),
                    file
                ), it
            )
        }
    }

    fun runOnTests(tests: List<Test>) {
        val testsAndResults = mutableListOf<TestAndResult>()
        tests.forEach { test ->
            val automatonCopy = openedAutomaton.getData().createAutomaton()
            val memory = automatonCopy.memoryDescriptors.zip(test.input).map {
                    (descriptor, content) -> descriptor.createMemoryUnit(content)
            }
            val executorResult = createExecutorAndRun(automatonCopy, memory) ?: return@runOnTests
            testsAndResults.add(TestAndResult(test, executorResult.executionResult, executorResult.graphic))
        }
        val testsResultsWindow = find<TestsResultsFragment>(mapOf(TestsResultsFragment::testsAndResults to testsAndResults))
        testsResultsWindow.title = I18N.messages.getString("TestsResultsFragment.Title")
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
