package org.batteryparkdev.logging.service


import mu.KotlinLogging

/*
Responsible for providing a consistent logging structure for all components
 */
val logger = KotlinLogging.logger {}
object LogService {
    fun logInfo(message: String) = logger.info { message }
    fun logFine(message: String) = logger.debug { message }
    fun logWarn(message: String) = logger.warn { message }
    fun logError(message: String) = logger.error{message}
    fun logException(e:Exception) = logger.error{e.message}

}