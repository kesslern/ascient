plugins {
    kotlin("jvm") version "1.3.10"
    java
    application
}

object Versions {
    const val exposed           = "0.12.1"
    const val flyway            = "5.2.1"
    const val kotlin            = "1.3.10"
    const val ktor              = "1.0.0"
    const val jackson           = "2.9.+"
    const val junit             = "5.1.+"
    const val jvm               =  1.8
    const val logback           = "1.2.+"
    const val postgres          = "42.2.5"
    const val postgresContainer = "1.10.1"
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
    compile("ch.qos.logback:logback-classic:${Versions.logback}")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-joda:${Versions.jackson}")
    compile("io.ktor:ktor-jackson:${Versions.ktor}")
    compile("io.ktor:ktor-server-netty:${Versions.ktor}")
    compile("org.flywaydb:flyway-core:${Versions.flyway}")
    compile("org.jetbrains.exposed:exposed:${Versions.exposed}")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    compile("org.postgresql:postgresql:${Versions.postgres}")

    testCompile("org.testcontainers:postgresql:${Versions.postgresContainer}")
    testCompile("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testCompile("io.ktor:ktor-server-test-host:${Versions.ktor}")
}

application {
    mainClassName = "us.kesslern.ascient.MainKt"
    group = "us.kesslern"
    applicationName = "ascient"

    // TODO: Make these configurable
    applicationDefaultJvmArgs = listOf(
        "-Ddatabase.connection=jdbc:postgresql://localhost:5432/postgres",
        "-Ddatabase.username=user",
        "-Ddatabase.password=pass"
    )
}

val test by tasks.getting(Test::class) {
    systemProperty("ascient.backend", System.getProperty("ascient.backend"))
}