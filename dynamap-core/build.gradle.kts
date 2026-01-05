plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.aws.dynamodb)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

    coordinates("io.github.ragboyjr.dynamap", "dynamap-core", project.findProperty("lib.version") as String?)

    pom {
        name = "Dynamap"
        description = "Library to serialize and deserialize documents from DynamoDB using kotlinx.serialization."
        url = "https://github.com/ragboyjr/dynamap"

        licenses {
            license {
                name = "GPL-v3.0"
                url = "http://www.gnu.org/licenses/gpl-3.0.txt"
            }
        }

        developers {
            developer {
                id = "diegofer"
                name = "Diego Fernandez"
                email = "diego@diegofer.com"
            }
            developer {
                id = "ragboyjr"
                name = "RJ Garcia"
                email = "ragboyjr@icloud.com"
            }
        }

        scm {
            connection = "scm:git:git://github.com/ragboyjr/dynamap.git"
            developerConnection = "scm:git:ssh://github.com:ragboyjr/dynamap.git"
            url = "https://github.com/ragboyjr/dynamap"
        }
    }
}

kotlin {
    explicitApi()
}
