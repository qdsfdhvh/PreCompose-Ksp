package io.github.seiko.precompose.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Route(
    val schema: String = "",
    val packageName: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RouteGraph

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RootRouteGraph

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class NavGraphDestination(
    val route: String,
    // [] not support for js
    val deepLink: Array<String> = emptyArray(),
    val packageName: String = "",
    val functionName: String = "scene",
    // val generatedFunctionName: String = "generatedRoute",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path(
    val name: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Query(
    val name: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Navigate

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Back

// DON'T USE, Just for ksp

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Meta(val metadata: String)
