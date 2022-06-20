package org.batteryparkdev.io

import java.nio.file.Paths
import kotlin.streams.asSequence

fun main(args: Array<String>) {

   val tsvPath = Paths.get("/Volumes/SSD870/COSMIC_rel96/sample/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    //val tsvPath = Paths.get("./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    println("+++++++ Test Process TSV file: ${tsvPath.toString()}")
    var rowNumber = 0
    UTF16CSVRecordSupplier(tsvPath).get().asSequence().forEach {
        rowNumber += 1
        println("TSV record: $rowNumber  ${it.get("GENE_NAME")}")
    }

}