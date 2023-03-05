package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.utils.I18N

enum class DynamicLayoutPolicy(val displayName: String) {
    LAYOUT_ALL(I18N.messages.getString("DynamicLayoutPolicy.All")),
    /**
     * @see [AutomatonVertex.requiresLayout]
     */
    LAYOUT_REQUIRING(I18N.messages.getString("DynamicLayoutPolicy.Requiring")),
    LAYOUT_NONE(I18N.messages.getString("DynamicLayoutPolicy.None"));

    companion object {
        val DEFAULT = LAYOUT_REQUIRING
    }
}
