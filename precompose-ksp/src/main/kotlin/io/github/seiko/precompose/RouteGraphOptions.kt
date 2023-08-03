package io.github.seiko.precompose

internal class RouteGraphOptions(
    val measureDuration: Boolean = false,
    val routeGraphPackageName: String? = null,
    val isGenerateContainer: Boolean = true,
) {
    companion object {
        fun of(options: Map<String, String>): RouteGraphOptions {
            return RouteGraphOptions(
                measureDuration = options["measureDuration"]?.toBoolean() ?: false,
                routeGraphPackageName = options["routeGraphPackageName"],
                isGenerateContainer = options["isGenerateContainer"]?.toBoolean() ?: true,
            )
        }
    }
}
