package org.batteryparkdev.io

import java.nio.file.Paths

fun main(args: Array<String>) {
    val csvFilename = if (args.isNotEmpty()) args[0] else "./data/cancer_gene_census.csv"
    val csvPath = Paths.get(csvFilename)
    println("++++++++ Test Processing CSV file: $csvFilename")
    CSVRecordSupplier(csvPath).get().forEach {
        println("CSV Record: ${it.get("Gene Symbol")}")
    }
    val tsvPath = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
    println("+++++++ Test Process TSV file: ${tsvPath.toString()}")

    var rowNumber = 0
    CSVRecordSupplier(tsvPath).get().forEach {
        rowNumber += 1
        println("TSV record: $rowNumber  ${it.get("Gene name")}")
    }

}