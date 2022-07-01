package org.batteryparkdev.io

import java.nio.file.Paths

/*
 Test using a small sample file
  */
fun main() {
    val path = Paths.get("./data/cancer_gene_census.csv")
    println("Processing csv file ${path.fileName}")
    CsvRecordSequenceSupplier(path).get().take(100)
        .forEach {println(it) }
}