package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.TypeName
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.code.routeBuilderType
import kotlin.reflect.KClass

internal data class NavGraphDestinationDeclaration(
    val route: String,
    val deepLinks: List<String>,
    val fileName: String,
    val packageName: String,
    val name: String,
    val scenePackageName: String,
    val sceneName: String,
    val receiver: TypeName,
    val parameters: List<FunctionParameter>,
    val containingFile: KSFile?,
) {
    companion object
}

@OptIn(KspExperimental::class)
internal fun NavGraphDestinationDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
): NavGraphDestinationDeclaration {
    val annotation = ksFunction.getAnnotationsByType(NavGraphDestination::class).first()
    val fixAnnotation = ksFunction.getFirstKSAnnotationsByType(NavGraphDestination::class)
    return NavGraphDestinationDeclaration(
        route = annotation.route,
        deepLinks = fixAnnotation.getValue<List<String>>("deepLinks") ?: emptyList(),
        fileName = "${ksFunction.packageName.asString()}.${ksFunction.simpleName.asString()}"
            .replace(".", "_")
            .replace("`", ""),
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        scenePackageName = annotation.packageName,
        sceneName = annotation.functionName,
        receiver = routeBuilderType,
        parameters = ksFunction.parameters.map { FunctionParameter.of(it) },
        containingFile = ksFunction.containingFile,
    )
}

@Suppress("UNCHECKED_CAST")
private fun <T> KSAnnotation.getValue(name: String): T? {
    println(arguments.joinToString { it.name?.asString().orEmpty() + "|" + it.value })
    return arguments.firstOrNull { it.name?.asString() == name }
        ?.value as? T
}

fun <T : Annotation> KSAnnotated.getFirstKSAnnotationsByType(annotationKClass: KClass<T>): KSAnnotation {
    return this.annotations.filter {
        it.shortName.getShortName() == annotationKClass.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == annotationKClass.qualifiedName
    }.first()
}
