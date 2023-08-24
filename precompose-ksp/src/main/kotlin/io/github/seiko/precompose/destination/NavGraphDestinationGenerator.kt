package io.github.seiko.precompose.destination

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.NavGraphDestinationDeclaration
import io.github.seiko.precompose.symbol.of

internal class NavGraphDestinationGenerator(
    private val fileGenerator: FileGenerator,
    private val navGraphDestinationFileSpecFactory: NavGraphDestinationFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        collectNavGraphDestination(resolver).forEach { navGraphDestination ->
            val fileSpec = navGraphDestinationFileSpecFactory.create(navGraphDestination)
            fileGenerator.createNewFile(
                fileSpec = fileSpec,
                dependencies = Dependencies(false, navGraphDestination.containingFile!!),
            )
        }
    }

    private fun collectNavGraphDestination(resolver: Resolver): Sequence<NavGraphDestinationDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(NavGraphDestination::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { NavGraphDestinationDeclaration.of(it) }
    }
}
