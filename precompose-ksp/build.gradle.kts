import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish.base")
}

dependencies {
    implementation(projects.precomposeAnnotation)
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
}

@Suppress("UnstableApiUsage")
configure<MavenPublishBaseExtension> {
    configure(KotlinJvm())
}
