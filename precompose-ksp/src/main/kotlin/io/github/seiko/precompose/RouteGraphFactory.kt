package io.github.seiko.precompose

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import io.github.seiko.precompose.debug.TimeMeasure
import io.github.seiko.precompose.destination.NavGraphContainerFileSpecFactory
import io.github.seiko.precompose.destination.NavGraphContainerGenerator
import io.github.seiko.precompose.destination.NavGraphDestinationFileSpecFactory
import io.github.seiko.precompose.destination.NavGraphDestinationGenerator
import io.github.seiko.precompose.file.FileGenerator

class RouteGraphFactory internal constructor(
    private val environment: SymbolProcessorEnvironment,
    private val options: RouteGraphOptions,
) {
    constructor(environment: SymbolProcessorEnvironment) : this(
        environment = environment,
        options = RouteGraphOptions.of(environment.options),
    )

    private val timeMeasure by lazy {
        TimeMeasure(
            options.measureDuration,
            environment.logger,
        )
    }

    private val fileGenerator by lazy {
        FileGenerator(environment.codeGenerator)
    }

    private fun createNavGraphDestinationGenerator(): NavGraphDestinationGenerator {
        return NavGraphDestinationGenerator(
            logger = environment.logger,
            fileGenerator = fileGenerator,
            sceneFileSpecFactory = NavGraphDestinationFileSpecFactory(
                packageName = options.routeGraphPackageName,
            ),
        )
    }

    private fun createNavGraphContainerGenerator(): NavGraphContainerGenerator {
        return NavGraphContainerGenerator(
            logger = environment.logger,
            packageName = options.routeGraphPackageName,
            fileGenerator = fileGenerator,
            rootRouteGraphFileSpecFactory = NavGraphContainerFileSpecFactory(),
        )
    }

    internal fun createProcessor(): RouteGraphProcessor {
        return RouteGraphProcessor(
            navGraphDestinationGenerator = createNavGraphDestinationGenerator(),
            navGraphContainerGenerator = createNavGraphContainerGenerator(),
            timeMeasure = timeMeasure,
        )
    }
}
