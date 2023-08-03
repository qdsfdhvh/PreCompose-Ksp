# Precompose-Ksp
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.qdsfdhvh/precompose-annotation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.qdsfdhvh/precompose-annotation)

A route compiler for [PreCompose](https://github.com/Tlaster/PreCompose) (KSP).

## Setup

Add the dependency in your common module's commonMain sourceSet

```diff title="build.gradle.kts"
plugins {
    // ...
+    id("com.google.devtools.ksp")
}

kotlin {
    android()
    ios()
    // ...

    sourceSets {
        val commonMain by getting {
            dependencies {
+                api("io.github.qdsfdhvh:precompose-annotation:1.0.9")
            }
        }
    }
}

dependencies {
+    kspAll("io.github.qdsfdhvh:precompose-ksp:1.0.9")
}

fun DependencyHandlerScope.kspAll(dependencyNotation: Any) {
    // add("kspCommonMainMetadata", dependencyNotation)
    add("kspAndroid", dependencyNotation)
    add("kspIosX64", dependencyNotation)
    add("kspIosArm64", dependencyNotation)
    add("kspIosSimulatorArm64", dependencyNotation)
    // ...
}
```


## How to Use

bind route like this:

```kotlin
@NavGraphDestination("Screen/Test")
@Compose
fun TestScreen() {
}

@NavGraphDestination("Dialog/Test", functionName = "dialog")
@Compose
fun TestDialog() {
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
@NavGraphContainer
expect fun RouteBuilder.generateRoute(navigator: Navigator)
```
