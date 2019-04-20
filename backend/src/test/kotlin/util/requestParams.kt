package us.kesslern.ascient.util

import kotlin.reflect.full.memberProperties

abstract class QueryParamBase {
    /**
     * Build a query param string (including the initial "?") from the class's member properties.
     */
    override fun toString(): String =
        this.javaClass.kotlin.memberProperties.foldIndexed("") { idx, acc, it ->
            val value = it.getter(this) ?: return@foldIndexed acc
            return@foldIndexed acc + "${if (idx == 0) "?" else "&"}${it.name}=$value"
        }
}
