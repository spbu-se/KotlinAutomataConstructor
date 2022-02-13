package automaton.constructor.model.memory

import automaton.constructor.model.property.EPSILON_VALUE

/**
 * A status of the [MemoryUnit] that is used to determine whether the execution state of the automaton
 * should accept or reject the input or continue working
 */
enum class MemoryUnitStatus(
    /**
     * No more data can be read via [MemoryUnit.getCurrentFilterValues] from the [MemoryUnit] that has
     * status with [noMoreDataAvailable] flag set to `true`
     *
     * That means that transition can only be possible if it has [EPSILON_VALUE] in all filters corresponding
     * to such [MemoryUnit]
     */
    val noMoreDataAvailable: Boolean
) {
    /**
     * If any memory unit has this status then the execution state of the automaton cannot accept the input
     * at the moment and must either reject it or continue working
     */
    NOT_READY_TO_ACCEPT(false),

    /**
     * All memory units with this status are ignored when deciding whether the execution state of the automaton
     * should accept or reject the input or continue working.
     */
    READY_TO_ACCEPT(false),

    /**
     * Equivalent of [READY_TO_ACCEPT] with [noMoreDataAvailable] flag set to `true`
     */
    REQUIRES_TERMINATION(true),

    /**
     * Equivalent of [REQUIRES_TERMINATION] that also makes current state to always be effectively final
     *
     * That means if any memory unit has this status then input can be accepted even if current state is
     * not final thou it's still required to not have any memory units with [NOT_READY_TO_ACCEPT] status
     */
    REQUIRES_ACCEPTANCE(true)
}
