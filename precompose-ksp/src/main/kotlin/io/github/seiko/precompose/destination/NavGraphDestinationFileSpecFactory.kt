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

    fun create(destination: NavGraphDestinationDeclaration): FileSpec {
        return FileSpec.builder(
            packageName ?: Names.routeGraphPackageName,
            destination.fileName,
        ).apply {
            if (destination.packageName.isNotEmpty()) {
                addImport(destination.packageName, destination.name)
            }
            if (destination.scenePackageName.isNotEmpty()) {
                addImport(destination.scenePackageName, destination.sceneName)
            }
            addFunction(createSceneFunction(destination))
        }.build()
    }

    private fun createSceneFunction(destination: NavGraphDestinationDeclaration): FunSpec {
        return FunSpec.builder(
            destination.fileName,
        ).apply {
            receiver(destination.receiver)
            destination.parameters.forEach {
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
            addSceneFunction(destination)
        }.build()
    }

    private fun FunSpec.Builder.addSceneFunction(destination: NavGraphDestinationDeclaration) {
        addStatement("%L(", destination.sceneName)
        addCode(
            buildCodeBlock {
                withIndent {
                    addStatement("route = %S,", destination.route)
                    if (destination.deepLinks.isNotEmpty()) {
                        addStatement("deepLinks = listOf(")
                        withIndent {
                            destination.deepLinks.forEach {
                                addStatement("%S,", it)
                            }
                        }
                        addStatement("),")
                    }
                }
            },
        )
        beginControlFlow(")")
        addStatement("%L(", destination.name)
        addCode(
            buildCodeBlock {
                withIndent {
                    destination.parameters.forEach {
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
