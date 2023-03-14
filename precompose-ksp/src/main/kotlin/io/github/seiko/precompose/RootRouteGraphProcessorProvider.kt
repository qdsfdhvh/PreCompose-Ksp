package io.github.seiko.precompose

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.seiko.precompose.destination.RootRouteGraphFileSpecFactory
import io.github.seiko.precompose.destination.RootRouteGraphGenerator
import io.github.seiko.precompose.file.FileGenerator

class RootRouteGraphProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RootRouteGraphProcessor(
            rootRouteGraphGenerator = RootRouteGraphGenerator(
                fileGenerator = FileGenerator(
                    codeGenerator = environment.codeGenerator,
                ),
                rootRouteGraphFileSpecFactory = RootRouteGraphFileSpecFactory(),
            )
        )
    }
}
