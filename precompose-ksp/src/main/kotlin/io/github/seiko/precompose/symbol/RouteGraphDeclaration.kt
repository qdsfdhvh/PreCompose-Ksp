package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.seiko.precompose.code.routeBuilderType

internal data class RouteGraphDeclaration(
    val packageName: String,
    val name: String,
    val receiver: TypeName,
    val parameters: List<FunctionParameter>,
    val scenes: Sequence<SceneDeclaration>,
) {
    companion object
}

internal fun RouteGraphDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
): RouteGraphDeclaration {
    return RouteGraphDeclaration(
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        receiver = ksFunction.extensionReceiver?.toTypeName() ?: routeBuilderType,
        parameters = ksFunction.parameters.map { FunctionParameter.of(it) },
        scenes = emptySequence(),
    )
}

internal fun RouteGraphDeclaration.Companion.of(
    name: String,
    scenes: Sequence<SceneDeclaration>,
): RouteGraphDeclaration {
    val parameters = scenes
        .flatMap { it.parameters }
        .distinct()
        .toList()
    return RouteGraphDeclaration(
        packageName = "",
        name = name,
        receiver = routeBuilderType,
        parameters = parameters,
        scenes = scenes,
    )
}
