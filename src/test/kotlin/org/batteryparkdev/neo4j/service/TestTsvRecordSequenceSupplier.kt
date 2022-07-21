package org.batteryparkdev.neo4j.service

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CsvHeaderSupplier
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths
import java.util.function.Consumer

fun main(args: Array<String>) {
    val filePathName = if (args.isNotEmpty()) args[0] else "./data/sample_CosmicMutantExportCensus.tsv"
    val aPath = Paths.get(filePathName)
    println("Processing delimited file: $filePathName")
    val headerMap = CsvHeaderSupplier(aPath).get()
    TsvRecordSequenceSupplier(aPath).get()
        .take(100)
        .forEach { record: CSVRecord ->
            headerMap?.keys?.forEach(Consumer { key: String ->
                println("*** column: $key  value= ${record[key]}")
            })
        }
}

