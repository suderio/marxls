package net.technearts

import org.apache.poi.ss.usermodel.DateUtil
import java.math.RoundingMode
import kotlin.reflect.KClass

infix fun <T : Any> Class<T>.named(name: String) = Pair(this, name)

infix fun <T : Any> KClass<T>.named(name: String) = Pair(this.java, name)

infix fun <T : Any> Pair<Class<T>, String>.from(sheet: String) = Triple(this.first, this.second, sheet)

infix fun <T : Any, V : Any> Triple<Class<T>, String, String>.read(members: List<Triple<String, String, (String) -> V>>): Mapping {
    val mapping = Mapping()
    mapping.className = this.first.name
    mapping.name = this.second
    mapping.sheetName = this.third
    mapping.members = members.map {
        val member = Member()
        member.property = it.first
        member.column = it.second
        member.converter = it.third
        member
    }
    return Mapping()
}

infix fun String.column(title: String) = Pair(this, title)

infix fun <T : Any> Pair<String, String>.converted(converter: (String) -> T) = Triple(this.first, this.second, converter)

val asDate = { a: String -> DateUtil.getJavaDate(java.lang.Double.valueOf(a), true) }

val asByte = { a: String -> a.toByte() }

val asShort = { a: String -> a.toShort() }

val asInteger = { a: String -> a.toInt() }

val asLong = { a: String -> a.toLong() }

val asBoolean = { a: String -> a.toBoolean() }

val asFloat = { a: String -> a.toFloat() }

val asDouble = { a: String -> a.toDouble() }

val asChar = { a: String -> a.toCharArray()[0] }

val asString = { a: String -> a }

fun asDecimal(scale: Int) = { a: String -> a.toBigDecimal().setScale(scale, RoundingMode.HALF_UP) }
