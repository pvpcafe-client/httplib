plugins {
    id("java")
}

group = "cat.psychward.pvpcafe"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}