package automaton.constructor.utils

/**
 * Indicates that annotated class/function is mostly generated or inline and hence
 * should be ignored by JaCoCo
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class IgnorableByCoverage
