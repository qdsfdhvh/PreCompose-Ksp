import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.application") version "7.2.2" apply false
    id("com.android.library") version "7.2.2" apply false
    kotlin("multiplatform") version "1.7.20" apply false
    id("com.google.devtools.ksp") version "1.7.20-1.0.7" apply false
    id("org.jetbrains.compose") version "1.2.0" apply false
    id("com.vanniktech.maven.publish") version "0.22.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    group = "io.github.qdsfdhvh"
    version = "1.0.2"

    plugins.withId("com.vanniktech.maven.publish.base") {
        @Suppress("UnstableApiUsage")
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
            signAllPublications()
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
