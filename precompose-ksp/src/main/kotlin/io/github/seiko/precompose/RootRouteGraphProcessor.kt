package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.seiko.precompose.annotation.RootRouteGraph

@OptIn(KspExperimental::class)
class RootRouteGraphProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator

    companion object {
        private val ROOT_ROUTE_GRAPH_NAME =
            requireNotNull(RootRouteGraph::class.qualifiedName) { "Can not get qualifiedName for RootRouteGraph" }
    }

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val rootRouteGraphSymbols = resolver
            .getSymbolsWithAnnotation(ROOT_ROUTE_GRAPH_NAME)
            .filterIsInstance<KSFunctionDeclaration>()

        val metaRouteGraphSymbols = if (rootRouteGraphSymbols.any()) {
            resolver
                .getDeclarationsFromPackage(META_PACKAGE_NAME)
                .filterIsInstance<KSFunctionDeclaration>()
        } else {
            emptySequence()
        }

        rootRouteGraphSymbols
            .forEach { rootRouteGraphSymbol ->
                val packageName = rootRouteGraphSymbol.packageName.asString()
                val functionName = rootRouteGraphSymbol.simpleName.asString()

                val functionBuilder = FunSpec.builder(functionName)
                    .apply {
                        rootRouteGraphSymbol.extensionReceiver?.let {
                            receiver(it.toTypeName())
                        }
                    }

                if (rootRouteGraphSymbol.modifiers.isNotEmpty()) {
                    functionBuilder.addModifiers(KModifier.ACTUAL)
                    functionBuilder.addModifiers(
                        rootRouteGraphSymbol.modifiers
                            .filter { it.name != KModifier.EXPECT.name }
                            .mapNotNull { it.toKModifier() },
                    )
                }

                val functionNames = functionBuilder.addParameterAndReturnNavigatorNames(
                    rootRouteGraphSymbol.parameters,
                )

                val fileBuilder = FileSpec.builder(packageName, functionName)

                metaRouteGraphSymbols.forEach { routeGraph ->
                    functionBuilder.addNavigateParameters(
                        fileBuilder = fileBuilder,
                        functionNames = functionNames,
                        functionDeclaration = routeGraph,
                        allowBackStackEntry = false,
                    )
                }

                fileBuilder.addFunction(functionBuilder.build())
                    .build()
                    .writeTo(codeGenerator, Dependencies(true))
            }
        return emptyList()
    }
}
