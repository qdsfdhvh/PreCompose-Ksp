@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")
    ios()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/route")
            dependencies {
                implementation(projects.sample.core)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("build/generated/ksp/desktop/desktopMain")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.precomposeKsp)
    add("kspDesktop", projects.precomposeKsp)
    add("kspIosX64", projects.precomposeKsp)
    add("kspIosArm64", projects.precomposeKsp)
}

ksp {
    arg("measureDuration", "false")
    arg("isGenerateContainer", "false")
}
