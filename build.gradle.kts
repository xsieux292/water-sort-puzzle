plugins {
    id("java")
    id("application")
}

group = "watersort"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.bytedeco:javacv:1.5.10")
    implementation("org.bytedeco:opencv:4.9.0-1.5.10")
    implementation("org.bytedeco:opencv:4.9.0-1.5.10:macosx-arm64")
    implementation("org.bytedeco:openblas:0.3.26-1.5.10:macosx-arm64")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

application {
    mainClass.set("watersort.Main")
}
