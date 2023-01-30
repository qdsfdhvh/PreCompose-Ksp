package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import io.github.seiko.precompose.annotation.Meta
import io.github.seiko.precompose.annotation.RootRouteGraph

@OptIn(KspExperimental::class)
class RootRouteGraphProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val generates = resolver
            .getSymbolsWithAnnotation(
                RootRouteGraph::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for RootRouteGraph")
            )

        if (generates.any()) {
            logger.warn("find RootRouteGraph !!!")

            val a = resolver.getDeclarationsFromPackage(META_PACKAGE_NAME)
            a.forEach {
                it.getAnnotationsByType(Meta::class).firstOrNull()?.let { meta ->
                    logger.warn(meta.metadata)
                }
            }
        }

        val ret = generates.filter { !it.validate() }.toList()
        generates.filter { it.validate() }
            .forEach { }
        return ret
    }
}