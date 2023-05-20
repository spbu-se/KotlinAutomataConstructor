package automaton.constructor.model.module.layout.static

import org.eclipse.elk.alg.graphviz.dot.transform.Command
import org.eclipse.elk.alg.graphviz.layouter.GraphvizMetaDataProvider
import org.eclipse.elk.alg.graphviz.layouter.GraphvizTool
import org.eclipse.elk.core.data.LayoutMetaDataService
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll

class DOTLayoutTest : AbstractStaticLayoutTest(DOTLayout) {
    @BeforeAll
    fun registerGraphviz() {
        runCatching {
            LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(GraphvizMetaDataProvider())
        }
        assumeTrue(runCatching { GraphvizTool(Command.DOT).initialize() }.isSuccess)
    }
}