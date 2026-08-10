import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Replaces the old `composeOptions { kotlinCompilerExtensionVersion }` block.
    // Compose 1.9+ requires this plugin and ignores the extension version.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Backs @Serializable navigation routes, and the JSON report export later.
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.anhprgm.deviceinfo" // com.shin.bear.studio.apps.deviceinfo
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anhprgm.deviceinfo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    androidResources {
        // Strips the ~80 AndroidX locales the app does not ship strings for.
        localeFilters += listOf("en", "vi")
    }

    buildTypes {
        release {
            // material-icons-extended ships every Material icon; without R8 the
            // release APK carries all of them.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        // Current AndroidX bytecode is built for JVM 11+; 1.8 fails to inline it.
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    // Lets Room emit schema JSON so future migrations can be generated from it.
    arg("room.schemaLocation", "$projectDir/schemas")
}

/**
 * Fails the build on user-visible text hardcoded into a Composable.
 *
 * Android Lint's HardcodedText check only inspects XML — it cannot see a
 * literal inside a `Text(...)` call, and no off-the-shelf Compose lint rule
 * covers it. Crude, but it is the only thing that actually keeps the strings
 * in resources once new screens start landing.
 */
val checkHardcodedStrings by tasks.registering {
    group = "verification"
    description = "Fails if a Composable passes a string literal straight to Text()."

    val screenDir = layout.projectDirectory.dir("src/main/java/com/anhprgm/deviceinfo/ui")
    inputs.dir(screenDir)
    // No output file; the task is a pure assertion over its inputs.
    outputs.upToDateWhen { false }

    doLast {
        val offenders = screenDir.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> Regex("""Text\(\s*"""").containsMatchIn(line) }
                    .map { (index, line) -> "${file.name}:${index + 1}: ${line.trim()}" }
            }
            .toList()

        if (offenders.isNotEmpty()) {
            throw GradleException(
                "Hardcoded UI strings found — move them to strings.xml:\n" +
                    offenders.joinToString("\n")
            )
        }
    }
}

tasks.named("check") { dependsOn(checkHardcodedStrings) }

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.splashscreen)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Dependency injection — the data layer has to be reachable from
    // framework-instantiated components (worker, widget receiver, tile service),
    // none of which have an Activity in scope.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
