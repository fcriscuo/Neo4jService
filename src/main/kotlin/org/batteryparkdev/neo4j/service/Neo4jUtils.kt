package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.placeholder.model.NodeIdentifier
import java.util.*

object Neo4jUtils {

    fun getEnvVariable(varname:String):String = System.getenv(varname) ?: "undefined"
    /*
    Function to simplify quoting a String value for Cypher input
     */
    fun formatQuotedString(input:String):String =
        "\"" + input +"\""

    /*
   Function that will quote a Neo4j property value if it
   is not numeric.
   Simplifies embedding property values into Cypher statements
    */
    fun formatPropertyValue(propertyValue: String): String {
        return when (propertyValue.toIntOrNull()) {
            null -> "\"$propertyValue\""
            else -> propertyValue
        }
    }
    /*******
    LABEL-related functions
    ***** */

    /*
   Utility function to add a secondary label to a node if that
   label is novel
    */
    @Deprecated("Use nodeExistsPredicate method instead",
        replaceWith = ReplaceWith("nodeExistsPredicate(nodeId: NodeIdentifier)"),
        level = DeprecationLevel.ERROR)
    fun addSecondaryNodeLabel(nodeId: NodeIdentifier) = addLabelToNode(nodeId)


    /*
    Utility method to add a secondary label to an existing node if the
    new label is novel
     */
    fun addLabelToNode(node: NodeIdentifier) {
        if (node.isValid().and(node.secondaryLabel.isNotBlank())){
            val cypher = "MATCH (child:${node.primaryLabel}{${node.idProperty}:" +
                    " ${formatPropertyValue(node.idValue)} }) " +
                    " WHERE apoc.label.exists(child,\"${node.secondaryLabel}\")  = false " +
                    " CALL apoc.create.addLabels(child, [\"${node.secondaryLabel}\"] )" +
                    " yield node return node"
            Neo4jConnectionService.executeCypherCommand(cypher)
        }
    }

    /*
    Function to delete a specified label from a specified node type
    n.b. May affect >= 1 node(s)
     */
    fun removeNodeLabel(nodeName: String, label: String) {
        val removeLabelTemplate = "MATCH (n:NODENAME) REMOVE n:LABEL RETURN COUNT(n)"
        val countLabelTemplate = "MATCH(l:LABEL) RETURN COUNT(l)"
        val removeLabelCommand = removeLabelTemplate
            .replace("NODENAME", nodeName)
            .replace("LABEL", label)
        val countLabelCommand = countLabelTemplate.replace("LABEL", label)
        val beforeCount = Neo4jConnectionService.executeCypherCommand(countLabelCommand)
        LogService.logInfo("Node type: $nodeName, removing label: $label before count = $beforeCount")
        Neo4jConnectionService.executeCypherCommand(removeLabelCommand)
        val afterCount = Neo4jConnectionService.executeCypherCommand(countLabelCommand)
        LogService.logInfo("Node type: $nodeName, after label removal command count = $afterCount")
    }

    /*******
    Node deletion functions
     ***** */

    /*
    Function to delete a specific Node
     */
    fun deleteNodeById(nodeId: NodeIdentifier) {
        if (nodeId.isValid()) {
            val cypher = "MATCH (n:${nodeId.primaryLabel}) WHERE n.${nodeId.idProperty} " +
                    " = ${formatPropertyValue(nodeId.idValue)} DETACH DELETE(n)"
            Neo4jConnectionService.executeCypherCommand(cypher)
        }
    }

    // detach and delete specified nodes in database
    fun detachAndDeleteNodesByName(nodeName: String) {
        val beforeCount = Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) RETURN COUNT (n)"
        )
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) DETACH DELETE (n);"
        )
        val afterCount = Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) RETURN COUNT (n)"
        )
        LogService.logInfo(
            "Deleted $nodeName nodes, before count=${beforeCount.toString()}" +
                    "  after count=$afterCount"
        )
    }

    /*******
    Node existence functions
     ***** */

    /*
  Function to determine if a node has already been loaded into Neo4j
   */
    @Deprecated("Use nodeExistsPredicate method instead",
        replaceWith = ReplaceWith("nodeExistsPredicate(nodeId: NodeIdentifier)"),
        level = DeprecationLevel.ERROR)
    fun nodeLoadedPredicate(cypherCommand: String): Boolean {
        if (cypherCommand.contains("PREDICATE", ignoreCase = true)) {
            try {
                val predicate = Neo4jConnectionService.executeCypherCommand(cypherCommand)
                when (predicate.lowercase(Locale.getDefault())) {
                    "true" -> return true
                    "false" -> return false
                }
            } catch (e: Exception) {
                LogService.logException(e)
                return false
            }
        }
        return false
    }
    /*
   Function to determine if a Publication node with a specified
   id exists in the database
    */
    @Deprecated("Use nodeExistsPredicate method instead",
        replaceWith = ReplaceWith("nodeExistsPredicate(nodeId: NodeIdentifier)"),
        level = DeprecationLevel.WARNING)
    fun publicationNodeExistsPredicate(pubId: String): Boolean {
        val nodeId = NodeIdentifier("Publication","pub_id", pubId )
        return nodeExistsPredicate(nodeId)
    }
/*
Utility function to determine if a specified node is in the database
 */

    fun nodeExistsPredicate(nodeId: NodeIdentifier):Boolean {
        when (nodeId.isValid()) {
            true -> {
                val cypher = "OPTIONAL MATCH (node:${nodeId.primaryLabel}{" +
                        "${nodeId.idProperty}:" +
                        "${formatPropertyValue(nodeId.idValue)}}) " +
                        " RETURN node IS NOT NULL AS Predicate"
                return try {
                    Neo4jConnectionService.executeCypherCommand(cypher).toBoolean()
                } catch (e: Exception) {
                    LogService.logException(e)
                    return false
                }
            }
            false -> LogService.logWarn("Invalid NodeIdentifier: $nodeId")
        }
        return false
    }

    /*******
    Node relationship functions
     ***** */

    /*
    Utility function to create a parent -[r:Relationship] -> child Neo4j relationship
     */
    fun createParentChildRelationship(parent:NodeIdentifier, child:NodeIdentifier,
                                      relationship: String) {
        if (nodeExistsPredicate(parent).and(nodeExistsPredicate(child))
                .and(relationship.isNotBlank())){
            val cypher = "MATCH (parent:${parent.primaryLabel}), " +
                    " (child:${child.primaryLabel}) WHERE " +
                    " parent.${parent.idProperty} = ${formatPropertyValue(parent.idValue)} " +
                    " AND child.${child.idProperty} = ${formatPropertyValue(child.idValue)} " +
                    " MERGE (parent) -[r:$relationship] -> (child) " +
                    " RETURN r "
            Neo4jConnectionService.executeCypherCommand(cypher)
        } else {
            LogService.logWarn("Invalid input(s) parent: $parent \n" +
                    " child: $child \n relationship: $relationship")
        }
    }
    /*
   Function to delete  all Neo4j relationships by a relationship name
    */
    fun deleteNodeRelationshipByName(parentNode: String, childNode: String, relName: String) {
        val deleteTemplate = "MATCH (:PARENT) -[r:RELATIONSHIP_NAME] ->(:CHILD) DELETE r;"
        val delCommand = deleteTemplate
            .replace("PARENT", parentNode)
            .replace("CHILD", childNode)
            .replace("RELATIONSHIP_NAME", relName)
        val countCommand = delCommand
            .replace("DELETE r", "RETURN COUNT(*)")
        val beforeCount = Neo4jConnectionService.executeCypherCommand(countCommand)
        LogService.logInfo("Deleting $parentNode $relName $childNode relationships, before count = $beforeCount")
        Neo4jConnectionService.executeCypherCommand(delCommand)
        val afterCount = Neo4jConnectionService.executeCypherCommand(countCommand)
        LogService.logInfo("After deletion command count = $afterCount")
    }

    /*
    Utility function to delete a specified relationship between two (2)
    specified nodes
     */
    fun deleteSpecificParentChildRelationship(parent:NodeIdentifier, child:NodeIdentifier,
                                              relationship: String) {
        if (nodeExistsPredicate(parent).and(nodeExistsPredicate(child))
                .and(relationship.isNotBlank())){
            val cypher = "MATCH (parent:${parent.primaryLabel}), " +
                    " (child:${child.primaryLabel}) WHERE " +
                    " parent.${parent.idProperty} = ${formatPropertyValue(parent.idValue)} " +
                    " AND child.${child.idProperty} = ${formatPropertyValue(child.idValue)} " +
                    " MATCH  (parent) -[r:$relationship] -> (child) " +
                    " DELETE r "
            Neo4jConnectionService.executeCypherCommand(cypher)
            LogService.logInfo("Deleted $relationship relationship between parent $parent " +
                    " and child $child")
        } else {
            LogService.logWarn("Invalid input(s) parent: $parent \n" +
                    " child: $child \n relationship: $relationship")
        }
    }
}