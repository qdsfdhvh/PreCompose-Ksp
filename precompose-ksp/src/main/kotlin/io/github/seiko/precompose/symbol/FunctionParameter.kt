package io.github.seiko.precompose.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.Navigate
import io.github.seiko.precompose.annotation.Path
import io.github.seiko.precompose.annotation.Query

internal data class FunctionParameter(
    val name: String,
    val typeName: TypeName,
    val type: FunctionParameterType,
) {
    companion object
}

sealed interface FunctionParameterType {
    data class Path(val name: String) : FunctionParameterType
    data class Query(val name: String) : FunctionParameterType
    object Back : FunctionParameterType
    object Navigate : FunctionParameterType
    object Custom : FunctionParameterType
}

@OptIn(KspExperimental::class)
internal fun FunctionParameter.Companion.of(
    ksValueParameter: KSValueParameter,
): FunctionParameter {
    val name = requireNotNull(ksValueParameter.name).asString()
    val type = when {
        ksValueParameter.isAnnotationPresent(Path::class) -> {
            require(!ksValueParameter.isNullable)
            val pathName = ksValueParameter.getAnnotationsByType(Path::class).first().name
            FunctionParameterType.Path(pathName.ifEmpty { name })
        }
        ksValueParameter.isAnnotationPresent(Query::class) -> {
            require(ksValueParameter.isNullable)
            val queryName = ksValueParameter.getAnnotationsByType(Query::class).first().name
            FunctionParameterType.Query(queryName.ifEmpty { name })
        }
        ksValueParameter.isAnnotationPresent(Back::class) -> {
            require(ksValueParameter.isFunctionType)
            FunctionParameterType.Back
        }
        ksValueParameter.isAnnotationPresent(Navigate::class) -> {
            require(ksValueParameter.isFunctionType)
            FunctionParameterType.Navigate
        }
        else -> {
            FunctionParameterType.Custom
        }
    }
    return FunctionParameter(
        name = name,
        typeName = ksValueParameter.type.toTypeName(),
        type = type,
    )
}

private val KSValueParameter.isNullable: Boolean
    get() = type.resolve().isMarkedNullable

private val KSValueParameter.isFunctionType: Boolean
    get() = type.resolve().isFunctionType
