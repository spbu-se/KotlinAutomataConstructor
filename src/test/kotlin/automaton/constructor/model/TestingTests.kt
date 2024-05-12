package automaton.constructor.model

import automaton.constructor.AutomatonConstructorApp
import automaton.constructor.utils.I18N
import javafx.application.Application
import javafx.scene.Node
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxAssert.verifyThat
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.matcher.control.TableViewMatchers.hasTableCell

class TestingTests: ApplicationTest() {
    private lateinit var app: Application
    @BeforeEach
    fun setUpClass() {
        app = FxToolkit.setupApplication(AutomatonConstructorApp::class.java)
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
    }
    override fun start(stage: Stage) {
        stage.show()
    }

    @AfterEach
    fun afterEachTest() {
        FxToolkit.cleanupApplication(app)
    }

    private fun openExampleAndRunTest(automaton: String, test: String) {
        clickOn(I18N.messages.getString("OpenedAutomatonController.OK"))
        clickOn(I18N.messages.getString("MainView.File"))
        clickOn(I18N.messages.getString("MainView.Examples"))
        doubleClickOn(automaton)
        clickOn(I18N.messages.getString("MainView.Tests"))
        clickOn(I18N.messages.getString("MainView.Tests.Create"))
        clickOn(I18N.messages.getString("TestsFragment.Add"))
        clickOn(from(rootNode(window("Test").scene)).lookup(".text-field").query<Node>()).write(test)
        clickOn(from(rootNode(window("Test").scene)).lookup(I18N.messages.getString(
            "TestsFragment.Run")).query<Node>())
        clickOn(rootNode(window("Tests results").scene))
    }

    @Test
    fun runningTestOnFiniteAutomatonTest() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.evenBinaryNumbersRecognizer"), "1")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Rejected")))
    }

    @Test
    fun runningTestOnCorrectBracketSequenceRecognizer() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.correctBracketSeqRecognizer"), "()[]{}")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Accepted")))
    }

    @Test
    fun runningTestOnTuringMachine() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.binaryNumberAdder"), "100+111")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Accepted")))
    }

    @Test
    fun runningTestOnEvenPalindromesRecognizer() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.evenPalindromesRecognizer"), "110011")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Accepted")))
    }

    @Test
    fun runningTestOnRegisterAutomaton() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.threeZerosAndOneOne"), "110011")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Rejected")))
    }

    @Test
    fun runningTestOnMealyMooreMachine() {
        assumeFalse(
            System.getProperty("testfx.headless") == "true",
            "Tests requiring graphical UI will be skipped in headless mode"
        )
        openExampleAndRunTest(I18N.automatonExamples.getString(
            "ExamplesFragment.zeroRemover"), "110011")
        verifyThat(".table-view", hasTableCell(I18N.messages.getString(
            "ExecutorController.Executor.Status.Accepted")))
    }
}
