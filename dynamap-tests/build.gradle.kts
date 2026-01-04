import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

dependencies {
    // Depend on the published API of dynamap-core
    testImplementation(project(":dynamap-core"))

    testImplementation(libs.kotlinx.serialization.core)
    testImplementation(libs.aws.dynamodb)
    testImplementation(libs.kotlin.test.junit5)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        outputs.upToDateWhen { false }
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
