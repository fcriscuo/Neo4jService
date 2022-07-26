package org.batteryparkdev.property.service

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

object Neo4jPropertiesService
    {
        private val config = ApplicationProperties("ne04j.config")
        val neo4jUri = config.getConfigPropertyAsString("neo4j.uri")
        val neo4jAccount = config.getConfigPropertyAsString("neo4j.account")
        val neo4jPassword = config.getConfigPropertyAsString("neo4j.password")
        val neo4jLogDir = config.getConfigPropertyAsString("neo4j.log.dir")
        val neo4jHome = config.getConfigPropertyAsString("neo4j.home")
        val neo4jLogFilePrefix = config.getConfigPropertyAsString("neo4j.log.file.prefix")
        val logCypherCommands = config.getConfigPropertyAsBoolean("log.cypher.commands")
        val neo4jDatabasePath = config.getConfigPropertyAsString("neo4j.db.path")

        private fun createLogFileDirectory():String {
           
            val logDirPath = Paths.get(neo4jLogDir)
            if(Files.isDirectory(logDirPath).not()) {
                File(neo4jLogDir).mkdirs()
                println("Log directory $neo4jLogDir created")
            }
            return neo4jLogDir
        }

        private fun resolveCurrentTime(): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy_HH:mm")
            return sdf.format(Date())
        }

        fun resolveCypherLogFilename(): String =
            createLogFileDirectory().plus("/")
                .plus(neo4jLogFilePrefix)
                .plus(resolveCurrentTime()).
                    plus(".log")

        fun cypherLoggingStatus():Boolean = logCypherCommands




    }
