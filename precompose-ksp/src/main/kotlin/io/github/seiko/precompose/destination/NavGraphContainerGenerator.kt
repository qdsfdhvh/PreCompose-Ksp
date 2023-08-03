package io.github.seiko.precompose.destination

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphContainer
import io.github.seiko.precompose.code.Names
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.NavGraphContainerDeclaration
import io.github.seiko.precompose.symbol.NavGraphDestinationLinkDeclaration
import io.github.seiko.precompose.symbol.of

internal class NavGraphContainerGenerator(
    private val logger: KSPLogger,
    private val packageName: String?,
    private val fileGenerator: FileGenerator,
    private val rootRouteGraphFileSpecFactory: NavGraphContainerFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        val container = collectRouteGraphs(resolver) ?: return
        val containerFileSpec = rootRouteGraphFileSpecFactory.create(container)
        fileGenerator.createNewFile(
            fileSpec = containerFileSpec,
            aggregating = false,
        )
    }

    @OptIn(KspExperimental::class)
    private fun collectRouteGraphs(resolver: Resolver): NavGraphContainerDeclaration? {
        val links = resolver
            .getDeclarationsFromPackage(packageName ?: Names.routeGraphPackageName)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { NavGraphDestinationLinkDeclaration.of(it) }

        logger.info(
            "find links(${
                links.toList()
                    .joinToString(",\n") { it.packageName + '.' + it.name }
            })",
        )

        val containers = resolver
            .getSymbolsWithAnnotation(NavGraphContainer::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { NavGraphContainerDeclaration.of(it, links) }

        logger.info("find container ${containers.toList()}")

        if (containers.none()) return null
        return containers.first()
    }
}
