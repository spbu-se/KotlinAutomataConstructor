package automaton.constructor.model.module.layout.static

import automaton.constructor.utils.I18N
import org.eclipse.elk.core.options.CoreOptions
import org.eclipse.elk.graph.ElkNode

object ELKLayeredLayout : StaticLayout {
    override val name: String
        get() = I18N.messages.getString("Layout.ELKLayered")

    override fun configureLayout(elkGraph: ElkNode) {
        elkGraph.setProperty(CoreOptions.ALGORITHM, "layered")
    }
}
