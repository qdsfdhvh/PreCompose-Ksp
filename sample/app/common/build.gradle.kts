@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.sample.core)
                implementation(projects.sample.feature.social)
                implementation(projects.sample.feature.wallet)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("build/generated/ksp/desktop/desktopMain")
        }
    }
}

dependencies {
    add("kspDesktop", projects.precomposeKsp)
    add("kspIosX64", projects.precomposeKsp)
    add("kspIosArm64", projects.precomposeKsp)
    add("kspJs", projects.precomposeKsp)
}

ksp {
    arg("measureDuration", "false")
}
