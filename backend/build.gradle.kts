plugins {
    kotlin("jvm") version "1.3.10"
    java
    application
}

object Versions {
    const val exposed           = "0.12.1"
    const val flyway            = "5.2.4"
    const val kotlin            = "1.3.21"
    const val ktor              = "1.1.2"
    const val jackson           = "2.9.+"
    const val jbcrypt           = "0.4"
    const val junit             = "5.1.+"
    const val jvm               =  1.8
    const val kotlinLogging     = "1.6.22"
    const val logback           = "1.2.+"
    const val postgres          = "42.2.5"
    const val postgresContainer = "1.10.6"
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
    compile("io.ktor:ktor-auth:${Versions.ktor}")
    compile("io.ktor:ktor-jackson:${Versions.ktor}")
    compile("io.ktor:ktor-server-netty:${Versions.ktor}")
    compile("io.ktor:ktor-websockets:${Versions.ktor}")
    compile("org.flywaydb:flyway-core:${Versions.flyway}")
    compile("org.jetbrains.exposed:exposed:${Versions.exposed}")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    compile("org.mindrot:jbcrypt:${Versions.jbcrypt}")
    compile("org.postgresql:postgresql:${Versions.postgres}")
    compile("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")

    testCompile("org.testcontainers:postgresql:${Versions.postgresContainer}")
    testCompile("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testCompile("io.ktor:ktor-client-apache:${Versions.ktor}")
    testCompile("io.ktor:ktor-server-test-host:${Versions.ktor}")
}

application {
    mainClassName = "us.kesslern.ascient.MainKt"
    group = "us.kesslern"
    applicationName = "ascient"

    applicationDefaultJvmArgs = mapOf(
            "database.hostname" to "localhost",
            "database.port"     to "5432",
            "database.dbname"   to "postgres",
            "database.username" to "user",
            "database.password" to "pass",
            "ascient.port"      to "8080"
    ).map {
        it.key to System.getProperty(it.key, it.value)
    }.map {
        "-D${it.first}=${it.second}"
    }
}

val test by tasks.getting(Test::class) {
    systemProperty("ascient.backend", System.getProperty("ascient.backend"))
}