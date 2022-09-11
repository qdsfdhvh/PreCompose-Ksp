plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm("desktop")
    ios()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/route")
            dependencies {
                api(projects.precomposeAnnotation)
                api("moe.tlaster:precompose:1.3.3")
                api(compose.material)
            }
        }
    }
}
