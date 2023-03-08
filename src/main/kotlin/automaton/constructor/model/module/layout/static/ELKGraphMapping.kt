package automaton.constructor.model.module.layout.static

import automaton.constructor.model.element.AutomatonEdge
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import org.eclipse.elk.graph.ElkEdge
import org.eclipse.elk.graph.ElkLabel
import org.eclipse.elk.graph.ElkNode

class ELKGraphMapping(
    val elkGraph: ElkNode,
    val elkNodeToVertex: Map<ElkNode, AutomatonVertex>,
    val elkLabelToTransition: Map<ElkLabel, Transition>,
    val elkEdgeToEdge: Map<ElkEdge, AutomatonEdge>
)
