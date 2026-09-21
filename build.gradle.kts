plugins {
    id("java")
    id("application")
    id("jacoco")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.graphics")
}

jacoco {
    toolVersion = "0.8.14"
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
    // `./gradlew run` เปิด Desktop GUI (JavaFX); CLI ยังใช้ได้ผ่าน `./gradlew runCli`
    mainClass.set("watersort.ui.WaterSortApp")
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics,ALL-UNNAMED", "-Xmx2g")
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat JAR containing all dependencies."
    manifest {
        attributes("Main-Class" to "watersort.Launcher")
    }
    archiveBaseName.set("WaterSortPuzzle")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.register<JavaExec>("runCli") {
    group = "application"
    description = "Runs the command-line interface (watersort.Main). Pass arguments with --args=\"--demo\"."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("watersort.Main")
    standardInput = System.`in`
}
