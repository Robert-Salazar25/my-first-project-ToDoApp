plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Añade KSP con versión explícita
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}