package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService

class TestNeo4jConnectionService {
}
/*
Basic integration test to confirm a valid connection to Neo4j
 */
/*
main function for basic integration testing
confirms that a Neo4j connection can be made and cypher command logging
is working
 */
fun main() {
    //Display database name
    println("Connected to database name: ${Neo4jConnectionService.getDatabaseName()}")
    // create a test node
    val geneCount = Neo4jConnectionService.executeCypherCommand("MERGE (g:Gene{gene_name:'BRCA1'}) RETURN COUNT(g)")
    Neo4jUtils.detachAndDeleteNodesByName("Gene")
    val command = "MATCH (n) RETURN COUNT(n)"
    val count = Neo4jConnectionService.executeCypherCommand(command)
    LogService.logInfo("Node count $count")
    Neo4jCypherWriter.close()
}