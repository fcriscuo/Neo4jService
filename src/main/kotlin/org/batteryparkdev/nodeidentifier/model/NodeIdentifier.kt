package org.batteryparkdev.nodeidentifier.model

import org.batteryparkdev.neo4j.service.Neo4jUtils

/*
Represents the properties needed to create a child nodeidentifier node,
an existing parent node, and the relationship between them
A nodeidentifier node allows for the initial creation of a node as soon
as its identity occurs in the input data stream. The remaining properties
for that node can be completed by a subsequent or asynchronous task.
A specified property in the child node must be blank to identify it as a nodeidentifier
node.
 */
//data class PlaceholderNode(
//    val parentNode: NodeIdentifier,
//    val childNode: NodeIdentifier,
//    val relationshipType: String,
//    val blankPropertyType: String
//) {
//    fun isValid():Boolean =
//        parentNode.isValid().and(childNode.isValid()).and(relationshipType.isNotBlank())
//            .and(blankPropertyType.isNotBlank())
//}

/*
Represents a data class whose properties contain sufficient information to
identify an individual node in a Neo4j graph database
 */
data class NodeIdentifier(
    val primaryLabel: String,
    val idProperty: String,
    val idValue: String,
    val secondaryLabel:String="",
){
    fun isValid():Boolean =
        primaryLabel.isNotBlank().and(idProperty.isNotBlank()).and(idValue.isNotBlank())

    fun mergeNodeIdentifierCypher():String =
        when (secondaryLabel.isNotEmpty()){
            true -> "MERGE (n:$primaryLabel:$secondaryLabel{$idProperty: " +
                    "${Neo4jUtils.formatPropertyValue(idValue)}}) " +
                    "RETURN n.$idProperty"
            false -> "MERGE (n:$primaryLabel {$idProperty: ${Neo4jUtils.formatPropertyValue(idValue)}}) " +
                    "RETURN n.$idProperty"
        }

    fun addNodeLabelCypher():String =
    "MATCH (child:$primaryLabel{$idProperty:" +
            " ${Neo4jUtils.formatPropertyValue(idValue)} }) " +
            " WHERE apoc.label.exists(child,${Neo4jUtils.formatPropertyValue(secondaryLabel)})  = false " +
            " CALL apoc.create.addLabels(child, [${Neo4jUtils.formatPropertyValue(secondaryLabel)}] )" +
            " yield node return node"

}