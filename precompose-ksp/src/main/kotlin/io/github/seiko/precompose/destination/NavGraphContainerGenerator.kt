package io.github.seiko.precompose.destination

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphContainer
import io.github.seiko.precompose.code.Names
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.NavGraphContainerDeclaration
import io.github.seiko.precompose.symbol.NavGraphDestinationLinkDeclaration
import io.github.seiko.precompose.symbol.of

internal class NavGraphContainerGenerator(
    private val packageName: String?,
    private val fileGenerator: FileGenerator,
    private val navGraphContainerFileSpecFactory: NavGraphContainerFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        val container = collectFirstNavGraphContainer(resolver) ?: return
        val containerFileSpec = navGraphContainerFileSpecFactory.create(container)
        fileGenerator.createNewFile(
            fileSpec = containerFileSpec,
            dependencies = Dependencies(
                aggregating = true,
                *container.links.mapNotNull {
                    it.containingFile
                }.toList().toTypedArray(),
            ),
        )
    }

    @OptIn(KspExperimental::class)
    private fun collectFirstNavGraphContainer(resolver: Resolver): NavGraphContainerDeclaration? {
        val containers = resolver
            .getSymbolsWithAnnotation(NavGraphContainer::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
        if (containers.none()) return null

        val links = resolver
            .getDeclarationsFromPackage(packageName ?: Names.ROOT_GRAPH_PACKAGE_NAME)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { NavGraphDestinationLinkDeclaration.of(it) }
        return containers.map { NavGraphContainerDeclaration.of(it, links) }.first()
    }
}
