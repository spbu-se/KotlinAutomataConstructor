package automaton.constructor.utils

import javafx.beans.binding.IntegerBinding
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import tornadofx.*

fun <E> Iterable<E>.countBinding(predicateFactory: (E) -> ObservableBooleanValue): IntegerBinding {
    val predicates = map(predicateFactory).toTypedArray()
    return integerBinding(predicates, *predicates) { predicates.count { it.value } }
}

fun <E> ObservableSet<E>.filteredSet(predicateFactory: (E) -> ObservableBooleanValue): ObservableSet<E> {
    val filteredSet = observableSetOf<E>()
    val listeners = mutableMapOf<E, Pair<ObservableBooleanValue, ChangeListener<Boolean>>>()
    fun onElementAdded(elm: E) {
        val observablePredicate = predicateFactory(elm)
        val listener = ChangeListener<Boolean> { _, _, newValue ->
            if (newValue) filteredSet.add(elm)
            else filteredSet.remove(elm)
        }
        observablePredicate.addListener(listener)
        listeners[elm] = observablePredicate to listener
        if (observablePredicate.value) filteredSet.add(elm)
    }

    fun onElementRemoved(elm: E) {
        val (observablePredicate, listener) = listeners.remove(elm)!!
        observablePredicate.removeListener(listener)
        if (observablePredicate.value) filteredSet.remove(elm)
    }

    forEach { onElementAdded(it) }
    addListener(SetChangeListener {
        if (it.wasAdded()) onElementAdded(it.elementAdded)
        if (it.wasRemoved()) onElementRemoved(it.elementRemoved)
    })
    return filteredSet
}
