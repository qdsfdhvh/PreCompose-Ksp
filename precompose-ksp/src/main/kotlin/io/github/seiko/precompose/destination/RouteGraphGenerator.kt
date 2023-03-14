package io.github.seiko.precompose.destination

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.file.FileGenerator
import io.github.seiko.precompose.symbol.RouteGraphDeclaration
import io.github.seiko.precompose.symbol.SceneDeclaration
import io.github.seiko.precompose.symbol.of

internal class RouteGraphGenerator(
    private val fileGenerator: FileGenerator,
    private val routeGraphFileSpecFactory: RouteGraphFileSpecFactory,
) {
    fun generate(resolver: Resolver) {
        val routeGraph = collectScenes(resolver) ?: return
        val file = routeGraphFileSpecFactory.create(routeGraph)
        fileGenerator.createNewFile(
            fileSpec = file,
            aggregating = true,
        )
    }

    private fun collectScenes(resolver: Resolver): RouteGraphDeclaration? {
        val scenes = resolver
            .getSymbolsWithAnnotation(NavGraphDestination::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .map { SceneDeclaration.of(it) }
        if (scenes.none()) return null
        return RouteGraphDeclaration.of(
            name = "routeGraph_${moduleName(resolver)}",
            scenes = scenes,
        )
    }
}

// TODO: not support k2
// https://github.com/google/ksp/issues/1015
private fun moduleName(resolver: Resolver): String {
    val moduleDescriptor = resolver::class.java
        .getDeclaredField("module")
        .apply { isAccessible = true }
        .get(resolver)
    val rawName = moduleDescriptor::class.java
        .getMethod("getName")
        .invoke(moduleDescriptor)
        .toString()
    return rawName.removeSurrounding("<", ">")
}
