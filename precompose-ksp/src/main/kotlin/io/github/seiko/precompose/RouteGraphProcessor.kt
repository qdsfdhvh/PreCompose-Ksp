package io.github.seiko.precompose

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.seiko.precompose.destination.RouteGraphGenerator

internal class RouteGraphProcessor(
    private val routeGraphGenerator: RouteGraphGenerator,
) : SymbolProcessor {

    private enum class Step {
        CollectScene,
        Completed,
    }

    private var step = Step.CollectScene

    override fun process(resolver: Resolver): List<KSAnnotated> {
        when (step) {
            Step.CollectScene -> {
                routeGraphGenerator.generate(resolver)
            }
            Step.Completed -> {
                // no op
            }
        }
        return emptyList()
    }
}
