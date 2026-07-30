import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.lloppy.akta.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Akta"
            packageVersion = "1.0.0"

            macOS { iconFile.set(project.file("icon.icns")) }
            windows { iconFile.set(project.file("icon.ico")) }
            linux { iconFile.set(project.file("icon.png")) }
        }
    }
}
