package org.batteryparkdev.io

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier
import java.util.stream.Stream

/*
Represents a Supplier that will parse records from either a TSV or CSV file
into a CSVRecord object
Designed to handle very large COSMIC files
TODO: refactor deprecated options
 */
class CSVRecordSupplier() : Supplier<Stream<CSVRecord>> {


    private var recordStream: Stream<CSVRecord> = Stream.empty<CSVRecord?>()
    constructor (aPath: Path) : this() {
        val reader = Files.newBufferedReader(aPath)
        val utf16charset = Charset.forName("UTF-16")
        val parser = when(aPath.toString().endsWith("tsv")) {
              true -> CSVParser.parse(
                 reader,
                CSVFormat.TDF.withFirstRecordAsHeader().withQuote(null).withIgnoreEmptyLines())
            false -> CSVParser.parse(
                reader,
                CSVFormat.RFC4180.withFirstRecordAsHeader())
        }
            recordStream = parser.stream()
    }
    override fun get(): Stream<CSVRecord> = recordStream
}


