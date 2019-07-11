package net.technearts

import kotlin.reflect.KClass

inline fun <T : Any> Class<T>.named(name: String) = Pair(this, name)

infix fun <T : Any> KClass<T>.named(name: String) = Pair(this.java, name)

infix fun <T : Any> Pair<Class<T>, String>.from(sheet: String) = Triple(this.first, this.second, sheet)

infix fun <T : Any, V : Any> Triple<Class<T>, String, String>.read(members: List<Triple<String, String, (String) -> V>>): KMapping {
    return KMapping()
}

infix fun String.column(title: String) = Pair(this, title)

infix fun <T : Any> Pair<String, String>.converted(converter: (String) -> T) = Triple(this.first, this.second, converter)

infix fun <T : Any, V: Any> Triple<String, String, (String) -> T>.and(other: Triple<String, String, (String) -> V>) = listOf(this, other)
