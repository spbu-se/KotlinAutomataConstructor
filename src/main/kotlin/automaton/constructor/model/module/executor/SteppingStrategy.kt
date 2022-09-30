package automaton.constructor.model.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.automaton.allowsStepByClosure
import automaton.constructor.model.element.Transition
import automaton.constructor.model.module.executor.ExecutionStatus.ACCEPTED
import automaton.constructor.utils.I18N

interface SteppingStrategy {
    val name: String
    fun step(automaton: Automaton, exeState: ExecutionState)
    fun isAvailableFor(automaton: Automaton): Boolean
}

abstract class AbstractSteppingStrategy(override val name: String) : SteppingStrategy {
    open fun getClosure(automaton: Automaton, exeState: ExecutionState): Collection<ExecutionState> =
        setOf(exeState)

    open fun getPossibleTransitions(
        automaton: Automaton,
        simpleExeState: SimpleExecutionState
    ): Collection<Transition> = automaton.getPossibleTransitions(simpleExeState.vertex, simpleExeState.memory)

    open fun preStepForSuperState(superExeState: SuperExecutionState) = Unit
    open fun postStepForSuperState(superExeState: SuperExecutionState) {
        val subExecutor = superExeState.subExecutor
        if (!subExecutor.started) subExecutor.start(superExeState.memory)
        else subExecutor.step(steppingStrategy = this)
    }

    override fun step(automaton: Automaton, exeState: ExecutionState) {
        exeState.justAcceptedOrRejected = false
        if (!exeState.requiresProcessing) return
        getClosure(automaton, exeState).forEach { curExeState ->
            if (curExeState.justAcceptedOrRejected) return@forEach
            when (curExeState) {
                is SuperExecutionState -> {
                    preStepForSuperState(curExeState)
                    curExeState.unhandledAcceptedStates.forEach { acceptedState ->
                        automaton.getPossibleTransitions(curExeState.buildingBlock, acceptedState.memory)
                            .forEach { transition ->
                                curExeState.takeTransition(transition, memory = acceptedState.memory)
                            }
                    }
                    curExeState.unhandledAcceptedStates.clear()
                    postStepForSuperState(curExeState)
                    if (curExeState.children.isEmpty() && !curExeState.canHaveMoreChildren && curExeState.status != ACCEPTED)
                        curExeState.fail()
                }
                is SimpleExecutionState -> {
                    getPossibleTransitions(automaton, curExeState).forEach { transition ->
                        curExeState.takeTransition(transition)
                    }
                    if (curExeState.children.isEmpty()) curExeState.fail()
                }
            }
        }
    }
}

abstract class SimpleStepStrategy(name: String) : AbstractSteppingStrategy(name) {
    override fun preStepForSuperState(superExeState: SuperExecutionState) {
        val subExecutor = superExeState.subExecutor
        if (!subExecutor.started) {
            subExecutor.start(superExeState.memory)
            subExecutor.runFor(terminationCondition = { it.finished })
        }
    }
}

object StepOverStrategy : SimpleStepStrategy(I18N.messages.getString("SteppingStrategy.StepOver")) {
    override fun isAvailableFor(automaton: Automaton) = automaton.allowsBuildingBlocks
}

object StepIntoStrategy : AbstractSteppingStrategy(I18N.messages.getString("SteppingStrategy.StepInto")) {
    override fun isAvailableFor(automaton: Automaton) = automaton.allowsBuildingBlocks
}

object StepByStateStrategy : SimpleStepStrategy(I18N.messages.getString("SteppingStrategy.StepByState")) {
    override fun isAvailableFor(automaton: Automaton) = !automaton.allowsBuildingBlocks
}

object StepByClosureStrategy : AbstractSteppingStrategy(I18N.messages.getString("SteppingStrategy.StepByClosure")) {
    override fun getClosure(automaton: Automaton, exeState: ExecutionState): Collection<ExecutionState> {
        val closure = mutableMapOf(exeState.vertex to exeState)
        val unhandledStates = mutableListOf(exeState)
        while (unhandledStates.isNotEmpty()) {
            val curState = unhandledStates.removeLast()
            automaton.getPureTransitions(curState.vertex).forEach { pureTransition ->
                if (!closure.containsKey(pureTransition.target)) {
                    val nextState = curState.takeTransition(pureTransition)
                    closure[pureTransition.target] = nextState
                    if (nextState is SimpleExecutionState && nextState.status == ExecutionStatus.RUNNING)
                        unhandledStates.add(nextState)
                }
            }
        }
        return closure.values
    }

    override fun getPossibleTransitions(
        automaton: Automaton,
        simpleExeState: SimpleExecutionState
    ) = super.getPossibleTransitions(automaton, simpleExeState).filter { !it.isPure() }

    override fun isAvailableFor(automaton: Automaton) = automaton.allowsStepByClosure
}

val STEPPING_STRATEGIES = listOf(StepOverStrategy, StepIntoStrategy, StepByStateStrategy, StepByClosureStrategy)
