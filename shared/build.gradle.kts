import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// JavaFX (для desktop-аудио) распространяется с классификатором под ОС/архитектуру.
val javafxVersion = "21.0.5"
val javafxClassifier: String = run {
    val osName = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val aarch = arch.contains("aarch64") || arch.contains("arm")
    when {
        osName.contains("mac") && aarch -> "mac-aarch64"
        osName.contains("mac") -> "mac"
        osName.contains("win") -> "win"
        aarch -> "linux-aarch64"
        else -> "linux"
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    jvm("desktop")

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.lloppy.audiolessons.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.session)
            implementation(libs.media3.transformer)
            implementation(libs.media3.effect)
            implementation(libs.media3.common)
        }
        val desktopTest by getting
        desktopTest.dependencies {
            implementation(kotlin("test"))
            implementation(compose.desktop.currentOs)
        }
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.coroutines.swing)
            implementation(libs.pdfbox)
            implementation("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
            implementation("org.openjfx:javafx-media:$javafxVersion:$javafxClassifier")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.material.icons.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
