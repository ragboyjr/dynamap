rootProject.name = "Codanbaru - DynaMap"

include("dynamap-core")
include("dynamap-tests")

dependencyResolutionManagement {
    versionCatalogs {
        // we're intentionally just overriding some versions at runtime for ci testing matrix
        create("libs") {
            providers.gradleProperty("kotlin.version.override").orNull?.let {
                version("kotlin", it)
            }

            providers.gradleProperty("kotlinx.serialization.version.override").orNull?.let {
                version("kotlinx-serialization", it)
            }
        }
    }
}
