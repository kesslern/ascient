import sun.tools.jar.resources.jar

plugins {
    kotlin("jvm") version "1.3.10"
    application
}

object Versions {
    const val exposed           = "0.11.2"
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

val databaseConnection =
        System.getProperty(
                "database.connection",
                "jdbc:postgresql://localhost:5432/postgres")

val databaseUsername = System.getProperty("database.username", "user")
val databasePassword = System.getProperty("database.password", "pass")

application {
    mainClassName = "us.kesslern.ascient.MainKt"
    group = "us.kesslern"
    applicationName = "ascient"
    applicationDefaultJvmArgs = listOf(
            "-Ddatabase.connection=${databaseConnection}",
            "-Ddatabase.username=${databaseUsername}",
            "-Ddatabase.password=${databasePassword}"
    )
}

tasks {
    withType<Jar> {
        archiveName = "ascient.jar"

        manifest {
            attributes(mapOf("Main-Class" to ""))
        }

        from(configurations.compile.map { if (it.isDirectory) it else zipTree(it) })
    }
}
