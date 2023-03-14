package io.github.seiko.precompose.destination

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.symbol.FunctionParameterType
import io.github.seiko.precompose.symbol.RootRouteGraphDeclaration
import io.github.seiko.precompose.symbol.RouteGraphDeclaration

internal class RootRouteGraphFileSpecFactory {

    fun create(rootRouteGraph: RootRouteGraphDeclaration): FileSpec {
        return FileSpec.builder(
            rootRouteGraph.packageName,
            rootRouteGraph.name,
        ).apply {
            rootRouteGraph.routeGraphs.forEach { routeGraph ->
                if (routeGraph.packageName.isNotEmpty()) {
                    addImport(routeGraph.packageName, routeGraph.name)
                }
            }
            addFunction(createFunction(rootRouteGraph))
        }.build()
    }

    private fun createFunction(rootRouteGraph: RootRouteGraphDeclaration): FunSpec {
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
                        FunctionParameterType.Custom -> {
                            addParameter(it.name, it.typeName)
                        }
                    }
                }
                rootRouteGraph.routeGraphs.forEach { routeGraph ->
                    addRouteGraphFunction(routeGraph)
                }
            }.build()
    }

    private fun FunSpec.Builder.addRouteGraphFunction(routeGraph: RouteGraphDeclaration) {
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
    }
}
