package org.batteryparkdev.neo4j.model

/*
Class to represent identifying properties for a Neo4j database node
 */
data class NodeIdentifier(
    val primaryLabel: String,
    val idProperty: String,
    val idValue: String,
    val secondaryLabel:String="",
){
    fun isValid():Boolean =
        primaryLabel.isNotBlank().and(idProperty.isNotBlank()).and(idValue.isNotBlank())
}