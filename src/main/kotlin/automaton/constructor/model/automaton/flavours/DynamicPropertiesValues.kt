package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.property.DynamicProperty

class DynamicPropertiesValues<T>(private val filterProps: List<DynamicProperty<T>>) {
    operator fun get(i: Int): T = filterProps[i].get()
    operator fun set(i: Int, value: T) = filterProps[i].set(value)
}
