package io.github.seiko.precompose.destination

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.symbol.FunctionParameterType
import io.github.seiko.precompose.symbol.NavGraphContainerDeclaration
import io.github.seiko.precompose.symbol.NavGraphDestinationLinkDeclaration

internal class NavGraphContainerFileSpecFactory {

    fun create(rootRouteGraph: NavGraphContainerDeclaration): FileSpec {
        return FileSpec.builder(
            rootRouteGraph.packageName,
            rootRouteGraph.name,
        ).apply {
            rootRouteGraph.links.forEach { routeGraph ->
                if (routeGraph.packageName.isNotEmpty()) {
                    addImport(routeGraph.packageName, routeGraph.name)
                }
            }
            addFunction(createFunction(rootRouteGraph))
        }.build()
    }

    private fun createFunction(rootRouteGraph: NavGraphContainerDeclaration): FunSpec {
        return FunSpec.builder(rootRouteGraph.name)
            .apply {
                receiver(rootRouteGraph.receiverType)
                addModifiers(rootRouteGraph.modifiers)
                rootRouteGraph.parameters.forEach {
                    when (it.type) {
                        is FunctionParameterType.Path -> Unit
                        is FunctionParameterType.Query -> Unit
                        FunctionParameterType.Back,
                        FunctionParameterType.Navigate,
                        FunctionParameterType.Custom,
                        -> {
                            addParameter(it.name, it.typeName)
                        }
                    }
                }
                rootRouteGraph.links.forEach { routeGraph ->
                    addRouteGraphFunction(routeGraph)
                }
            }.build()
    }

    private fun FunSpec.Builder.addRouteGraphFunction(routeGraph: NavGraphDestinationLinkDeclaration) {
        addStatement("%L(", routeGraph.name)
        addCode(
            buildCodeBlock {
                withIndent {
                    routeGraph.parameters.forEach {
                        when (it.type) {
                            is FunctionParameterType.Path -> Unit
                            is FunctionParameterType.Query -> Unit
                            FunctionParameterType.Back,
                            FunctionParameterType.Navigate,
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
    }
}
