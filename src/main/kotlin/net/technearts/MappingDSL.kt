package net.technearts

import kotlin.reflect.KClass

inline fun <T : Any> Class<T>.named(name: String) = Pair(this, name)

infix fun <T : Any> KClass<T>.named(name: String) = Pair(this.java, name)

infix fun <T : Any> Pair<Class<T>, String>.from(sheet: String) = Triple(this.first, this.second, sheet)

infix fun <T : Any> Triple<Class<T>, String, String>.read(members: List<Triple<String, String, String>>): Mapping {

    return Mapping(this.first.name, this.second, this.third, members.map {
        Member(it.first, null, it.third, it.second, null, false)
    })
}
