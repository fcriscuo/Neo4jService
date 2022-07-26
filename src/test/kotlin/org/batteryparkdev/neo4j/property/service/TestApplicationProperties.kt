package org.batteryparkdev.neo4j.property.service

import org.batteryparkdev.property.service.ApplicationProperties

class TestApplicationProperties {


}
fun main() {
    val neo4jProperties = ApplicationProperties("neo4j.config")
    // display Neo4j URI
    println("Neo4j URI: ${neo4jProperties.getConfigPropertyAsString("neo4j.uri")}")
    // look for an invalid properties file
    try {
        val badProperties= ApplicationProperties("invalid.config")
    } catch (e: Exception) {
        println(e.message)
    }
}