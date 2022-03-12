package org.batteryparkdev.neo4j.service

import org.batteryparkdev.property.service.ApplicationPropertiesService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
Responsible for logging all MERGE and CREATE cypher commands to a specified file
 */
object Neo4jCypherWriter {
    private val cypherPath = resolveCypherLogFileName()
    private val cypherFileWriter = File(cypherPath).bufferedWriter()

    fun close()= cypherFileWriter.close()

    fun recordCypherCommand(command:String) =
        cypherFileWriter.write("$command\n")

    private fun resolveCurrentTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy_HH:mm:ss")
        return sdf.format(Date())
    }

    private fun resolveCypherLogFileName() =
        ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.dir") + "/" +
                ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.file.prefix") +
                "_" + resolveCurrentTime() + ".log"
}