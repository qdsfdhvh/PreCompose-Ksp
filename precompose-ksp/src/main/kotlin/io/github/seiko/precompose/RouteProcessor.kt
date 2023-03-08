package io.github.seiko.precompose

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.seiko.precompose.annotation.Route

internal class RouteProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    companion object {
        private val ROUTE_ANNOTATION_NAME =
            requireNotNull(Route::class.qualifiedName) { "Can not get qualifiedName for Route" }
    }

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val symbols = resolver
            .getSymbolsWithAnnotation(ROUTE_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
        symbols
            .forEach { it.accept(RouteVisitor(), symbols.toList()) }
        return emptyList()
    }

    inner class RouteVisitor : KSEmptyVisitor<List<KSClassDeclaration>, Unit>() {
        @OptIn(KspExperimental::class)
        override fun defaultHandler(node: KSNode, data: List<KSClassDeclaration>) {
            if (node !is KSClassDeclaration) {
                throw IllegalArgumentException("Expected KSClassDeclaration, got ${node::class.qualifiedName}")
            }

            val annotation = node.getAnnotationsByType(Route::class).first()
            val schema = annotation.schema
            val packageName = annotation.packageName.takeIf { it.isNotEmpty() }
                ?: node.packageName.asString()
            val className = node.qualifiedName?.getShortName() ?: "<ERROR>"
            val route = generateRoute(declaration = node)
                .takeIf {
                    it is NestedRouteDefinition
                }?.let {
                PrefixRouteDefinition(
                    schema = schema,
                    child = it as NestedRouteDefinition,
                    className = className,
                )
            }
                ?: throw IllegalArgumentException("Expected NestedRouteDefinition, got ${node::class.qualifiedName}")

            val dependencies = Dependencies(
                true,
                *(data.mapNotNull { it.containingFile } + listOfNotNull(node.containingFile)).toTypedArray(),
            )
            generateFile(
                dependencies,
                packageName,
                className,
                route.generateRoute(),
            )
        }

        private fun generateFile(
            dependencies: Dependencies,
            packageName: String,
            className: String,
            route: Taggable,
        ) {
            FileSpec.builder(packageName, className)
                .apply {
                    when (route) {
                        is TypeSpec -> addType(route)
                        is FunSpec -> addFunction(route)
                        is PropertySpec -> addProperty(route)
                    }
                }
                .build()
                .writeTo(codeGenerator, dependencies)
        }

        private fun generateRoute(
            declaration: KSDeclaration,
            parent: RouteDefinition? = null,
        ): RouteDefinition {
            val name = declaration.simpleName.getShortName()
            return when (declaration) {
                is KSClassDeclaration -> {
                    if (declaration.declarations.any { it is KSFunctionDeclaration && it.simpleName.getShortName() == "invoke" }) {
                        ParameterRouteDefinition(
                            name,
                            parent,
                        ).also { definition ->
                            definition.childRoute.addAll(
                                declaration.declarations
                                    .filter { it.simpleName.getShortName() != "<init>" }
                                    .map { generateRoute(it, definition) },
                            )
                        }
                    } else {
                        NestedRouteDefinition(
                            name = name,
                            parent = parent,
                        ).also { nestedRouteDefinition ->
                            nestedRouteDefinition.childRoute.addAll(
                                declaration.declarations
                                    .filter { it.simpleName.getShortName() != "<init>" }
                                    .map { generateRoute(it, nestedRouteDefinition) },
                            )
                        }
                    }
                }
                is KSPropertyDeclaration -> {
                    val isConst = declaration.modifiers.contains(Modifier.CONST)
                    ConstRouteDefinition(name, parent, isConst)
                }
                is KSFunctionDeclaration -> {
                    FunctionRouteDefinition(
                        name = name,
                        parent = parent,
                        parameters = declaration.parameters.map {
                            val parameterName = it.name?.getShortName() ?: "_"
                            val parameterType = it.type.toTypeName()
                            RouteParameter(
                                name = parameterName,
                                type = parameterType,
                                parameter = it,
                            )
                        },
                    )
                }
                else -> throw NotImplementedError()
            }
        }
    }
}
