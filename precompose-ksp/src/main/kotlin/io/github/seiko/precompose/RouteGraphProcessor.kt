package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.withIndent
import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.GeneratedRoute
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.annotation.Navigate
import io.github.seiko.precompose.annotation.Path
import io.github.seiko.precompose.annotation.Query

@OptIn(KspExperimental::class)
internal class RouteGraphProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val destinations = resolver
            .getSymbolsWithAnnotation(
                NavGraphDestination::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for RouteGraphDestination")
            ).filterIsInstance<KSFunctionDeclaration>()

        val generates = resolver
            .getSymbolsWithAnnotation(
                GeneratedRoute::class.qualifiedName
                    ?: throw CloneNotSupportedException("Can not get qualifiedName for GeneratedRoute")
            ).filterIsInstance<KSFunctionDeclaration>()

        val ret = generates.filter { !it.validate() }.toList()
        generates.filter { it.validate() }
            .forEach { generateRoute(it, destinations.toList()) }
        return ret
    }

    private fun generateRoute(
        functionDeclaration: KSFunctionDeclaration,
        destinations: List<KSFunctionDeclaration>,
    ) {
        val packageName = functionDeclaration.packageName.asString()
        val functionName = functionDeclaration.qualifiedName?.getShortName() ?: "<ERROR>"

        val functionBuilder = FunSpec.builder(functionName)
            .receiver(routeBuilderType)

        if (functionDeclaration.modifiers.isNotEmpty()) {
            functionBuilder.addModifiers(KModifier.ACTUAL)
            functionBuilder.addModifiers(
                functionDeclaration.modifiers
                    .filter { it.name != KModifier.EXPECT.name }
                    .mapNotNull { it.toKModifier() }
            )
        }
        val functionNames = NavigatorFunctionNames()
        functionDeclaration.parameters.forEach { parameter ->
            val name = parameter.name?.getShortName().orEmpty()
            val type = parameter.type.toTypeName()
            when {
                type == navControllerType -> functionNames.navigatorName = name
                parameter.isAnnotationPresent(Back::class) -> functionNames.onBackName = name
                parameter.isAnnotationPresent(Navigate::class) -> functionNames.onNavigateName = name
                else -> Unit
            }
            functionBuilder.addParameter(
                ParameterSpec.builder(name, type).build()
            )
        }
        require(functionNames.navigatorName.isNotEmpty()) {
            "not find navigator in ${functionDeclaration.packageName}.$functionName"
        }

        val fileBuilder = FileSpec.builder(packageName, functionName)
        destinations.forEach { destination ->
            generateDestination(
                fileBuilder = fileBuilder,
                functionBuilder = functionBuilder,
                destination = destination,
                functionNames = functionNames,
            )
        }

        fileBuilder.addFunction(functionBuilder.build())
            .build()
            .writeTo(
                codeGenerator,
                Dependencies(
                    true,
                    // *(destinations.mapNotNull { it.containingFile }).toTypedArray()
                )
            )
    }

    private fun generateDestination(
        fileBuilder: FileSpec.Builder,
        functionBuilder: FunSpec.Builder,
        destination: KSFunctionDeclaration,
        functionNames: NavigatorFunctionNames,
    ) {
        val annotation = destination.getAnnotationsByType(NavGraphDestination::class).first()

        functionBuilder.addStatement("%L(", annotation.functionName)
        functionBuilder.addCode(
            buildCodeBlock {
                withIndent {
                    addStatement(
                        "route = %S,",
                        annotation.route,
                    )
                    if (annotation.deepLink.isNotEmpty()) {
                        addStatement("deepLinks = listOf(")
                        withIndent {
                            annotation.deepLink.forEach {
                                addStatement("%S,", it)
                            }
                        }
                        addStatement("),")
                    }
                }
            }
        )
        functionBuilder.beginControlFlow(")")

        destination.parameters.forEach {
            if (it.isAnnotationPresent(Path::class)) {
                require(!it.type.resolve().isMarkedNullable)
            }
            if (it.isAnnotationPresent(Query::class)) {
                require(it.type.resolve().isMarkedNullable)
            }
            if (it.isAnnotationPresent(Path::class)) {
                val path = it.getAnnotationsByType(Path::class).first()
                functionBuilder.addStatement(
                    "val %L = it.%T<%T>(%S)!!",
                    it.name?.asString().orEmpty(),
                    pathType,
                    it.type.toTypeName(),
                    path.name,
                )
            } else if (it.isAnnotationPresent(Query::class)) {
                val query = it.getAnnotationsByType(Query::class).first()
                functionBuilder.addStatement(
                    "val %L = it.%T<%T>(%S)",
                    it.name?.asString().orEmpty(),
                    queryType,
                    it.type.toTypeName(),
                    query.name,
                )
            }
        }

        if (destination.packageName.asString() != fileBuilder.packageName) {
            fileBuilder.addImport(
                destination.packageName.asString(),
                destination.simpleName.asString(),
            )
        }
        functionBuilder.addStatement("%L(", destination)
        functionBuilder.addCode(
            buildCodeBlock {
                if (destination.parameters.isNotEmpty()) {
                    withIndent {
                        destination.parameters.forEach {
                            when {
                                it.type.toTypeName() == navControllerType -> {
                                    addStatement(
                                        "%N = %N,",
                                        it.name?.asString() ?: "",
                                        functionNames.navigatorName
                                    )
                                }
                                it.type.toTypeName() == navBackStackEntryType -> {
                                    addStatement(
                                        "%N = it,",
                                        it.name?.asString() ?: "",
                                    )
                                }
                                it.isAnnotationPresent(Query::class) || it.isAnnotationPresent(Path::class) -> {
                                    addStatement(
                                        "%N = %N,",
                                        it.name?.asString() ?: "",
                                        it.name?.asString() ?: ""
                                    )
                                }
                                it.isAnnotationPresent(Back::class) -> {
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
                                            functionNames.navigatorName
                                        )
                                    }
                                }
                                it.isAnnotationPresent(Navigate::class) -> {
                                    val type = it.type.resolve()
                                    require(type.isFunctionType)
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
                    }
                }
            }
        )
        functionBuilder.addStatement(")")

        functionBuilder.endControlFlow()
    }
}

private data class NavigatorFunctionNames(
    var navigatorName: String = "",
    var onBackName: String = "",
    var onNavigateName: String = "",
)
