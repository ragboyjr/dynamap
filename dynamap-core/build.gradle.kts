import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    id("maven-publish")
    alias(libs.plugins.maven.central.publish)
    alias(libs.plugins.ktlint)

    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.aws.dynamodb)
    testImplementation(libs.kotlin.test.junit5)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        outputs.upToDateWhen { false }
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

publishing {
    publications {
        create<MavenPublication>("MavenJava") {
            groupId = "io.github.ragboyjr.dynamap"
            artifactId = "dynamap-core"
            version = project.findProperty("lib.version") as String?

            from(components["java"])

            pom {
                name = "Dynamap"
                description =
                    "Library to serialize and deserialize documents from DynamoDB using kotlinx.serialization."
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
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["MavenJava"])
}

mavenCentral {
    authToken = project.findProperty("maven_central.publish_token") as String?

    publishingType = "USER_MANAGED"

    maxWait = 120
}
