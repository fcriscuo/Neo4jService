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

        fun getConfigPropertyAsString(propertyName:String): String =
            config.getString(propertyName,"")

        fun getConfigPropertyAsBoolean(propertyName: String):Boolean =
            config.getBoolean(propertyName,false)

        fun getConfigPropertyAsInt(propertyName: String): Int =
            config.getInt(propertyName, 0)

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

        private fun resolveSampleFileProperty(fileProperty: String): String =
            resolveCosmicSampleFileLocation(fileProperty)

        private fun resolveCompleteFileProperty(fileProperty: String): String =
            ConfigurationPropertiesService.resolveCosmicCompleteFileLocation(fileProperty)

        fun resolveCosmicDataFileProperty(runMode: String, fileProperty: String): String =
            when (runMode.lowercase().equals("complete")) {
                true -> resolveCompleteFileProperty(fileProperty)
                false -> resolveSampleFileProperty(fileProperty)
            }

    }
