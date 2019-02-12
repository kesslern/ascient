package us.kesslern.ascient

object Environment {
    val databaseConnection: String = System.getProperty("database.connection", "")
    val databaseUsername: String = System.getProperty("database.username", "")
    val databasePassword: String = System.getProperty("database.password", "")
    val databasePort: Int = System.getProperty("ascient.port", "8080").toInt()
    val purgeInterval: Long = System.getProperty("ascient.sessions.purgeInterval", "300").toLong() * 1000
    val password: String = System.getProperty("ascient.sessions.password", "please")
    val sessionLength: Int = System.getProperty("ascient.sessions.length", "600").toInt()

}