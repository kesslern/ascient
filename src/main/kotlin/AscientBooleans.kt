package us.kesslern.ascient

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Booleans : Table("booleans") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", 25)
    val value: Column<Boolean> = bool("value")
}

object AscientBooleans {
    fun get(): List<AscientBoolean> =
            transaction {
                Booleans.selectAll().map { it ->
                    AscientBoolean(
                            it[Booleans.id],
                            it[Booleans.name],
                            it[Booleans.value]
                    )
                }
            }

    fun insert(newName: String, newValue: Boolean): Int =
            transaction {
                Booleans.insert {
                    it[name] = newName
                    it[value] = newValue
                } get (Booleans.id) ?: throw IllegalArgumentException()
            }

    fun update(id: Int, newValue: Boolean) {
        Booleans.update({ Booleans.id eq id }) {
            it[value] = newValue
        }
    }

    fun delete(id: Int) {
        transaction {
            Booleans.deleteWhere { Booleans.id eq id }
        }
    }
}