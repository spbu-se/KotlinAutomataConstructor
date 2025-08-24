package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.data.RecursiveAutomatonData
import automaton.constructor.model.element.EBNFGrammar
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.element.State
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N
import javafx.collections.SetChangeListener

class RecursiveAutomaton(
    override val inputTape: InputTapeDescriptor,
    private val registerInitially: Boolean = true
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape),
    "",
    I18N.messages.getString("RecursiveAutomaton.Nondeterministic"),
    I18N.messages.getString("RecursiveAutomaton.Untitled")
), AutomatonWithInputTape {
    // Number of RecursiveAutomatonBox instances referencing this RA (excluding self-reference boxes)
    internal var referenceCount: Int = 0
        private set

    internal fun incrementReference() {
        if (referenceCount == 0) registry.add(this)
        referenceCount++
    }

    internal fun decrementReference() {
        if (referenceCount > 0) referenceCount--
        maybeUnregisterIfOrphan()
    }

    private fun maybeUnregisterIfOrphan() {
        if (referenceCount == 0 && vertices.isEmpty() && nameProperty.isBound) {
            registry.remove(this)
        }
    }

    private var cachedInitialNonterminalName: String? = null
    val initialNonterminalNameSnapshot: String?
        get() = grammar?.initialNonterminal?.value ?: cachedInitialNonterminalName

    var grammar: EBNFGrammar? = null
        set(value) {
            field = value
            value?.initialNonterminal?.value?.let { cachedInitialNonterminalName = it }
            ensureNameFromGrammar()
        }

    private fun ensureNameFromGrammar() {
        val initial = grammar?.initialNonterminal?.value ?: cachedInitialNonterminalName
        if (!initial.isNullOrBlank() && (name.isBlank() || name == untitledAdjective || name.startsWith(
                untitledAdjective,
                ignoreCase = true
            ))
        ) {
            runCatching { name = initial }
        }
    }

    init {
        if (registerInitially) registry.add(this)
        vertices.addListener(SetChangeListener { change ->
            if (change.wasRemoved() && vertices.isEmpty()) maybeUnregisterIfOrphan()
            ensureNameFromGrammar()
        })
    }

    fun shallowCloneForRecursion(): RecursiveAutomaton {
        val clone = RecursiveAutomaton(inputTape, registerInitially = false)
        val vertexMap =
            mutableMapOf<automaton.constructor.model.element.AutomatonVertex, automaton.constructor.model.element.AutomatonVertex>()
        vertices.forEach { v ->
            when (v) {
                is State -> {
                    val newState = clone.addState(v.name)
                    newState.isInitial = v.isInitial
                    newState.isFinal = v.isFinal
                    vertexMap[v] = newState
                }

                is RecursiveAutomatonBox -> {
                    val newBox = clone.addRecursiveAutomatonBox(
                        subAutomaton = v.subAutomaton,
                        name = v.name,
                        bindName = false,
                        registerSubManager = false,
                        visibleInParent = v.visibleInParent,
                        position = v.position
                    )
                    vertexMap[v] = newBox
                }

                else -> {
                    // No other vertex types are possible in Recursive Automaton
                }
            }
        }
        transitions.forEach { t ->
            val newSrc = vertexMap[t.source] ?: return@forEach
            val newTgt = vertexMap[t.target] ?: return@forEach
            val newTransition = clone.addTransition(newSrc, newTgt)
            val label = t.getProperty(inputTape.expectedChar).value
            newTransition.getProperty(clone.inputTape.expectedChar).set(label)
        }
        clone.grammar = grammar
        return clone
    }

    override fun getTypeData() = RecursiveAutomatonData(
        inputTape = inputTape.getData(),
        initialNonterminalName = grammar?.initialNonterminal?.value ?: cachedInitialNonterminalName
    )

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this),
    )

    override fun createEmptyAutomatonOfSameType() = RecursiveAutomaton(
        inputTape,
        registerInitially = false
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("RecursiveAutomaton")
        internal val registry: MutableSet<RecursiveAutomaton> = mutableSetOf()
        fun allInstances(): Set<RecursiveAutomaton> = registry.toSet()
    }
}