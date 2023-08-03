package io.github.seiko.precompose

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.seiko.precompose.annotation.NavGraphContainer
import io.github.seiko.precompose.debug.TimeMeasure
import io.github.seiko.precompose.destination.NavGraphContainerGenerator
import io.github.seiko.precompose.destination.NavGraphDestinationGenerator

internal class RouteGraphProcessor(
    private val isGenerateContainer: Boolean,
    private val navGraphDestinationGenerator: NavGraphDestinationGenerator,
    private val navGraphContainerGenerator: NavGraphContainerGenerator,
    private val timeMeasure: TimeMeasure,
) : SymbolProcessor {

    private var step = Step.CollectScene

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var nextGenerateList = emptyList<KSAnnotated>()
        timeMeasure.measure(step.name) {
            when (step) {
                Step.CollectScene -> {
                    navGraphDestinationGenerator.generate(resolver)
                    if (isGenerateContainer) {
                        step = Step.GenerateContainer
                        // pass containers to Step.GenerateContainer
                        nextGenerateList = resolver
                            .getSymbolsWithAnnotation(NavGraphContainer::class.qualifiedName!!)
                            .toList()
                    } else {
                        step = Step.Completed
                    }
                }
                Step.GenerateContainer -> {
                    navGraphContainerGenerator.generate(resolver)
                    step = Step.Completed
                }
                Step.Completed -> {
                    // no op
                }
            }
        }
        return nextGenerateList
    }

    private enum class Step {
        CollectScene,
        GenerateContainer,
        Completed,
    }
}
