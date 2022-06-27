package org.batteryparkdev.neo4j.nodeidentifier.dao

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.dao.NodeIdentifierDao
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.nodeidentifier.model.RelationshipDefinition

class TestNodeIdentifierDao {
    private val book = NodeIdentifier("Book", "title", "Moby Dick")
    private val author = NodeIdentifier("Author", "name", "Melville")
    private val book2 = NodeIdentifier("Book", "title", "Omoo")
    private val relDef = RelationshipDefinition(book, author, "HAS_AUTHOR")
    private val relDef2 = RelationshipDefinition(book2, author, "HAS_AUTHOR")

    private fun queryAllBooks() {
        val cypher = "MATCH (b:Book) -[:HAS_AUTHOR] -> (a:Author) RETURN b.title,a.name"
        val results = Neo4jConnectionService.executeCypherQuery(cypher)
        results.forEach { record ->
            run {
                val recMap = record.asMap()
                println("Book: ${recMap.get("b.title").toString()}   Author: ${recMap.get("a.name").toString()}")
            }
        }
    }

    private fun queryABook(label:String, title:String) {
        val cypher = "MATCH (b:$label) WHERE b.title = ${Neo4jUtils.formatPropertyValue(title)} " +
                " RETURN b.title"
        val results = Neo4jConnectionService.executeCypherQuery(cypher)
        results.forEach { record ->
            run {
                val recMap = record.asMap()
                println("Book: ${recMap.get("b.title").toString()} ")
            }
        }
    }

    private fun createBooks() {
        NodeIdentifierDao.defineRelationship(relDef)
        NodeIdentifierDao.defineRelationship(relDef2)
    }

    private fun deleteBooks() {
        Neo4jUtils.deleteNodeById(book)
        Neo4jUtils.deleteNodeById(book2)
        Neo4jUtils.deleteNodeById(author)
    }

    fun testRelationshipDefinition() {
        createBooks()
        // display the books written by the author
        println("After creation, Books written by Melville  (should be two)")
        queryAllBooks()
        deleteBooks()
        println("After deletion, Books written by Melville  (should be zero)")
    }

    fun testAddSecondaryLabel() {
        val aBook = NodeIdentifier("Book", "title", "The Martian")
        NodeIdentifierDao.createPlaceholderNode(aBook)
        // display as a Book
        queryABook("Book","The Martian")
        val sciBook = NodeIdentifier("Book", "title", "The Martian",
            "SciFi")
        println("Query book by new label")
        queryABook("SciFi", "The Martian")
        deleteBooks()
    }

    fun testDeleteSpecifiedRelationship() {
        val node1 = NodeIdentifier("Person", "name", "John Smith")
        val node2 = NodeIdentifier("Person", "name", "Betty Roberts")
        val relDef = RelationshipDefinition(node1, node2, "HAS_FRIEND")
        NodeIdentifierDao.deleteSpecificParentChildRelationship(relDef)
    }
}
fun main() {
    val test = TestNodeIdentifierDao()
    test.testRelationshipDefinition()
    test.testAddSecondaryLabel()
}