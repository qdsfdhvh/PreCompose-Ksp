package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val rootRouteGraphs = resolver
            .getSymbolsWithAnnotation(
                RootRouteGraph::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for RootRouteGraph")
            ).filterIsInstance<KSFunctionDeclaration>()

        val routeGraphs = if (rootRouteGraphs.any()) {
            resolver.getDeclarationsFromPackage(META_PACKAGE_NAME)
                .filterIsInstance<KSFunctionDeclaration>()
        } else emptySequence()

        val ret = rootRouteGraphs.filter { !it.validate() }.toList()
        rootRouteGraphs.filter { it.validate() }
            .forEach { functionDeclaration ->
                val packageName = functionDeclaration.packageName.asString()
                val functionName = functionDeclaration.simpleName.asString()

                val functionBuilder = FunSpec.builder(functionName)
                    .receiver(routeBuilderType)
                    .apply {
                        functionDeclaration.extensionReceiver?.let {
                            receiver(it.toTypeName())
                        }
                    }

                if (functionDeclaration.modifiers.isNotEmpty()) {
                    functionBuilder.addModifiers(KModifier.ACTUAL)
                    functionBuilder.addModifiers(
                        functionDeclaration.modifiers
                            .filter { it.name != KModifier.EXPECT.name }
                            .mapNotNull { it.toKModifier() }
                    )
                }

                val functionNames = functionBuilder.addParameterAndReturnNavigatorNames(
                    functionDeclaration.parameters
                )
                require(functionNames.navigatorName.isNotEmpty()) {
                    "not find navigator in ${functionDeclaration.packageName}.$functionName"
                }

                val fileBuilder = FileSpec.builder(packageName, functionName)

                routeGraphs.forEach { routeGraph ->
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
        return ret
    }
}