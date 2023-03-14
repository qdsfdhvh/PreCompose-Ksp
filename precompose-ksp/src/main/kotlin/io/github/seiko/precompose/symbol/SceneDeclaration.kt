package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.seiko.precompose.annotation.NavGraphDestination

internal data class SceneDeclaration(
    val route: String,
    val deepLinks: List<String>,
    val packageName: String,
    val name: String,
    val scenePackageName: String,
    val sceneName: String,
    val parameters: List<FunctionParameter>,
    val containingFile: KSFile?,
) {
    companion object
}

@OptIn(KspExperimental::class)
internal fun SceneDeclaration.Companion.of(
    ksFunction: KSFunctionDeclaration,
): SceneDeclaration {
    val annotation = ksFunction.getAnnotationsByType(NavGraphDestination::class).first()
    return SceneDeclaration(
        route = annotation.route,
        deepLinks = annotation.deepLink.toList(),
        packageName = ksFunction.packageName.asString(),
        name = ksFunction.simpleName.asString(),
        scenePackageName = annotation.packageName,
        sceneName = annotation.functionName,
        parameters = ksFunction.parameters.map { FunctionParameter.of(it) },
        containingFile = ksFunction.containingFile,
    )
}
