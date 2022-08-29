plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp").version("1.7.10-1.0.6")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.precomposeAnnotation)
                implementation("moe.tlaster:precompose:1.3.3")
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
        }
    }
}

dependencies {
    // add("kspCommonMainMetadata", projects.ktorFitKsp)
    // add("kspJvm", projects.ktorFitKsp)
    // add("kspJvmTest", projects.ktorFitKsp)
    // add("kspLinuxX64", projects.ktorFitKsp)
    // add("kspMacosX64", projects.ktorFitKsp)
    // add("kspWatchosX64", projects.ktorFitKsp)
    // add("kspIosX64", projects.ktorFitKsp)
    // add("kspJs",projects.ktorFitKsp)
}
