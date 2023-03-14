package io.github.seiko.precompose

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.seiko.precompose.destination.SceneFileSpecFactory
import io.github.seiko.precompose.destination.SceneGenerator
import io.github.seiko.precompose.file.FileGenerator

class RouteGraphProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteGraphProcessor(
            sceneGenerator = SceneGenerator(
                fileGenerator = FileGenerator(
                    codeGenerator = environment.codeGenerator,
                ),
                sceneFileSpecFactory = SceneFileSpecFactory(),
            ),
        )
    }
}
