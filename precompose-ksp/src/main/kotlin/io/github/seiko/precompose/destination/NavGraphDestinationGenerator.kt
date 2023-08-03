package io.github.seiko.precompose.destination

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.NavGraphDestinationDeclaration
import io.github.seiko.precompose.symbol.of

internal class NavGraphDestinationGenerator(
    private val logger: KSPLogger,
    private val fileGenerator: FileGenerator,
    private val sceneFileSpecFactory: NavGraphDestinationFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        collectScenes(resolver).forEach { scene ->
            logger.info("find scene ${scene.packageName}.${scene.name}")
            val fileSpec = sceneFileSpecFactory.create(scene)
            fileGenerator.createNewFile(
                fileSpec = fileSpec,
                aggregating = true,
            )
        }
    }

    private fun collectScenes(resolver: Resolver): Sequence<NavGraphDestinationDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(NavGraphDestination::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { NavGraphDestinationDeclaration.of(it) }
    }
}
