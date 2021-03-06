/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.batteryparkdev.neo4j.service

import com.google.common.flogger.StackSize
import org.batteryparkdev.logging.service.LogService
import org.jetbrains.kotlin.konan.file.use
import org.neo4j.driver.*

/**
 * Responsible for establishing a connection to a local Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {

    private val neo4jAccount = Neo4jUtils.getEnvVariable("NEO4J_ACCOUNT")
    private val neo4jPassword = Neo4jUtils.getEnvVariable("NEO4J_PASSWORD")

    private const val uri = "neo4j://localhost:7687"
    private val config: Config = Config.builder().withLogging(Logging.slf4j()).build()
    private val driver = GraphDatabase.driver(
        uri, AuthTokens.basic(neo4jAccount, neo4jPassword),
        config
    )

    fun close() {
        driver.close()
        LogService.close()
    }

    /*
    Constraint definitions do not return a result
     */
    fun defineDatabaseConstraint(command: String) {
        val session: Session = driver.session()
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
        val session = driver.session()
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
        if (command.uppercase().startsWith("MERGE ") ||
            command.uppercase().startsWith("CREATE ")
        ) {
            LogService.recordCypherCommand(command)
        }
        val session = driver.session()
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

/*
main function for basic integration testing
 */
fun main() {
    val command = "MATCH (n) RETURN COUNT(n)"
    val count = Neo4jConnectionService.executeCypherCommand(command)
    LogService.logInfo("Node count $count")
    // test journaling a fake command
    LogService.recordCypherCommand("MERGE (n:FAKE_NODE{nid:100}) RETURN n.nid")
    LogService.close()
}