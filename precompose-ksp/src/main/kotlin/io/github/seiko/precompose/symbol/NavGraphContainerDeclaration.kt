package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toKModifier
import io.github.seiko.precompose.annotation.Ignore
import io.github.seiko.precompose.code.routeBuilderType

internal class NavGraphContainerDeclaration(
    val packageName: String,
    val name: String,
    val receiverType: TypeName,
    val modifiers: List<KModifier>,
    val parameters: List<FunctionParameter>,
    val links: Sequence<NavGraphDestinationLinkDeclaration>,
) {
    companion object
}

@OptIn(KspExperimental::class)
internal fun NavGraphContainerDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
    links: Sequence<NavGraphDestinationLinkDeclaration>,
): NavGraphContainerDeclaration {
    return NavGraphContainerDeclaration(
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        receiverType = routeBuilderType,
        modifiers = ksFunction.modifiers.mapNotNull {
            if (it.name == KModifier.EXPECT.name) {
                KModifier.ACTUAL
            } else it.toKModifier()
        },
        parameters = ksFunction.parameters
            .filterNot { it.isAnnotationPresent(Ignore::class) }
            .map { FunctionParameter.of(it) },
        links = links,
    )
}
