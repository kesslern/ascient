package us.kesslern.ascient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer

class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

object TestContext {
    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JodaModule())

    private const val databaseDriver = "org.postgresql.Driver"
    private const val databaseConnection = "jdbc:tc:postgresql:9.6.8://hostname/databasename?TC_DAEMON=true"

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    var backend: String = System.getProperty("ascient.backend", "")
    var useRealBackend = !backend.isEmpty()

    init {
        if (!useRealBackend) {
            KPostgreSQLContainer().start()
            Flyway
                    .configure()
                    .dataSource(databaseConnection, "", "")
                    .load()
                    .migrate()
            Database.connect(databaseConnection, databaseDriver)
        }
    }
}

