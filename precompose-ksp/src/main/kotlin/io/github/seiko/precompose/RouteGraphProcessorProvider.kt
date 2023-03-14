package io.github.seiko.precompose

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.seiko.precompose.destination.RouteGraphFileSpecFactory
import io.github.seiko.precompose.destination.RouteGraphGenerator
import io.github.seiko.precompose.file.FileGenerator

class RouteGraphProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteGraphProcessor(
            routeGraphGenerator = RouteGraphGenerator(
                fileGenerator = FileGenerator(
                    codeGenerator = environment.codeGenerator,
                ),
                routeGraphFileSpecFactory = RouteGraphFileSpecFactory(),
            ),
        )
    }
}
