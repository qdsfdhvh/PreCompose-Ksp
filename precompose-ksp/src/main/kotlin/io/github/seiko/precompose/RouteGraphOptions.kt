package io.github.seiko.precompose

internal class RouteGraphOptions(
    val measureDuration: Boolean = false,
    val routeGraphPackageName: String? = null,
) {
    companion object {
        fun of(options: Map<String, String>): RouteGraphOptions {
            return RouteGraphOptions(
                measureDuration = options["measureDuration"].toBoolean(),
                routeGraphPackageName = options["routeGraphPackageName"],
            )
        }
    }
}
