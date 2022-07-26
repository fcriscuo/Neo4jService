/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.batteryparkdev.neo4j.service

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.property.service.Neo4jPropertiesService
import org.jetbrains.kotlin.konan.file.use
import org.neo4j.driver.*

/**
 * Responsible for establishing a connection to a local or remote Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {

    private val uri = Neo4jPropertiesService.neo4jUri
    private val logCypher = Neo4jPropertiesService.logCypherCommands
    private val config: Config = Config.builder().withLogging(Logging.slf4j()).build()
    private val database  = Neo4jPropertiesService.neo4jDatabase
    private val driver = GraphDatabase.driver(
        uri, AuthTokens.basic(Neo4jPropertiesService.neo4jAccount, Neo4jPropertiesService.neo4jPassword),
        config
    )

    fun close() {
        driver.close()
        Neo4jCypherWriter.close()
    }

    fun isSampleContext():Boolean = database == "sample"

    // expose the current database name
    fun getDatabaseName():String = database

    /*
    Constraint definitions do not return a result
     */
    fun defineDatabaseConstraint(command: String) {
        val session: Session = driver.session(SessionConfig.forDatabase(database))
        session.use {
            session.writeTransaction { tx ->
                tx.run(command)
            }!!
        }
    }
    /*
    Function to execute a query that returns multiple results
    Return type is a List of Records
     */
    fun executeCypherQuery(query: String): List<Record> {
        val retList = mutableListOf<Record>()
        val session = driver.session(SessionConfig.forDatabase(database))
        session.use {
            try {
                session.readTransaction { tx ->
                    val result = tx.run(query)
                    while (result.hasNext()) {
                        retList.add(result.next())
                    }
                }
            } catch (e: Exception) {
                LogService.logException(e)
                LogService.logError("Cypher query: $query")
            }
            return retList.toList()
        }
    }

    fun executeCypherCommand(command: String): String {
        if (logCypher)
         {
            Neo4jCypherWriter.recordCypherCommand(command)
        }
        val session = driver.session(SessionConfig.forDatabase(database))
        lateinit var resultString: String
        session.use {
            try {
                session.writeTransaction { tx ->
                    val result: org.neo4j.driver.Result = tx.run(command)
                    resultString = when (result.hasNext()) {
                        true -> result.single()[0].toString()
                        false -> ""
                    }
                }!!
                return resultString.toString()
            } catch (e: Exception) {
                LogService.logException(e)
                LogService.logError("Cypher command: $command")
            }
        }
        return ""
    }
}

