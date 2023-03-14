package io.github.seiko.precompose

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.seiko.precompose.destination.RootRouteGraphGenerator

internal class RootRouteGraphProcessor(
    private val rootRouteGraphGenerator: RootRouteGraphGenerator,
) : SymbolProcessor {

    private enum class Step {
        CollectRouteGraph,
        Completed,
    }

    private var step = Step.CollectRouteGraph

    override fun process(resolver: Resolver): List<KSAnnotated> {
        when (step) {
            Step.CollectRouteGraph -> {
                rootRouteGraphGenerator.generate(resolver)
            }
            Step.Completed -> {
                // no op
            }
        }
        return emptyList()
    }
}
