package automaton.constructor.utils

import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableIntegerValue
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import tornadofx.*

fun <E> Iterable<E>.countBinding(predicateFactory: (E) -> ObservableBooleanValue): ObservableIntegerValue {
    val countProperty = 0.toProperty()
    forEach {
        val predicate = predicateFactory(it)
        if (predicate.value) countProperty.value++
        predicate.addListener { _, _, newValue ->
            if (newValue) countProperty.value++
            else countProperty.value--
        }
    }
    return countProperty
}

fun <E> ObservableSet<E>.filteredSet(predicate: (E) -> ObservableBooleanValue): ObservableSet<E> {
    val filteredSet = observableSetOf<E>()
    val predicates = mutableMapOf<E, ObservableBooleanValue>()
    fun onElementAdded(elm: E) {
        val shouldBeIncludedProperty = predicate(elm)
        predicates[elm] = shouldBeIncludedProperty
        shouldBeIncludedProperty.onChange {
            if (it) filteredSet.add(elm)
            else filteredSet.remove(elm)
        }
        if (shouldBeIncludedProperty.value) filteredSet.add(elm)
    }
    forEach { onElementAdded(it) }
    addListener(SetChangeListener {
        if (it.wasAdded()) onElementAdded(it.elementAdded)
        if (it.wasRemoved() && predicates.remove(it.elementRemoved)!!.value) filteredSet.remove(it.elementRemoved)
    })
    return filteredSet
}
