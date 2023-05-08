package automaton.constructor.model.module.layout.static

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER
import automaton.constructor.model.element.AutomatonEdge
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.text.Font
import org.eclipse.elk.core.options.CoreOptions
import org.eclipse.elk.core.options.Direction
import org.eclipse.elk.core.options.EdgeRouting
import org.eclipse.elk.graph.ElkLabel
import org.eclipse.elk.graph.ElkNode
import org.eclipse.elk.graph.util.ElkGraphUtil
import tornadofx.plus
import tornadofx.times
import kotlin.collections.set

interface StaticLayout {
    val name: String
    val requiresGraphviz: Boolean
    fun configureLayout(elkGraph: ElkNode)
}

val STATIC_LAYOUTS = listOf(ELKLayeredLayout, DOTLayout)

private const val LAYOUT_SCALE = 5.0
private const val TRANSITION_FONT_SIZE = 7

fun Automaton.toElkGraphMapping(transitionLayoutBounds: Map<Transition, Bounds>): ELKGraphMapping {
    val elkGraph = ElkGraphUtil.createGraph()
    val vertexToElkNode = vertices.associateWith {
        ElkGraphUtil.createNode(elkGraph).apply {
            width = (AutomatonVertex.RADIUS * 2) / LAYOUT_SCALE
            height = (AutomatonVertex.RADIUS * 2) / LAYOUT_SCALE
            x = (it.position.x - AutomatonVertex.RADIUS - GRAPH_PANE_CENTER.x) / LAYOUT_SCALE
            y = (it.position.y - AutomatonVertex.RADIUS - GRAPH_PANE_CENTER.y) / LAYOUT_SCALE
        }
    }
    val elkNodeToVertex = vertices.associateBy { vertexToElkNode.getValue(it) }
    val elkLabelToTransition = mutableMapOf<ElkLabel, Transition>()
    val elkEdgeToEdge = edges.values.associateBy { edge ->
        val elkEdge = ElkGraphUtil.createSimpleEdge(
            ElkGraphUtil.createPort(vertexToElkNode.getValue(edge.source)),
            ElkGraphUtil.createPort(vertexToElkNode.getValue(edge.target))
        )
        edge.transitions.forEach { transition ->
            val elkLabel = ElkGraphUtil.createLabel(transition.propetiesText, elkEdge)
            elkLabel.width = transitionLayoutBounds.getValue(transition).width / LAYOUT_SCALE
            elkLabel.height = transitionLayoutBounds.getValue(transition).height / LAYOUT_SCALE
            elkLabel.setProperty(CoreOptions.FONT_SIZE, TRANSITION_FONT_SIZE)
            elkLabel.setProperty(CoreOptions.FONT_NAME, Font.font(TRANSITION_FONT_SIZE.toDouble()).name)
            elkLabelToTransition[elkLabel] = transition
        }
        elkEdge
    }
    elkGraph.setProperty(CoreOptions.EDGE_ROUTING, EdgeRouting.SPLINES)
    elkGraph.setProperty(CoreOptions.DIRECTION, Direction.RIGHT)
    elkGraph.setProperty(CoreOptions.SPACING_EDGE_LABEL, 0.0)
    return ELKGraphMapping(elkGraph, elkNodeToVertex, elkLabelToTransition, elkEdgeToEdge)
}

fun Automaton.applyLayout(elkGraphMapping: ELKGraphMapping, transitionLayoutBounds: Map<Transition, Bounds>) =
    undoRedoManager.group {
        with(elkGraphMapping) {
            fun fixElkPos(x: Double, y: Double) = Point2D(x, y)
                .subtract(elkGraph.width / 2, elkGraph.height / 2) * LAYOUT_SCALE + GRAPH_PANE_CENTER

            elkGraph.children.forEach { elkNode ->
                elkNodeToVertex[elkNode]?.let { vertex ->
                    vertex.lastReleasePosition = fixElkPos(
                        elkNode.x + (AutomatonVertex.RADIUS / LAYOUT_SCALE),
                        elkNode.y + (AutomatonVertex.RADIUS / LAYOUT_SCALE)
                    )
                    vertex.requiresLayout = false
                }
            }
            elkGraph.containedEdges.forEach { elkEdge ->
                elkEdgeToEdge[elkEdge]?.routing =
                    AutomatonEdge.PiecewiseCubicSpline(
                        elkEdge.sections.flatMapIndexed { i, elkEdgeSection ->
                            buildList {
                                if (i == 0) add(fixElkPos(elkEdgeSection.startX, elkEdgeSection.startY))
                                addAll(elkEdgeSection.bendPoints.map { fixElkPos(it.x, it.y) })
                                add(fixElkPos(elkEdgeSection.endX, elkEdgeSection.endY))
                            }
                        })
                elkEdge.labels.forEach { elkLabel ->
                    elkLabelToTransition[elkLabel]?.let { transition ->
                        val layoutBounds = transitionLayoutBounds[transition]
                        if (layoutBounds != null) {
                            transition.position = fixElkPos(
                                elkLabel.x + layoutBounds.width / 2.0 / LAYOUT_SCALE,
                                elkLabel.y + layoutBounds.height / 2.0 / LAYOUT_SCALE
                            )
                        } else {
                            edges[transition.source to transition.target]?.resetRouting()
                        }
                    }
                }
            }
        }
    }
