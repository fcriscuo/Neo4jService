package org.batteryparkdev.io

import java.nio.file.Paths

fun main(args: Array<String>) {
    val csvFilename = if (args.isNotEmpty()) args[0] else "./data/cancer_gene_census.csv"
    val csvPath = Paths.get(csvFilename)
    println("++++++++ Test Processing CSV file: $csvFilename")
    CSVRecordSupplier(csvPath).get().forEach {
        println("CSV Record: ${it.get("Gene Symbol")}")
    }
    val tsvPath = Paths.get("./data/sample_CosmicMutantExport.tsv")
    println("+++++++ Test Process TSV file: ${tsvPath.toString()}")
    CSVRecordSupplier(tsvPath).get().forEach {
        println("TSV record: ${it.get("LEGACY_MUTATION_ID")}")
    }

}