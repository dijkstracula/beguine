plugins {
    id("java")
}

group = "net.dijkstracula.melina"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("tools.aqua:z3-turnkey:4.12.2")
    implementation("io.vavr:vavr:1.0.0-alpha-4")
}

tasks.test {
    useJUnitPlatform()
}
