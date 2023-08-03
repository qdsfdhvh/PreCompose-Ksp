package io.github.seiko.precompose.destination

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.code.Names
import io.github.seiko.precompose.code.pathType
import io.github.seiko.precompose.code.queryType
import io.github.seiko.precompose.symbol.FunctionParameterType
import io.github.seiko.precompose.symbol.NavGraphDestinationDeclaration

internal class NavGraphDestinationFileSpecFactory(
    private val packageName: String?,
) {

    fun create(scene: NavGraphDestinationDeclaration): FileSpec {
        return FileSpec.builder(
            packageName ?: Names.routeGraphPackageName,
            scene.fileName,
        ).apply {
            if (scene.packageName.isNotEmpty()) {
                addImport(scene.packageName, scene.name)
            }
            if (scene.scenePackageName.isNotEmpty()) {
                addImport(scene.scenePackageName, scene.sceneName)
            }
            addFunction(createSceneFunction(scene))
        }.build()
    }

    private fun createSceneFunction(scene: NavGraphDestinationDeclaration): FunSpec {
        return FunSpec.builder(
            scene.fileName,
        ).apply {
            receiver(scene.receiver)
            scene.parameters.forEach {
                when (it.type) {
                    is FunctionParameterType.Path -> Unit
                    is FunctionParameterType.Query -> Unit
                    FunctionParameterType.Navigate,
                    FunctionParameterType.Back,
                    FunctionParameterType.Custom,
                    -> {
                        addParameter(it.name, it.typeName)
                    }
                }
            }
            addSceneFunction(scene)
        }.build()
    }

    private fun FunSpec.Builder.addSceneFunction(scene: NavGraphDestinationDeclaration) {
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
            },
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
                            FunctionParameterType.Custom,
                            -> {
                                addStatement(
                                    "%N = %N,",
                                    it.name,
                                    it.name,
                                )
                            }
                        }
                    }
                }
            },
        )
        addStatement(")")
        endControlFlow()
    }
}
