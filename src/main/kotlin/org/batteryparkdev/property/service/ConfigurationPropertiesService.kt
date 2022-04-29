package org.batteryparkdev.property.service

import org.apache.commons.configuration2.builder.fluent.Configurations
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

object ConfigurationPropertiesService
    {
        private const val PROPERTIES_FILE = "configuration.properties"
        private val config = Configurations().properties(PROPERTIES_FILE)

        fun getEnvVariable(varname:String):String = System.getenv(varname) ?: "undefined"

        fun resolveCosmicCompleteFileLocation(filename: String): String =
            when (getEnvVariable("NEO4J_URI").contains("localhost")) {
                true -> config.getString("localhost.cosmic.data.directory").plus("/")
                    .plus(filename)
                false -> config.getString("remote.cosmic.data.directory").plus("/")
                    .plus(filename)
            }
        fun resolveCosmicSampleFileLocation(filename: String): String =
            when (getEnvVariable("NEO4J_URI").contains("localhost")) {
                true -> config.getString("localhost.cosmic.sample.directory").plus("/")
                    .plus(filename)
                false -> config.getString("remote.cosmic.sample.directory").plus("/")
                    .plus(filename)
            }

        private fun createLogFileDirectory():String {
            val dirname = config.getString("neo4j.log.dir")
            val logDirPath = Paths.get(dirname)
            if(Files.isDirectory(logDirPath).not()) {
                File(dirname).mkdirs()
                println("Log directory $dirname created")
            }
            return dirname
        }

        private fun resolveCurrentTime(): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy_HH:mm")
            return sdf.format(Date())
        }

        fun resolveCypherLogFilename(): String =
            createLogFileDirectory().plus("/")
                .plus(config.getString("neo4j.log.file.prefix"))
                .plus(resolveCurrentTime()).
                    plus(".log")

        fun cypherLoggingStatus():Boolean =
            config.getBoolean("log.cypher.commands")

}

fun main() {
    // test for complete Cosmic file
    val localCompleteFilename = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicCompleteCNA.tsv")
    when (File(localCompleteFilename).exists()) {
        true -> println("$localCompleteFilename exists")
        false -> println("ERROR: $localCompleteFilename does NOT exist")
    }
    val sampleCompleteFilename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteCNA.tsv")
    when (File(sampleCompleteFilename).exists()) {
        true -> println("$sampleCompleteFilename exists")
        false -> println("ERROR: $sampleCompleteFilename does NOT exist")
    }
    if (ConfigurationPropertiesService.cypherLoggingStatus()) {
        println("Cypher log file: ${ConfigurationPropertiesService.resolveCypherLogFilename()}")
    } else {
        println("Cypher command logging is turned off")
    }



}