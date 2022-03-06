package org.batteryparkdev.logging.service

import com.google.common.flogger.FluentLogger
import com.google.common.flogger.StackSize
import org.batteryparkdev.property.service.ApplicationPropertiesService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
Responsible for providing a consistent logging structure for all components
A FileWriter to a temp file is supported to journal CYPHER commands in the order
that they were executed
 */

object LogService {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun logInfo(message: String) = logger.atInfo().log(message)
    fun logFine(message: String) = logger.atFine().log(message)
    fun logWarn(message: String) = logger.atWarning().log(message)
    fun logError(message: String) = logger.atSevere().log(message)
    fun logException(e:Exception) = logger.atSevere().withStackTrace(StackSize.FULL)
        .withCause(e).log(e.message)

    private val cypherPath = resolveCypherLogFileName()
    private val cypherFileWriter = File(cypherPath).bufferedWriter()

    fun close()= cypherFileWriter.close()

    fun recordCypherCommand(command:String) = cypherFileWriter.write("$command\n")

    private fun resolveCurrentTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy_HH:mm:ss")
        return sdf.format(Date())
    }

    private fun resolveCypherLogFileName() =
        ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.dir") + "/" +
                ApplicationPropertiesService.resolvePropertyAsString("neo4j.log.file.prefix") +
                "_" + resolveCurrentTime() + ".log"
}