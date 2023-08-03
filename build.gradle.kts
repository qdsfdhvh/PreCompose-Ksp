import com.vanniktech.maven.publish.SonatypeHost

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.jb) apply false
    alias(libs.plugins.publish)
    alias(libs.plugins.spotless)
}

allprojects {
    group = "io.github.qdsfdhvh"
    version = "1.0.7"

    plugins.withId("com.vanniktech.maven.publish.base") {
        mavenPublishing {
            publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
            signAllPublications()
            @Suppress("UnstableApiUsage")
            pom {
                description.set("A route compiler for PreCompose (KSP).")
                name.set(project.name)
                url.set("https://github.com/qdsfdhvh/PreCompose-Ksp")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Seiko")
                        name.set("SeikoDes")
                        email.set("seiko_des@outlook.com")
                    }
                }
                scm {
                    url.set("https://github.com/qdsfdhvh/PreCompose-Ksp")
                    connection.set("scm:git:git://github.com/qdsfdhvh/PreCompose-Ksp.git")
                    developerConnection.set("scm:git:git://github.com/qdsfdhvh/PreCompose-Ksp.git")
                }
            }
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/")
        ktlint(libs.versions.ktlint.get())
    }
}
