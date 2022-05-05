package org.batteryparkdev.neo4j.property.service

import org.batteryparkdev.logging.service.LogService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestConfigurationPropertiesService {
}
fun main() {
    val completeFileLocation = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("testfile.csv")
    LogService.logInfo("Complete file location for test file = $completeFileLocation")
    val sampleFileLocation = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("samplefile.tsv")
    LogService.logInfo("Sample file location for sample file = $sampleFileLocation")
}