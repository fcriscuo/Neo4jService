package org.batteryparkdev.neo4j.system

import io.github.cdimascio.dotenv.dotenv
import org.batteryparkdev.system.SystemUtil

class TestSystemUtil {
}
fun main() {
    SystemUtil.setEnv("FAVORITE_COLOR", "Blue")
    println("Favorite color = ${SystemUtil.getEnvVariable("FAVORITE_COLOR")}")
    val dotenv = dotenv()
    for (e in dotenv.entries()) {
        println("dotenv  ${e.key} = ${e.value}")
    }
    println("Single fetch = ${SystemUtil.getEnvVariable("NEO4J_HOME")}}")

}