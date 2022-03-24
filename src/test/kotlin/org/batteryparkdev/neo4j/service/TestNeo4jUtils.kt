package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.neo4j.service.Neo4jUtils.detachAndDeleteNodesByName
import org.batteryparkdev.neo4j.service.Neo4jUtils.formatPropertyValue
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.nodeidentifier.model.RelationshipDefinition

class TestNeo4jUtils {

    fun restoreGraph(){
        val cypher1 = "MERGE (p:Person{name:\"John Smith\"}) " +
                "SET p += {age:30} RETURN p "
       executeCypher(cypher1)
        val cypher2 = "MERGE (p:Person{name:\"Betty Roberts\"}) " +
                "SET p += {age:28} RETURN p "
        executeCypher(cypher2)
        val cypher3 = "MATCH (p1:Person), (p2:Person) WHERE " +
                " p1.name = \"John Smith\" AND p2.name = \"Betty Roberts\" " +
                "  MERGE (p1) -[r:HAS_FRIEND] -> (p2) RETURN r"
        executeCypher (cypher3)
    }

    fun close() {
        detachAndDeleteNodesByName("Person")
        }

    private fun executeCypher(cypher:String){
        try {
            Neo4jConnectionService.executeCypherCommand(cypher)
        } catch (e: Exception) {
            LogService.logException(e)
        }
    }

    fun testFormatPropertyValue(){
        val stringProperty = "XYZ"
        val numericProperty = "123"
        LogService.logInfo("should be quoted: ${formatPropertyValue(stringProperty)}")
        LogService.logInfo("should not be quoted: ${formatPropertyValue(numericProperty)}")
    }

    fun testNodeExistsPredicate() {
        val node1 = NodeIdentifier("Person", "name", "John Smith")
        LogService.logInfo("ValidPerson node exists should be true: ${Neo4jUtils.nodeExistsPredicate(node1)}")
        val node2 = NodeIdentifier("Person", "name", "Mary Jones")
        LogService.logInfo("Invalid Person node exists should be false: ${Neo4jUtils.nodeExistsPredicate(node2)}")
    }
    /*
    fun deleteNodeRelationshipByName(parentNode: String, childNode: String, relName: String)
     */
    fun testDeleteRelationshipsByName() {
        val relType = "HAS_FRIEND"
        Neo4jUtils.deleteRelationshipByType(relType)
    }

    fun testAddLabel() {
        val node1 = NodeIdentifier("Person", "name", "John Smith","Customer")
        Neo4jUtils.addLabelToNode(node1)
        // test if query finds node with new label
        val node2 = NodeIdentifier("Customer", "name", "John Smith")
        LogService.logInfo("Customer John Smith exists = ${Neo4jUtils.nodeExistsPredicate(node2)}")
        // remove all Customer labels
        Neo4jUtils.removeNodeLabel("Person","Customer")
        // test should now fail
        LogService.logInfo("Customer John Smith exists = ${Neo4jUtils.nodeExistsPredicate(node2)}")
    }

    fun testDeleteSpecificNode() {
        restoreGraph()
        val node1 = NodeIdentifier("Person", "name", "John Smith")
        Neo4jUtils.deleteNodeById(node1)
        // check if node1 still exists
        println("Deleted node exists = ${Neo4jUtils.nodeExistsPredicate(node1)} (should be false)")
    }




}

fun main(){
    val test = TestNeo4jUtils()
    test.restoreGraph()
    test.testNodeExistsPredicate()
    test.testFormatPropertyValue()
    test.testAddLabel()
    test.testDeleteSpecificNode()
    test.testDeleteRelationshipsByName()
    test.close()
}