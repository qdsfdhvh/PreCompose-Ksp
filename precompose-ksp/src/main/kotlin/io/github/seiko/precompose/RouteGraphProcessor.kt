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
import com.google.devtools.ksp.validate
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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val scenes = resolver
            .getSymbolsWithAnnotation(
                NavGraphDestination::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for RouteGraphDestination")
            ).filterIsInstance<KSFunctionDeclaration>()

        val routeGraphs = resolver
            .getSymbolsWithAnnotation(
                RouteGraph::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for RouteGraph")
            ).filterIsInstance<KSFunctionDeclaration>()

        val ret = routeGraphs.filter { !it.validate() }.toList()
        routeGraphs.filter { it.validate() }
            .forEach { routeGraph ->
                generateRouteGraph(routeGraph, scenes.toList())
                generateRouteGraphMetaData(routeGraph)
            }
        return ret
    }

    private fun generateRouteGraph(
        routeGraph: KSFunctionDeclaration,
        scenes: List<KSFunctionDeclaration>,
    ) {
        val packageName = routeGraph.packageName.asString()
        val functionName = routeGraph.simpleName.asString()

        val functionBuilder = FunSpec.builder(functionName)
            .apply {
                routeGraph.extensionReceiver?.let {
                    receiver(it.toTypeName())
                }
            }

        if (routeGraph.modifiers.isNotEmpty()) {
            functionBuilder.addModifiers(KModifier.ACTUAL)
            functionBuilder.addModifiers(
                routeGraph.modifiers
                    .filter { it.name != KModifier.EXPECT.name }
                    .mapNotNull { it.toKModifier() }
            )
        }

        val functionNames = functionBuilder.addParameterAndReturnNavigatorNames(
            routeGraph.parameters
        )

        val fileBuilder = FileSpec.builder(packageName, functionName)
        scenes.forEach { scene ->
            generateScene(
                fileBuilder = fileBuilder,
                functionBuilder = functionBuilder,
                functionNames = functionNames,
                scene = scene,
            )
        }

        fileBuilder.addFunction(functionBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true))
    }

    private fun generateScene(
        fileBuilder: FileSpec.Builder,
        functionBuilder: FunSpec.Builder,
        functionNames: NavigatorFunctionNames,
        scene: KSFunctionDeclaration,
    ) {
        val annotation = scene.getAnnotationsByType(NavGraphDestination::class).first()
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
            }
        )
        functionBuilder.beginControlFlow(")")
        scene.parameters.forEach {
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
            functionDeclaration = scene,
        )
        functionBuilder.endControlFlow()
    }

    private fun generateRouteGraphMetaData(
        functionDeclaration: KSFunctionDeclaration,
    ) {
        val functionName = functionDeclaration.simpleName.asString()

        FileSpec.builder(META_PACKAGE_NAME, "meta$$functionName")
            .addImport(
                functionDeclaration.packageName.asString(),
                functionName,
            )
            .addFunction(
                FunSpec.builder(functionName)
                    .receiver(functionDeclaration.extensionReceiver!!.toTypeName())
                    .addParameters(
                        functionDeclaration.parameters.map {
                            ParameterSpec.builder(it.name!!.asString(), it.type.toTypeName())
                                .apply {
                                    it.annotations.forEach { annotation ->
                                        addAnnotation(annotation.toAnnotationSpec())
                                    }
                                }
                                .build()
                        }
                    )
                    .addStatement(
                        "%L(",
                        functionName,
                    )
                    .addCode(
                        buildCodeBlock {
                            withIndent {
                                functionDeclaration.parameters.forEach {
                                    addStatement(
                                        "%L = %L,",
                                        it.name!!.asString(),
                                        it.name!!.asString(),
                                    )
                                }
                            }
                        }
                    )
                    .addStatement(")")
                    .build()
            )
            .build()
            .writeTo(codeGenerator, Dependencies(true))
    }
}
