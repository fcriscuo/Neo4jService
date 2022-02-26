/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.batteryparkdev.neo4j.service

import com.google.common.flogger.FluentLogger
import com.google.common.flogger.StackSize
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils.getEnvVariable
import org.batteryparkdev.property.service.ApplicationPropertiesService
import org.jetbrains.kotlin.konan.file.use
import org.neo4j.driver.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Responsible for establishing a connection to a local Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val neo4jAccount = getEnvVariable("NEO4J_ACCOUNT")
    private val neo4jPassword = getEnvVariable("NEO4J_PASSWORD")
    private val cypherPath = resolveCypherLogFileName()
    private val cypherFileWriter = File(cypherPath).bufferedWriter()
    private const val uri = "bolt://localhost:7687"
    private val config: Config = Config.builder().withLogging(Logging.slf4j()).build()
    private val driver = GraphDatabase.driver(
        uri, AuthTokens.basic(neo4jAccount, neo4jPassword),
        config
    )

    fun close() {
        driver.close()
        cypherFileWriter.close()
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
                logger.atSevere().withStackTrace(StackSize.FULL)
                    .withCause(e).log(e.message)
                logger.atSevere().log("Cypher query: $query")
            }

            return retList.toList()
        }
    }

    fun executeCypherCommand(command: String): String {
        if (command.uppercase().startsWith("MERGE ") ||
            command.uppercase().startsWith("CREATE ")
        ) {
            cypherFileWriter.write("$command\n")
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
                logger.atSevere().withStackTrace(StackSize.FULL)
                    .withCause(e).log(e.message)
                logger.atSevere().log("Cypher command: $command")
            }
        }
        return ""
    }
}

fun resolveCurrentTime(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy_hh:mm:ss")
    return sdf.format(Date())
}

fun resolveCypherLogFileName() =
    ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.dir") + "/" +
            ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.file.prefix") +
            "_" + resolveCurrentTime() + ".log"

/*
main function for basic integration testing
 */
fun main() {
    val command = "MATCH (n) RETURN COUNT(n)"
    val count = Neo4jConnectionService.executeCypherCommand(command)
    println(count)
}