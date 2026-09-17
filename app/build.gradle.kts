import java.io.File
import java.util.Properties
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(localProperties::load)
}

fun localString(name: String): String = localProperties.getProperty(name).orEmpty()

android {
    namespace = "com.guessroll"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.guessroll"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localString("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localString("SUPABASE_ANON_KEY")}\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.compose.cloudy)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.zxing.core)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

tasks.withType<Test>().configureEach {
    if (name == "testDebugUnitTest") {
        // The project path contains Cyrillic characters. The Android build is allowed
        // through android.overridePathCheck, but Gradle's JUnit worker can still fail
        // to load test classes from that path on Windows. Copy tests to an ASCII temp
        // directory right before execution.
        doFirst {
            val javaTestClasses = layout.buildDirectory.dir(
                "intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes",
            ).get().asFile
            val kotlinTestClasses = layout.buildDirectory.dir(
                "intermediates/built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes",
            ).get().asFile
            val kotlinMainClasses = layout.buildDirectory.dir(
                "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
            ).get().asFile
            val asciiTestClasses = File(System.getProperty("java.io.tmpdir"), "guessroll-debug-unit-test-classes")
            val asciiRuntimeClasses = File(System.getProperty("java.io.tmpdir"), "guessroll-debug-runtime-classes")

            delete(asciiTestClasses)
            delete(asciiRuntimeClasses)
            copy {
                from(javaTestClasses)
                from(kotlinTestClasses)
                into(asciiTestClasses)
            }
            copy {
                from(kotlinMainClasses)
                into(asciiRuntimeClasses)
            }

            testClassesDirs = files(asciiTestClasses)
            classpath = files(asciiTestClasses, asciiRuntimeClasses, classpath)
        }
    }
}
