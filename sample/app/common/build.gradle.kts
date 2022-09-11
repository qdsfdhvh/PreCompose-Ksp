plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm("desktop")
    ios()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.sample.core)
                implementation(projects.sample.feature.social)
                implementation(projects.sample.feature.wallet)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.precomposeKsp)
    add("kspDesktop", projects.precomposeKsp)
    add("kspIosX64", projects.precomposeKsp)
    add("kspIosArm64", projects.precomposeKsp)
}

