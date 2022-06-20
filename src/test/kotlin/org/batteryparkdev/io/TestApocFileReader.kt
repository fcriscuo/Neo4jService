package org.batteryparkdev.io

class TestApocFileReader {
}
fun main() {
    // test reading a CSV file from local data file
    ApocFileReader.processDelimitedFile("/Volumes/Sea5TBExt/COSMIC_rel96/cancer_gene_census.csv")
        .map { record -> record.get("map") }
        .forEach {  value -> println("Gene symbol: ${value["Gene Symbol"]}") }
}