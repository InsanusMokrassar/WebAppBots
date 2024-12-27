package dev.inmo.tools.telegram.webapps.server

import io.ktor.http.*

fun String.decodeUrlQueryToMap(): Map<String, List<String>> {
    return split("&").map {
        val splitted = it.split("=")

        when (splitted.size) {
            1 -> it.decodeURLQueryComponent() to ""
            else -> splitted.first().decodeURLQueryComponent() to splitted[1].decodeURLQueryComponent()
        }
    }.groupBy { it.first }.mapValues { it.value.map { it.second } }.toMap()
}
