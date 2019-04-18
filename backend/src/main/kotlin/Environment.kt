package us.kesslern.ascient

object Environment {
    val databaseHostname: String = System.getProperty("database.hostname", "")
    val databasePort: String = System.getProperty("database.port", "")
    val databaseName: String = System.getProperty("database.dbname", "")
    val databaseUsername: String = System.getProperty("database.username", "")
    val databasePassword: String = System.getProperty("database.password", "")
    val ascientPort: Int = System.getProperty("ascient.port", "8080").toInt()
    val purgeInterval: Long = System.getProperty("ascient.sessions.purgeInterval", "300").toLong() * 1000
    val sessionLength: Int = System.getProperty("ascient.sessions.length", "600").toInt()
    val databaseConnection = "jdbc:postgresql://$databaseHostname:$databasePort/$databaseName"
}