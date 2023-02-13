package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.Navigate

@OptIn(KspExperimental::class)
internal fun FunSpec.Builder.addParameterAndReturnNavigatorNames(
    parameters: List<KSValueParameter>,
): NavigatorFunctionNames {
    val functionNames = NavigatorFunctionNames()
    parameters.forEach { parameter ->
        val name = parameter.name?.getShortName().orEmpty()
        val type = parameter.type.toTypeName()
        when {
            type == navControllerType -> functionNames.navigatorName = name
            parameter.isAnnotationPresent(Back::class) -> functionNames.onBackName = name
            parameter.isAnnotationPresent(Navigate::class) -> functionNames.onNavigateName = name
        }
        addParameter(
            ParameterSpec.builder(name, type).build(),
        )
    }
    return functionNames
}

internal fun FunSpec.Builder.addNavigateParameters(
    fileBuilder: FileSpec.Builder,
    functionNames: NavigatorFunctionNames,
    functionDeclaration: KSFunctionDeclaration,
    allowBackStackEntry: Boolean = true,
) {
    if (functionDeclaration.packageName.asString() != fileBuilder.packageName) {
        fileBuilder.addImport(
            functionDeclaration.packageName.asString(),
            functionDeclaration.simpleName.asString(),
        )
    }
    addStatement("%L(", functionDeclaration)
    addNavigateParameters(
        functionNames = functionNames,
        parameters = functionDeclaration.parameters,
        allowBackStackEntry = allowBackStackEntry,
    )
    addStatement(")")
}

@OptIn(KspExperimental::class)
private fun FunSpec.Builder.addNavigateParameters(
    functionNames: NavigatorFunctionNames,
    parameters: List<KSValueParameter>,
    allowBackStackEntry: Boolean = true,
) {
    if (parameters.isEmpty()) return
    addCode(
        buildCodeBlock {
            withIndent {
                parameters.forEach {
                    when {
                        allowBackStackEntry && it.type.toTypeName() == navBackStackEntryType -> {
                            addStatement(
                                "%N = it,",
                                it.name?.asString() ?: "",
                            )
                        }

                        functionNames.navigatorName.isNotEmpty() -> {
                            when {
                                it.type.toTypeName() == navControllerType -> {
                                    addStatement(
                                        "%N = %N,",
                                        it.name?.asString() ?: "",
                                        functionNames.navigatorName,
                                    )
                                }

                                it.isAnnotationPresent(Back::class) -> {
                                    require(it.type.resolve().isFunctionType)
                                    if (functionNames.onBackName.isNotEmpty()) {
                                        addStatement(
                                            "%N = %N,",
                                            it.name?.asString() ?: "",
                                            functionNames.onBackName,
                                        )
                                    } else {
                                        addStatement(
                                            "%N = { %N.popBackStack() },",
                                            it.name?.asString() ?: "",
                                            functionNames.navigatorName,
                                        )
                                    }
                                }

                                it.isAnnotationPresent(Navigate::class) -> {
                                    require(it.type.resolve().isFunctionType)
                                    if (functionNames.onNavigateName.isNotEmpty()) {
                                        addStatement(
                                            "%N = %N,",
                                            it.name?.asString() ?: "",
                                            functionNames.onNavigateName,
                                        )
                                    } else {
                                        addStatement(
                                            "%N = { uri -> %N.navigate(uri) },",
                                            it.name?.asString() ?: "",
                                            functionNames.navigatorName,
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            addStatement(
                                "%N = %N,",
                                it.name?.asString() ?: "",
                                it.name?.asString() ?: "",
                            )
                        }
                    }
                }
            }
        },
    )
}

internal data class NavigatorFunctionNames(
    var onBackName: String = "",
    var onNavigateName: String = "",
    var navigatorName: String = "",
)
