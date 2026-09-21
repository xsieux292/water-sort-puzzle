plugins {
    id("java")
    id("application")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.12"
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
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
    }
}

application {
    mainClass.set("watersort.Main")
}
