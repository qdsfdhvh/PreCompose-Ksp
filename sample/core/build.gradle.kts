@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")
    ios()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/route")
            dependencies {
                api(projects.precomposeAnnotation)
                api(libs.precompose)
                api(compose.material)
            }
        }
    }
}
