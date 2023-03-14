package io.github.seiko.precompose.destination

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.RootRouteGraph
import io.github.seiko.precompose.code.Names
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.RootRouteGraphDeclaration
import io.github.seiko.precompose.symbol.RouteGraphDeclaration
import io.github.seiko.precompose.symbol.of

internal class RootRouteGraphGenerator(
    private val fileGenerator: FileGenerator,
    private val rootRouteGraphFileSpecFactory: RootRouteGraphFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        val rootRouteGraph = collectRouteGraphs(resolver) ?: return
        val file = rootRouteGraphFileSpecFactory.create(rootRouteGraph)
        fileGenerator.createNewFile(
            fileSpec = file,
            aggregating = true,
        )
    }

    @OptIn(KspExperimental::class)
    private fun collectRouteGraphs(resolver: Resolver): RootRouteGraphDeclaration? {
        val routeGraphs = resolver
            .getDeclarationsFromPackage(Names.routeGraphPackageName)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { RouteGraphDeclaration.of(it) }

        val rootRouteGraphs = resolver
            .getSymbolsWithAnnotation(RootRouteGraph::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { RootRouteGraphDeclaration.of(it, routeGraphs) }
        if (rootRouteGraphs.none()) return null
        return rootRouteGraphs.first()
    }
}
