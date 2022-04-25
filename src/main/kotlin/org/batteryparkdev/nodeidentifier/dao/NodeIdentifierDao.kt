package org.batteryparkdev.nodeidentifier.dao

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.nodeidentifier.model.RelationshipDefinition


object NodeIdentifierDao {
    /******
     *  Placeholder node creation function
     *******/
    fun createPlaceholderNode(node: NodeIdentifier){
        if(node.isValid().and(Neo4jUtils.nodeExistsPredicate(node)).not()) {
             Neo4jConnectionService.executeCypherCommand(node.mergeNodeIdentifierCypher())
        }
    }

    /*******
    Node relationship functions
     ***** */
    /*
    Public function to merge two(2) nodes and a specified relationship
    between them
     */
    fun defineRelationship(relDefinition: RelationshipDefinition) =
        when (relDefinition.isValid()) {
            true -> {
                Neo4jConnectionService.executeCypherCommand(relDefinition.generateRelationshipCypher())
            }
            false -> {
                LogService.logError("ERROR: RelationshipDefinition is invalid: $relDefinition")
            }
        }

    /*
       Utility function to delete a specified relationship between two (2)
       specified nodes
        */
    fun deleteSpecificParentChildRelationship(relDefinition: RelationshipDefinition) {
        if (Neo4jUtils.nodeExistsPredicate(relDefinition.parentNode)
                .and(Neo4jUtils.nodeExistsPredicate(relDefinition.childNode))
                .and( relDefinition.relationshipType.isNotBlank())
        ) {
            Neo4jConnectionService.executeCypherCommand(relDefinition.deleteRelationshipDefinitionCypher())
            LogService.logInfo(
                "Deleted ${relDefinition.relationshipType} relationship between parent " +
                        "\${relDefinition.parentNode}  and child ${relDefinition.childNode}"
            )
        } else {
            LogService.logWarn(
                "Invalid input parent: $relDefinition"
            )
        }
    }

     fun addChildSecondaryLabel(nodeIdentifier: NodeIdentifier) {
        if (nodeIdentifier.isValid().and(nodeIdentifier.secondaryLabel.isNotEmpty())){
            Neo4jConnectionService.executeCypherCommand(nodeIdentifier.addNodeLabelCypher())
        }
    }
}

// cypher verification test
fun main() {
    // should be false
    val nodeId = NodeIdentifier("XYZ", "pub_id", "1234567")
    println("Predicate = ${Neo4jUtils.nodeExistsPredicate(nodeId)}")
}
