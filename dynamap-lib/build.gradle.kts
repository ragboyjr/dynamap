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
            groupId = "com.codanbaru.kotlin"
            artifactId = "dynamap"
            version = project.findProperty("lib.version") as String? ?: "0"

            from(components["java"])

            pom {
                name = "Dynamap"
                description =
                    "Library to serialize and deserialize documents from DynamoDB using kotlinx.serialization."
                url = "https://github.com/codanbaru/dynamap"

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
                }

                scm {
                    connection = "scm:git:git://github.com/codanbaru/dynamap.git"
                    developerConnection = "scm:git:ssh://github.com:codanbaru/dynamap.git"
                    url = "https://github.com/codanbaru/dynamap"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["MavenJava"])
}

mavenCentral {
    authToken = project.findProperty("sonartype.central.token") as String? ?: ""

    publishingType = "USER_MANAGED"

    maxWait = 120
}
