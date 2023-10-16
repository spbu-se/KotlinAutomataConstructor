package automaton.constructor.model

import automaton.constructor.AutomatonConstructorApp
import javafx.application.Application
import javafx.scene.Node
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
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
    }
    override fun start(stage: Stage) {
        stage.show()
    }
    @AfterEach
    fun afterEachTest() {
        FxToolkit.cleanupApplication(app)
    }

    private fun openExampleAndRunTest(automaton: String, test: String) {
        clickOn("OK")
        clickOn("File")
        clickOn("Examples")
        doubleClickOn(automaton)
        clickOn("Tests")
        clickOn("Create a set of tests")
        clickOn("Add")
        clickOn(from(rootNode(window("Test").scene)).lookup(".text-field").query<Node>()).write(test)
        clickOn(from(rootNode(window("Test").scene)).lookup("Run").query<Node>())
    }

    @Test
    fun runningTestOnFiniteAutomatonTest() {
        openExampleAndRunTest("Even binary numbers recognizer", "1")
        verifyThat(".table-view", hasTableCell("Input rejected"))
    }

    @Test
    fun runningTestOnCorrectBracketSequenceRecognizer() {
        openExampleAndRunTest("Correct bracket sequence recognizer", "()[]{}")
        verifyThat(".table-view", hasTableCell("Input accepted"))
    }

    @Test
    fun runningTestOnTuringMachine() {
        openExampleAndRunTest("Binary number adder", "100+111")
        verifyThat(".table-view", hasTableCell("Input accepted"))
    }

    @Test
    fun runningTestOnEvenPalindromesRecognizer() {
        openExampleAndRunTest("Even palindromes recognizer", "110011")
        verifyThat(".table-view", hasTableCell("Input accepted"))
    }

    @Test
    fun runningTestOnRegisterAutomaton() {
        openExampleAndRunTest("Recognizer of numbers consisting of 3 zeros and 1 one", "110011")
        verifyThat(".table-view", hasTableCell("Input rejected"))
    }

    @Test
    fun runningTestOnMealyMooreMachine() {
        openExampleAndRunTest("Remover zeros from binary numbers", "110011")
        verifyThat(".table-view", hasTableCell("Input accepted"))
    }
}
