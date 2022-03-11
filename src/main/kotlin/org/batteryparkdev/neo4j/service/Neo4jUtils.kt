package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.neo4j.model.NodeIdentifier
import java.util.*

object Neo4jUtils {

    fun getEnvVariable(varname:String):String = System.getenv(varname) ?: "undefined"
    /*
    Function to simplify quoting a String value for Cypher input
     */
    fun formatQuotedString(input:String):String =
        "\"" + input +"\""
    /*
    Function to determine if a node has already been loaded into Neo4j
     */
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
    Function to delete a Neo4j relationship
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
    Function to delete a specified label from a specified node type
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

    /*
   Function to determine if a Publication node with a specified
   id exists in the database
    */
    fun publicationNodeExistsPredicate(pubId: String): Boolean {
        val cypher = "OPTIONAL MATCH (pub:Publication{pub_id: $pubId }) " +
                " RETURN pub IS NOT NULL AS Predicate"
        return try {
            Neo4jConnectionService.executeCypherCommand(cypher).toBoolean()
        } catch (e: Exception) {
            LogService.logException(e)
            false
        }
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

    /*
    Public function to determine if a Publication node with a specified
    id and label exists in the database
     */
    fun publicationIdAndLabelPredicate(pubId: String,label:String):Boolean =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (pub:Publication{ pub_id: $pubId}) " +
                    " WHERE apoc.label.exists(pub,\"$label\")" +
                    " WITH count(*) AS count " +
                    " CALL apoc.when (count > 0, " +
                    " \"RETURN true AS bool\",  " +
                    " \"RETURN false AS bool\", " +
                    " {count:count}" +
                    " ) YIELD value " +
                    " return value.bool "
        ).toBoolean()

    /*
    Utility function to add a secondary label to a node if that
    label is novel
     */
    fun addSecondaryNodeLabel(nodeId: NodeIdentifier) {
        when (nodeId.isValid()) {
            true -> {
                LogService.logFine(
                    "Add Label to ${nodeId.primaryLabel} ${nodeId.primaryLabel}:${nodeId.idValue} " +
                            "new label = ${nodeId.secondaryLabel}"
                )
                val cypher = "MATCH (child:${nodeId.primaryLabel}{${nodeId.primaryLabel}:" +
                        " ${formatPropertyValue(nodeId.idValue)} }) " +
                        " WHERE apoc.label.exists(child,\"${nodeId.secondaryLabel}\")  = false " +
                        " CALL apoc.create.addLabels(child, [\"${nodeId.secondaryLabel}\"] )" +
                        " yield node return node"
                Neo4jConnectionService.executeCypherCommand(cypher)
            }
            false -> LogService.logWarn("Invalid NodeIdentifier: $nodeId")
        }
    }

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
    Utility function that will quote a Neo4j property value if it
    is not numeric.
    Simplifies creating Cypher statements
     */
    fun formatPropertyValue(propertyValue: String): String {
        return when (propertyValue.toIntOrNull()) {
            null -> "\"$propertyValue\""
            else -> propertyValue
        }
    }

}