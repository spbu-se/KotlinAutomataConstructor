package automaton.constructor.controller.algorithms

import automaton.constructor.controller.algorithms.rpq.RPQAlgo
import automaton.constructor.controller.algorithms.rpq.RPQTransition
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.perform
import automaton.constructor.model.action.transition.SimplifyRegexEntirelyTransitionAction
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.factory.FiniteAutomatonFactory
import automaton.constructor.model.property.FormalRegex
import automaton.constructor.model.transformation.DeterminizeAutomatonAction
import automaton.constructor.utils.I18N
import automaton.constructor.view.algorithms.RPQAlgoExecutionView
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller
import tornadofx.action
import tornadofx.observableListOf

data class RPQResultPair(val source: AutomatonVertex, val target: AutomatonVertex) {
    override fun toString(): String = "(${source.name}, ${target.name})"
}

class RPQAlgoController(private val openedAutomaton: FiniteAutomaton) : Controller() {

    var regex: FormalRegex? = null
    val regexProperty = SimpleStringProperty()

    private val faFactory = FiniteAutomatonFactory()
    var regexAutomaton: FiniteAutomaton? = null
        private set

    var rpq: RPQAlgo? = null
        private set

    private val currentTransitions = observableListOf<RPQTransition>()
    private val allTransitions = observableListOf<RPQTransition>()
    private val resultPairs = observableListOf<RPQResultPair>()

    private var rpqView: RPQAlgoExecutionView? = null

    private val finishedProperty: BooleanProperty = SimpleBooleanProperty(false)

    init {
        regexProperty.addListener { _, _, newValue ->
            try {
                regex = if (newValue.isNullOrBlank()) null else FormalRegex.fromString(newValue)
                invalidateRunPreserveRegex()
            } catch (_: Exception) {
                regex = null
                invalidateRunPreserveRegex()
            }
        }
    }

    fun execute() {
        if (rpqView == null) {
            rpqView = find(
                RPQAlgoExecutionView::class, mapOf(
                    RPQAlgoExecutionView::controller to this,
                    RPQAlgoExecutionView::currentTransitions to currentTransitions,
                    RPQAlgoExecutionView::allTransitions to allTransitions,
                    RPQAlgoExecutionView::resultPairs to resultPairs,
                    RPQAlgoExecutionView::regexProperty to regexProperty,
                )
            ).apply {
                title = I18N.messages.getString("RPQAlgorithm.Execution.Title")
                openWindow()
                unlockRegexInput()
            }
        }
        wireButtons()
    }

    private fun wireButtons() {
        rpqView?.apply {
            nextIterationButton.action { onNextIterationPressed() }
            runToEndButton.action { runToEnd() }
            resetButton.action { resetRunPreserveRegex() }
        }
    }

    private fun onNextIterationPressed() {
        if (!prepareAlgorithmIfNeeded()) return

        val algo = rpq!!
        if (algo.isFinished()) {
            disableButtonsAfterFinish()
            return
        }

        val newEdges = algo.nextIteration()
        updateTransitionLists(newEdges)
        refreshResultPairs()

        if (algo.isFinished()) {
            disableButtonsAfterFinish()
        }
    }

    /**
     * Run until completion.
     */
    private fun runToEnd() {
        if (!prepareAlgorithmIfNeeded()) return
        val algo = rpq!!
        var totalNew = 0
        while (!algo.isFinished()) {
            val newEdges = algo.nextIteration()
            totalNew += newEdges.size
            updateTransitionLists(newEdges)
            refreshResultPairs()
        }
        disableButtonsAfterFinish()
    }

    private fun prepareAlgorithmIfNeeded(): Boolean {
        val raw = regexProperty.value
        if (raw.isNullOrBlank()) {
            return false
        }
        if (regex == null) {
            return false
        }

        if (regexAutomaton == null) {
            regexAutomaton = buildRegexAutomaton(raw)
        }

        if (rpq == null) {
            rpq = RPQAlgo(openedAutomaton, regexAutomaton!!)
            rpqView?.lockRegexInput()
        }
        return true
    }

    private fun buildRegexAutomaton(raw: String): FiniteAutomaton {
        faFactory.regex = raw
        val fa = faFactory.createAutomaton()
        fa.simplifyRegexViaAction()

        val result = try {
            fa.determinizeViaAction()
        } catch (_: Exception) {
            fa
        }

        result.undoRedoManager.reset()
        return result
    }

    private fun updateTransitionLists(newEdges: List<RPQTransition>) {
        allTransitions.forEach { it.isNew.set(false) }
        currentTransitions.setAll(newEdges)
        allTransitions.addAll(newEdges)
    }

    private fun refreshResultPairs() {
        val algo = rpq ?: return
        val pairs = algo.getResultPairs()
        val converted = pairs.map { (src, dst) -> RPQResultPair(src, dst) }
        resultPairs.setAll(converted)
    }

    private fun disableButtonsAfterFinish() {
        finishedProperty.set(true)
        rpqView?.apply {
            nextIterationButton.isDisable = true
            runToEndButton.isDisable = true
        }
    }

    fun resetRunPreserveRegex() {
        clearRunState()
        rpqView?.apply {
            unlockRegexInput()
            nextIterationButton.isDisable = false
            runToEndButton.isDisable = false
        }
    }

    private fun invalidateRunPreserveRegex() {
        clearRunState()
        rpqView?.apply {
            nextIterationButton.isDisable = false
            runToEndButton.isDisable = false
        }
    }

    private fun clearRunState() {
        rpq = null
        regexAutomaton = null
        currentTransitions.clear()
        allTransitions.clear()
        resultPairs.clear()
        finishedProperty.set(false)
    }
}

fun FiniteAutomaton.determinizeViaAction(): FiniteAutomaton {
    transformationActions.first { it is DeterminizeAutomatonAction }.perform()
    val transformation = isInputForTransformation
    transformation!!.complete()
    return transformation.resultingAutomaton as FiniteAutomaton
}

fun FiniteAutomaton.simplifyRegexViaAction() {
    val simplifyAction =
        this.transitionActions.first { it is SimplifyRegexEntirelyTransitionAction } as SimplifyRegexEntirelyTransitionAction

    val initialRegexTransition =
        this.transitions.singleOrNull() ?: error("Expected a single regex transition right after factory creation")

    if (simplifyAction.getAvailabilityFor(initialRegexTransition) == ActionAvailability.AVAILABLE) {
        simplifyAction.performOn(initialRegexTransition)
    }
}
