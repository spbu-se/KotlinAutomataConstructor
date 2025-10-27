package automaton.constructor.model.automaton.recursive

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.AbstractAutomaton
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.data.RecursiveAutomatonData
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.touchesRecursiveBox
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D

class RecursiveAutomaton(
    override val inputTape: InputTapeDescriptor, private val registerInitially: Boolean = true
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
                untitledAdjective, ignoreCase = true
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
        val vertexMap = mutableMapOf<AutomatonVertex, AutomatonVertex>()
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
        inputTape, registerInitially = false
    )

    /**
     * @param subAutomaton the automaton to wrap; defaults to this
     * @param name optional explicit name; if null, a unique "R"-prefixed name is generated
     * @param position initial position of the box in the graph
     * @param bindName whether to bind `subAutomaton.nameProperty` to the box's name (only if subAutomaton !== this)
     * @param registerSubManager whether to register the sub-automaton's UndoRedoManager as a sub-manager of this automaton (only if subAutomaton !== this).
     * Needed for the parent to know that the sub-automaton was modified
     * @return the created [RecursiveAutomatonBox]
     */
    fun addRecursiveAutomatonBox(
        subAutomaton: Automaton = this,
        name: String? = null,
        position: Point2D = Point2D.ZERO,
        bindName: Boolean = true,
        registerSubManager: Boolean = true,
    ): RecursiveAutomatonBox {
        val box = RecursiveAutomatonBox(
            memoryDescriptors,
            subAutomaton,
            name ?: (RECURSIVE_BOX_NAME_PREFIX + nextRecursiveBoxSuffix()),
            position,
            registerSubManager = registerSubManager
        )
        if (bindName && subAutomaton !== this) {
            if (subAutomaton.nameProperty.isBound) subAutomaton.nameProperty.unbind()
            subAutomaton.nameProperty.bind(box.nameProperty)
        } else if (subAutomaton !== this && subAutomaton.name.startsWith(
                (subAutomaton as? AbstractAutomaton)?.untitledAdjective ?: "Untitled"
            )
        ) {
            if (!subAutomaton.nameProperty.isBound) subAutomaton.name = box.name
        }
        addVertex(box)
        return box
    }

    private fun nextRecursiveBoxSuffix(): Int = nextVertexSuffix(GENERATED_RECURSIVE_BOX_NAME_REGEX)

    override fun onVertexAddition(vertex: AutomatonVertex) {
        if (vertex is RecursiveAutomatonBox && vertex.subAutomaton !== this && vertex.registerSubManager) undoRedoManager.registerSubManager(
            vertex.subAutomaton.undoRedoManager
        )
        if (vertex is RecursiveAutomatonBox && vertex.subAutomaton !== this) (vertex.subAutomaton as? RecursiveAutomaton)?.incrementReference()
    }

    override fun onVertexRemoval(vertex: AutomatonVertex) {
        if (vertex is RecursiveAutomatonBox && vertex.subAutomaton !== this && vertex.registerSubManager) undoRedoManager.unregisterSubManager(
            vertex.subAutomaton.undoRedoManager
        )
        if (vertex is RecursiveAutomatonBox && vertex.subAutomaton !== this) (vertex.subAutomaton as? RecursiveAutomaton)?.decrementReference()
    }

    override fun validateTransitionEndpoints(source: AutomatonVertex, target: AutomatonVertex) {
        if (source is RecursiveAutomatonBox && target is RecursiveAutomatonBox) {
            throw IllegalArgumentException(I18N.messages.getString("RecursiveAutomaton.TransitionBetweenBoxesHint"))
        }
    }

    // Forces the transition label to have epsilon value since transitions to and from `RecursiveAutomatonBox` are not allowed to be anything other that epsilon
    // This is needed in order to form "non-terminal" transitions
    override fun afterTransitionCreated(transition: Transition) {
        if (!transition.touchesRecursiveBox()) return
        transition[inputTape.expectedChar] = EPSILON_VALUE
        val expectedProp = transition.getProperty(inputTape.expectedChar)
        expectedProp.addListener { _, _, newValue ->
            if (newValue != EPSILON_VALUE) expectedProp.value = EPSILON_VALUE
        }
    }

    override fun clearNestedAutomaton(vertex: AutomatonVertex, visited: MutableSet<Automaton>) {
        if (vertex is RecursiveAutomatonBox) {
            val sub = vertex.subAutomaton
            if (sub !== this) (sub as? AbstractAutomaton)?.clearExecutionStates()
        }
    }

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("RecursiveAutomaton")
        internal val registry: MutableSet<RecursiveAutomaton> = mutableSetOf()
        fun allInstances(): Set<RecursiveAutomaton> = registry.toSet()
        private const val RECURSIVE_BOX_NAME_PREFIX = "R"
        private val GENERATED_RECURSIVE_BOX_NAME_REGEX = Regex("${RECURSIVE_BOX_NAME_PREFIX}(\\d+)")

    }
}