package org.batteryparkdev.neo4j.service

import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.io.File

class TestConfigurationPropertiesService {
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
    val runMode = "sample"
    println("sample hgnc file = ${ConfigurationPropertiesService.resolveCosmicDataFileProperty(runMode,"file.cosmic.hgnc" )}")


}