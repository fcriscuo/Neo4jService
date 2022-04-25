package org.batteryparkdev.nodeidentifier.model

import org.batteryparkdev.neo4j.service.Neo4jUtils


/*
Represents the data attributes needed to identify two (2) nodes and create a
specified relationship between them

 */
data class RelationshipDefinition(
    val parentNode: NodeIdentifier,
    val childNode: NodeIdentifier,
    val relationshipType: String,
    val relationshipProperty: RelationshipProperty = RelationshipProperty()
) {
    fun isValid():Boolean =
        parentNode.isValid().and(childNode.isValid()).and(relationshipType.isNotBlank())

    fun generateRelationshipCypher() =
        "MERGE (p: ${parentNode.primaryLabel}{ ${parentNode.idProperty}: " +
                "${Neo4jUtils.formatPropertyValue(parentNode.idValue)}}) " +
                " MERGE (c: ${childNode.primaryLabel}{ ${childNode.idProperty}: " +
                "${Neo4jUtils.formatPropertyValue(childNode.idValue)} })" +
                "MERGE (p) -[r: ${relationshipType} ] -> (c) " +
                "RETURN p.${parentNode.idProperty} "

    fun deleteRelationshipDefinitionCypher() =
     "MATCH (parent:${parentNode.primaryLabel}), " +
            " (child:${childNode.primaryLabel}) WHERE " +
            " parent.${parentNode.idProperty} = " +
            "${Neo4jUtils.formatPropertyValue(parentNode.idValue)} " +
            " AND child.${childNode.idProperty} = " +
            "${Neo4jUtils.formatPropertyValue(childNode.idValue)} " +
            " MATCH  (parent) -[r:${relationshipType}] -> (child) " +
            " DELETE r "
}

data class RelationshipProperty(
    val relPropertyName:String ="",
    val relPropertyValue: String =""
) {
    fun isValid():Boolean =
        relPropertyName.isNotEmpty().and(relPropertyValue.isNotEmpty())
}
