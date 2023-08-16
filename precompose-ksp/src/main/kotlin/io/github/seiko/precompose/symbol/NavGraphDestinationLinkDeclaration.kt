package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.seiko.precompose.annotation.Ignore
import io.github.seiko.precompose.code.routeBuilderType

internal data class NavGraphDestinationLinkDeclaration(
    val packageName: String,
    val name: String,
    val receiver: TypeName,
    val parameters: List<FunctionParameter>,
    val containingFile: KSFile?,
) {
    companion object
}

@OptIn(KspExperimental::class)
internal fun NavGraphDestinationLinkDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
): NavGraphDestinationLinkDeclaration {
    return NavGraphDestinationLinkDeclaration(
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        receiver = ksFunction.extensionReceiver?.toTypeName() ?: routeBuilderType,
        parameters = ksFunction.parameters
            .filterNot { it.isAnnotationPresent(Ignore::class) }
            .map { FunctionParameter.of(it) },
        containingFile = ksFunction.containingFile,
    )
}
