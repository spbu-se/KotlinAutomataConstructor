package automaton.constructor.model

import automaton.constructor.AutomatonConstructorApp
import automaton.constructor.utils.I18N
import automaton.constructor.view.tests.TestAndResult
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest

class TestingTests: ApplicationTest() {
    private lateinit var app: Application
    @BeforeEach
    fun setUpClass() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        app = FxToolkit.setupApplication(AutomatonConstructorApp::class.java)
    }
    override fun start(stage: Stage) {
        stage.show()
    }

    @AfterEach
    fun afterEachTest() {
        if (::app.isInitialized) {
            FxToolkit.cleanupApplication(app)
        }
    }

    private fun openExampleAndRunTest(automaton: String, test: String): String {
        clickOn(I18N.messages.getString("OpenedAutomatonController.OK"))
        clickOn(I18N.messages.getString("MainView.File"))
        clickOn(I18N.messages.getString("MainView.Examples"))
        doubleClickOn(automaton)
        clickOn(I18N.messages.getString("MainView.Tests"))
        clickOn(I18N.messages.getString("MainView.Tests.Create"))
        clickOn(I18N.messages.getString("TestsFragment.Add"))
        clickOn(
            from(rootNode(window(I18N.messages.getString("TestsFragment.Title")).scene)
            ).lookup(".text-field").query<Node>()).write(test)
        clickOn(
            from(rootNode(window(I18N.messages.getString("TestsFragment.Title")).scene)
            ).lookup(I18N.messages.getString("TestsFragment.Run")).query<Node>())
        val resultsRootNode = rootNode(window(I18N.messages.getString("TestsResultsFragment.Title")).scene)
        clickOn(resultsRootNode)
        return from(resultsRootNode).lookup(".table-view").queryAll<TableView<TestAndResult>>().first().items.first().result
    }

    @Test
    fun runningTestOnFiniteAutomatonTest() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.evenBinaryNumbersRecognizer"), "1"
            ),
            I18N.messages.getString("ExecutorController.Executor.Status.Rejected"))
    }

    @Test
    fun runningTestOnCorrectBracketSequenceRecognizer() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.correctBracketSeqRecognizer"), "()[]{}"
            ),
            I18N.messages.getString("ExecutorController.Executor.Status.Accepted"))
    }

    @Test
    fun runningTestOnTuringMachine() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.binaryNumberAdder"), "100+111"
            ),
            I18N.messages.getString("ExecutorController.Executor.Status.Accepted"))
    }

    @Test
    fun runningTestOnEvenPalindromesRecognizer() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.evenPalindromesRecognizer"), "110011"
            ), I18N.messages.getString("ExecutorController.Executor.Status.Accepted"))
    }

    @Test
    fun runningTestOnRegisterAutomaton() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.threeZerosAndOneOne"), "110011"
            ), I18N.messages.getString("ExecutorController.Executor.Status.Rejected"))
    }

    @Test
    fun runningTestOnMealyMooreMachine() {
        assertEquals(
            openExampleAndRunTest(
                I18N.automatonExamples.getString("ExamplesFragment.zeroRemover"), "110011"
            ), I18N.messages.getString("ExecutorController.Executor.Status.Accepted"))
    }
}
