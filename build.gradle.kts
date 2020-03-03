import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
}

repositories {
    jcenter()
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("com.squareup.retrofit2:retrofit:2.7.2")

    testImplementation("com.squareup.retrofit2:converter-scalars:2.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}