package io.github.seiko.precompose.destination

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.symbol.FunctionParameterType
import io.github.seiko.precompose.symbol.NavGraphContainerDeclaration
import io.github.seiko.precompose.symbol.NavGraphDestinationLinkDeclaration

internal class NavGraphContainerFileSpecFactory {

    fun create(container: NavGraphContainerDeclaration): FileSpec {
        return FileSpec.builder(
            container.packageName,
            container.name,
        ).apply {
            container.links.forEach { routeGraph ->
                if (routeGraph.packageName.isNotEmpty()) {
                    addImport(routeGraph.packageName, routeGraph.name)
                }
            }
            addFunction(createFunction(container))
        }.build()
    }

    private fun createFunction(container: NavGraphContainerDeclaration): FunSpec {
        return FunSpec.builder(container.name)
            .apply {
                receiver(container.receiverType)
                addModifiers(container.modifiers)
                container.parameters.forEach {
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
                container.links.forEach { routeGraph ->
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
