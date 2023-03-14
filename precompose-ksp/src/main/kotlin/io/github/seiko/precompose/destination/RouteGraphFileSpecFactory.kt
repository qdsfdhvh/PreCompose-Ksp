package io.github.seiko.precompose.destination

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.code.Names
import io.github.seiko.precompose.code.pathType
import io.github.seiko.precompose.code.queryType
import io.github.seiko.precompose.symbol.FunctionParameterType
import io.github.seiko.precompose.symbol.SceneDeclaration
import io.github.seiko.precompose.symbol.RouteGraphDeclaration

internal class RouteGraphFileSpecFactory {

    fun create(routeGraph: RouteGraphDeclaration): FileSpec {
        return FileSpec.builder(
            Names.routeGraphPackageName,
            routeGraph.name,
        ).apply {
            routeGraph.scenes.forEach { scene ->
                if (scene.packageName.isNotEmpty()) {
                    addImport(scene.packageName, scene.name)
                }
                if (scene.scenePackageName.isNotEmpty()) {
                    addImport(scene.scenePackageName, scene.sceneName)
                }
            }
            addFunction(createRouteGraphFunction(routeGraph))
        }.build()
    }

    private fun createRouteGraphFunction(routeGraph: RouteGraphDeclaration): FunSpec {
        return FunSpec.builder(
            routeGraph.name,
        ).apply {
            receiver(routeGraph.receiver)
            routeGraph.parameters.forEach {
                when (it.type) {
                    is FunctionParameterType.Path -> Unit
                    is FunctionParameterType.Query -> Unit
                    FunctionParameterType.Navigate,
                    FunctionParameterType.Back,
                    FunctionParameterType.Custom -> {
                        addParameter(it.name, it.typeName)
                    }
                }
            }
            routeGraph.scenes.forEach { scene ->
                addNavGraphDestinationFunction(scene)
            }
        }.build()
    }

    private fun FunSpec.Builder.addNavGraphDestinationFunction(scene: SceneDeclaration) {
        addStatement("%L(", scene.sceneName)
        addCode(
            buildCodeBlock {
                withIndent {
                    addStatement("route = %S,", scene.route)
                    if (scene.deepLinks.isNotEmpty()) {
                        addStatement("deepLinks = listOf(")
                        withIndent {
                            scene.deepLinks.forEach {
                                addStatement("%S,", it)
                            }
                        }
                        addStatement("),")
                    }
                }
            }
        )
        beginControlFlow(")")
        addStatement("%L(", scene.name)
        addCode(
            buildCodeBlock {
                withIndent {
                    scene.parameters.forEach {
                        when (it.type) {
                            is FunctionParameterType.Path -> {
                                addStatement(
                                    "%L = it.%T(%S)!!,",
                                    it.name,
                                    pathType,
                                    it.type.name,
                                )
                            }
                            is FunctionParameterType.Query -> {
                                addStatement(
                                    "%L = it.%T(%S),",
                                    it.name,
                                    queryType,
                                    it.type.name,
                                )
                            }
                            FunctionParameterType.Navigate,
                            FunctionParameterType.Back,
                            FunctionParameterType.Custom -> {
                                addStatement(
                                    "%N = %N,",
                                    it.name,
                                    it.name,
                                )
                            }
                        }
                    }
                }
            }
        )
        addStatement(")")
        endControlFlow()
    }

}
