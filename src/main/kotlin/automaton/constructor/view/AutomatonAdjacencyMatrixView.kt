package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import javafx.scene.control.TableColumn

class AutomatonAdjacencyMatrixView(automaton: Automaton, automatonViewContext: AutomatonViewContext
): AutomatonTableView<AdjacencyMatrixTransitionView>(automaton, automatonViewContext) {
    override fun registerVertex(vertex: AutomatonVertex) {
        val vertexView = AutomatonBasicVertexView(vertex)
        controller.registerAutomatonElementView(vertexView)
        vertexToViewMap[vertex] = vertexView
        if (transitionsByVertices.none { it.source == vertex }) {
            transitionsByVertices.add(TransitionMap(vertex))
            transitionsColumns.add(TableColumn(vertex.name))
        }
        filtersCount[vertex.name] = 0
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        transitionsColumns.removeAll { it.text == vertex.name }
        vertexToViewMap.remove(vertex)
        filtersCount.remove(vertex.name)
    }

    override fun registerTransition(transition: Transition) {
        val transitionView = AdjacencyMatrixTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list + transition)
        }
    }

    override fun unregisterTransition(transition: Transition) {
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list - transition)
        }
        transitionToViewMap.remove(transition)
    }
}