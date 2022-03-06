package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService
import java.io.File


private const val constraintTemplate = "CREATE CONSTRAINT CONSTRAINT_NAME " +
        " IF NOT EXISTS ON (NODE_ABBREV:NODE_LABEL) " +
        " ASSERT NODE_ABBREV.NODE_PROPERTY IS UNIQUE"
/*
Function to define a new constraint in the Neo4j database
 */
fun defineNeo4jConstraint(constraintName:String, nodeAbbreviation:String,
 nodeLabel:String, nodeProperty:String) {
    val cypher = constraintTemplate.replace("CONSTRAINT_NAME",constraintName)
        .replace("NODE_ABBREV",nodeAbbreviation)
        .replace("NODE_LABEL", nodeLabel)
        .replace("NODE_PROPERTY", nodeProperty)
    Neo4jConnectionService.defineDatabaseConstraint(cypher)
    LogService.logInfo("Constraint: $cypher  has been defined")
}

/*
Function to read Cypher constraint definitions from a specified File
Constraint must be defined on a single line
 */
fun defineConstraintsFromFile(filename:String) {
    try {
        defineConstraints(File(filename).readLines())
    } catch (e: Exception) {
        LogService.logException(e)
    }
}

fun defineConstraints(constraints: List<String>) {
    constraints.forEach {
        Neo4jConnectionService.defineDatabaseConstraint(it)
        LogService.logInfo("Constraint: $it  has been defined")
    }
}

// stand-alone invocation
fun main(){
   defineNeo4jConstraint("unique_node_id", "n",
        "TestNode","node_id")
}