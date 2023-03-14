package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toKModifier
import io.github.seiko.precompose.code.routeBuilderType

internal class RootRouteGraphDeclaration(
    val packageName: String,
    val name: String,
    val receiverType: TypeName,
    val modifiers: List<KModifier>,
    val parameters: List<FunctionParameter>,
    val routeGraphs: Sequence<RouteGraphDeclaration>,
) {
    companion object
}

internal fun RootRouteGraphDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
    routeGraphs: Sequence<RouteGraphDeclaration>,
): RootRouteGraphDeclaration {
    return RootRouteGraphDeclaration(
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        receiverType = routeBuilderType,
        modifiers = ksFunction.modifiers.mapNotNull {
            if (it.name == KModifier.EXPECT.name) KModifier.ACTUAL
            else it.toKModifier()
        },
        parameters = ksFunction.parameters.map {
            FunctionParameter.of(it)
        },
        routeGraphs = routeGraphs,
    )
}
