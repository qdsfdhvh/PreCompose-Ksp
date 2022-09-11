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
            kotlin.srcDir("src/commonMain/route")
            kotlin.srcDir("src/build/generated/ksp/desktop")
            dependencies {
                implementation(projects.sample.core)
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
