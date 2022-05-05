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
    // test journaling a fake command
    Neo4jCypherWriter.recordCypherCommand("MERGE (n:FAKE_NODE{nid:100}) RETURN n.nid")
    val command = "MATCH (n) RETURN COUNT(n)"
    val count = Neo4jConnectionService.executeCypherCommand(command)
    LogService.logInfo("Node count $count")
    Neo4jCypherWriter.close()
}