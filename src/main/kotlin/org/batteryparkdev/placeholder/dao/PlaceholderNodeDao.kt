package org.batteryparkdev.placeholder.dao

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.neo4j.service.Neo4jUtils.formatPropertyValue
import org.batteryparkdev.placeholder.model.NodeIdentifier
import org.batteryparkdev.placeholder.model.PlaceholderNode

object PlaceholderNodeDao {

    private const val createChildNodeTemplate =
        "MERGE (child:CHILD_LABEL{CHILD_ID_PROPERTY:CHILD_ID_VALUE}) " +
                "SET child += {BLANK_PROPERTY: \"\"} RETURN child.CHILD_ID_PROPERTY"

    private const val parentRelationshipTemplate = "MATCH (child:CHILD_LABEL), " +
            " (par:PARENT_LABEL) WHERE par.PARENT_ID_PROPERTY = PARENT_ID_VALUE " +
            " AND child.CHILD_ID_PROPERTY = CHILD_ID_VALUE " +
            " MERGE (par) -[r:RELATIONSHIP] -> (child) " +
            " RETURN r "

    /*
    Persist a placeholder node and create a parent to child relationship
     */
    fun persistPlaceholderNode(placeHolder: PlaceholderNode): String {
        if (placeHolder.isValid()) {
            // if the node does not already exist, load it
            if (!Neo4jUtils.nodeExistsPredicate(placeHolder.childNode)) {
                val childId = createChildNode(placeHolder)
            }
            createParentChildRelationship(placeHolder)
            if (placeHolder.childNode.secondaryLabel.isNotBlank()) {
                addChildSecondaryLabel(placeHolder)
            }
            println(
                "Placeholder node type: ${placeHolder.childNode.primaryLabel} " +
                        "  id = ${placeHolder.childNode.idValue} "
            )
        } else {
            LogService.logWarn("Invalid PlaceHolderNode instance")
        }
        return ""
    }

    /*
 Create a placeholder node.
 n.b. A placeholder node is identified by having an empty title property
  */
    private fun createChildNode(placeHolder: PlaceholderNode): String {
        val cypher = createChildNodeTemplate.replace("CHILD_LABEL", placeHolder.childNode.primaryLabel)
            .replace("CHILD_ID_PROPERTY", placeHolder.childNode.idProperty)
            .replace("CHILD_ID_VALUE", formatPropertyValue(placeHolder.childNode.idValue))
            .replace("BLANK_PROPERTY", placeHolder.blankPropertyType)
        LogService.logFine("Merger cypher: $cypher")  //TODO: remove after testing
        return Neo4jConnectionService.executeCypherCommand(cypher)
    }

    /*
    Complete a parent node to child node relationship
     */
    private fun createParentChildRelationship(placeHolder: PlaceholderNode) {
        val cypher = parentRelationshipTemplate.replace("CHILD_LABEL", placeHolder.childNode.primaryLabel)
            .replace("PARENT_LABEL", placeHolder.parentNode.primaryLabel)
            .replace("PARENT_ID_PROPERTY", placeHolder.parentNode.idProperty)
            .replace("PARENT_ID_VALUE", formatPropertyValue(placeHolder.parentNode.idValue))
            .replace("CHILD_ID_PROPERTY", placeHolder.childNode.idProperty)
            .replace("CHILD_ID_VALUE", formatPropertyValue(placeHolder.childNode.idValue))
            .replace("RELATIONSHIP", placeHolder.relationshipType)
        LogService.logFine("Relationship cypher: $cypher") //TODO: remove after testing
        Neo4jConnectionService.executeCypherCommand(cypher)
    }

    private fun addChildSecondaryLabel(placeHolder: PlaceholderNode) {
        val cypher = "MATCH (child:${placeHolder.childNode.primaryLabel}{${placeHolder.childNode.idProperty}:" +
                " ${formatPropertyValue(placeHolder.childNode.idValue)} }) " +
                " WHERE apoc.label.exists(child,${formatPropertyValue(placeHolder.childNode.secondaryLabel)})  = false " +
                " CALL apoc.create.addLabels(child, [${formatPropertyValue(placeHolder.childNode.secondaryLabel)}] )" +
                " yield node return node"
        LogService.logFine(cypher)  //TODO: remove after testing
        Neo4jConnectionService.executeCypherCommand(cypher)
    }
}

// cypher verification test
fun main() {
    // should be false
    val nodeId = NodeIdentifier("XYZ","pub_id", "1234567" )
    println("Predicate = ${Neo4jUtils.nodeExistsPredicate(nodeId)}")
}
