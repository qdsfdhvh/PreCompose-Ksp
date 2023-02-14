package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.annotation.Path
import io.github.seiko.precompose.annotation.Query
import io.github.seiko.precompose.annotation.RouteGraph

@OptIn(KspExperimental::class)
internal class RouteGraphProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator

    companion object {
        private val DESTINATION_ANNOTATION_NAME =
            requireNotNull(NavGraphDestination::class.qualifiedName) { "Can not get qualifiedName for RouteGraphDestination" }
        private val ROUTE_GRAPH_NAME =
            requireNotNull(RouteGraph::class.qualifiedName) { "Can not get qualifiedName for RouteGraph" }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val sceneSymbols = resolver
            .getSymbolsWithAnnotation(DESTINATION_ANNOTATION_NAME)
            .filterIsInstance<KSFunctionDeclaration>()

        val routeGraphSymbols = resolver
            .getSymbolsWithAnnotation(ROUTE_GRAPH_NAME)
            .filterIsInstance<KSFunctionDeclaration>()

        routeGraphSymbols
            .forEach { routeGraphSymbol ->
                generateRouteGraph(routeGraphSymbol, sceneSymbols.toList())
                    .writeTo(
                        codeGenerator,
                        Dependencies(true),
                    )
                generateRouteGraphMetaData(routeGraphSymbol)
                    .writeTo(
                        codeGenerator,
                        Dependencies(true),
                    )
            }
        return emptyList()
    }

    private fun generateRouteGraph(
        routeGraphSymbol: KSFunctionDeclaration,
        sceneSymbols: List<KSFunctionDeclaration>,
    ): FileSpec {
        val packageName = routeGraphSymbol.packageName.asString()
        val functionName = routeGraphSymbol.simpleName.asString()

        val functionBuilder = FunSpec.builder(functionName)
            .apply {
                routeGraphSymbol.extensionReceiver?.let {
                    receiver(it.toTypeName())
                }
            }

        if (routeGraphSymbol.modifiers.isNotEmpty()) {
            functionBuilder.addModifiers(KModifier.ACTUAL)
            functionBuilder.addModifiers(
                routeGraphSymbol.modifiers
                    .filter { it.name != KModifier.EXPECT.name }
                    .mapNotNull { it.toKModifier() },
            )
        }

        val functionNames = functionBuilder.addParameterAndReturnNavigatorNames(
            routeGraphSymbol.parameters,
        )

        val fileBuilder = FileSpec.builder(packageName, functionName)
        sceneSymbols.forEach { sceneSymbol ->
            generateScene(
                fileBuilder = fileBuilder,
                functionBuilder = functionBuilder,
                functionNames = functionNames,
                sceneSymbol = sceneSymbol,
            )
        }

        return fileBuilder.addFunction(functionBuilder.build())
            .build()
    }

    private fun generateScene(
        fileBuilder: FileSpec.Builder,
        functionBuilder: FunSpec.Builder,
        functionNames: NavigatorFunctionNames,
        sceneSymbol: KSFunctionDeclaration,
    ) {
        val annotation = sceneSymbol.getAnnotationsByType(NavGraphDestination::class).first()
        if (annotation.packageName.isNotEmpty()) {
            fileBuilder.addImport(annotation.packageName, annotation.functionName)
        }

        functionBuilder.addStatement("%L(", annotation.functionName)
        functionBuilder.addCode(
            buildCodeBlock {
                withIndent {
                    addStatement(
                        "route = %S,",
                        annotation.route,
                    )
                    if (annotation.deepLink.isNotEmpty()) {
                        addStatement("deepLinks = listOf(")
                        withIndent {
                            annotation.deepLink.forEach {
                                addStatement("%S,", it)
                            }
                        }
                        addStatement("),")
                    }
                }
            },
        )
        functionBuilder.beginControlFlow(")")
        sceneSymbol.parameters.forEach {
            if (it.isAnnotationPresent(Path::class)) {
                require(!it.type.resolve().isMarkedNullable)
            }
            if (it.isAnnotationPresent(Query::class)) {
                require(it.type.resolve().isMarkedNullable)
            }
            if (it.isAnnotationPresent(Path::class)) {
                val path = it.getAnnotationsByType(Path::class).first()
                functionBuilder.addStatement(
                    "val %L = it.%T<%T>(%S)!!",
                    it.name?.asString().orEmpty(),
                    pathType,
                    it.type.toTypeName(),
                    path.name,
                )
            } else if (it.isAnnotationPresent(Query::class)) {
                val query = it.getAnnotationsByType(Query::class).first()
                functionBuilder.addStatement(
                    "val %L = it.%T<%T>(%S)",
                    it.name?.asString().orEmpty(),
                    queryType,
                    it.type.toTypeName(),
                    query.name,
                )
            }
        }

        functionBuilder.addNavigateParameters(
            fileBuilder = fileBuilder,
            functionNames = functionNames,
            functionDeclaration = sceneSymbol,
        )
        functionBuilder.endControlFlow()
    }

    private fun generateRouteGraphMetaData(
        routeGraphSymbol: KSFunctionDeclaration,
    ): FileSpec {
        val functionName = routeGraphSymbol.simpleName.asString()

        return FileSpec.builder(META_PACKAGE_NAME, "meta$$functionName")
            .addImport(
                routeGraphSymbol.packageName.asString(),
                functionName,
            )
            .addFunction(
                FunSpec.builder(functionName)
                    .receiver(routeGraphSymbol.extensionReceiver!!.toTypeName())
                    .addParameters(
                        routeGraphSymbol.parameters.map {
                            ParameterSpec.builder(it.name!!.asString(), it.type.toTypeName())
                                .apply {
                                    it.annotations.forEach { annotation ->
                                        addAnnotation(annotation.toAnnotationSpec())
                                    }
                                }
                                .build()
                        },
                    )
                    .addStatement(
                        "%L(",
                        functionName,
                    )
                    .addCode(
                        buildCodeBlock {
                            withIndent {
                                routeGraphSymbol.parameters.forEach {
                                    addStatement(
                                        "%L = %L,",
                                        it.name!!.asString(),
                                        it.name!!.asString(),
                                    )
                                }
                            }
                        },
                    )
                    .addStatement(")")
                    .build(),
            )
            .build()
    }
}
