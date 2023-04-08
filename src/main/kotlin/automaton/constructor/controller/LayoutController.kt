package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.transformationOutput
import automaton.constructor.model.element.Transition
import automaton.constructor.model.module.layout.dynamic.DynamicLayoutPolicy
import automaton.constructor.model.module.layout.dynamic.DynamicLayoutPolicy.LAYOUT_ALL
import automaton.constructor.model.module.layout.dynamic.DynamicLayoutPolicy.LAYOUT_REQUIRING
import automaton.constructor.model.module.layout.dynamic.dynamicLayout
import automaton.constructor.model.module.layout.static.StaticLayout
import automaton.constructor.model.module.layout.static.applyLayout
import automaton.constructor.model.module.layout.static.toElkGraphMapping
import automaton.constructor.utils.*
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Bounds
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.util.Duration
import org.eclipse.elk.core.RecursiveGraphLayoutEngine
import org.eclipse.elk.core.util.BasicProgressMonitor
import tornadofx.*
import java.text.MessageFormat

class LayoutController(val uiComponent: UIComponent) {
    val selectedAutomatonProperty = SimpleObjectProperty<Automaton>()
    var selectedAutomaton by selectedAutomatonProperty

    val policyProperty = DynamicLayoutPolicy.DEFAULT.toProperty()
    var policy: DynamicLayoutPolicy by policyProperty

    private var isDynamicLayoutRunning = false

    fun undoDynamicLayout() {
        if (policy == LAYOUT_ALL)
            policy = LAYOUT_REQUIRING
        for (automaton in automataToLayoutDynamically)
            automaton.vertices.forEach {
                it.position = it.lastReleasePosition
            }
    }

    fun layout(automaton: Automaton, transitionLayoutBounds: Map<Transition, Bounds>, layout: StaticLayout) {
        val elkGraphMapping = automaton.toElkGraphMapping(transitionLayoutBounds)
        layout.configureLayout(elkGraphMapping.elkGraph)
        var isCancelled = false
        val progressMonitor = object : BasicProgressMonitor() {
            override fun isCanceled() = isCancelled
        }
        uiComponent.runAsyncWithDialog(
            MessageFormat.format(
                I18N.messages.getString("LayoutController.LayingOutAutomaton"),
                automaton.name,
                layout.name
            ), daemon = true
        ) {
            RecursiveGraphLayoutEngine().layout(elkGraphMapping.elkGraph, progressMonitor)
        } addOnCancel {
            isCancelled = true
        } addOnSuccess {
            if (policy == LAYOUT_ALL)
                policy = LAYOUT_REQUIRING
            automaton.applyLayout(elkGraphMapping, transitionLayoutBounds)
        } addOnFail {
            if (!layout.requiresGraphviz) throw it
            else
                alert(
                    Alert.AlertType.ERROR,
                    I18N.messages.getString("LayoutController.SuggestInstallGraphviz"),
                    null,
                    ButtonType(I18N.messages.getString("Dialog.yes.button"), ButtonType.YES.buttonData),
                    ButtonType(I18N.messages.getString("Dialog.no.button"), ButtonType.NO.buttonData),
                    owner = uiComponent.currentWindow,
                    title = I18N.messages.getString("Dialog.error")
                ) { button ->
                    if (button.buttonData == ButtonType.YES.buttonData) {
                        val os = System.getProperty("os.name")?.lowercase()
                        FX.application.hostServices.showDocument(
                            "https://graphviz.org/download/" + when {
                                os?.contains("windows") == true -> "#windows"
                                os?.contains("linux") == true -> "#linux"
                                os?.contains("mac") == true -> "#mac"
                                else -> ""
                            }
                        )
                    }
                }
        }
    }

    fun startDynamicLayout() {
        if (!isDynamicLayoutRunning) {
            isDynamicLayoutRunning = true
            dynamicLayoutStep()
        }
    }

    fun stopDynamicLayout() {
        isDynamicLayoutRunning = false
    }

    private fun dynamicLayoutStep() {
        for (automaton in automataToLayoutDynamically)
            automaton.dynamicLayout.sync(policy)
        runAsync {
            for (automaton in automataToLayoutDynamically)
                automaton.dynamicLayout.step(policy)
        } addOnSuccess {
            if (isDynamicLayoutRunning)
                runLater(DYNAMIC_LAYOUT_PERIOD) {
                    dynamicLayoutStep()
                }
        } addOnFail {
            throw it
        }
    }

    private val automataToLayoutDynamically
        get() = listOfNotNull(
            selectedAutomaton,
            selectedAutomaton.transformationOutput
        )

    companion object {
        private val DYNAMIC_LAYOUT_PERIOD = Duration.seconds(1 / 50.0)
    }
}
