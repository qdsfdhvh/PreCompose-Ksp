package io.github.seiko.precompose.destination

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.SceneDeclaration
import io.github.seiko.precompose.symbol.of

internal class SceneGenerator(
    private val fileGenerator: FileGenerator,
    private val sceneFileSpecFactory: SceneFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        collectScenes(resolver).forEach { scene ->
            val file = sceneFileSpecFactory.create(scene)
            fileGenerator.createNewFile(
                fileSpec = file,
                aggregating = true,
            )
        }
    }

    private fun collectScenes(resolver: Resolver): Sequence<SceneDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(NavGraphDestination::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { SceneDeclaration.of(it) }
    }
}

