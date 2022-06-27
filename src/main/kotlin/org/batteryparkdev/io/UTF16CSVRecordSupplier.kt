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
Represents a Supplier that will parse records from a TSV or CSV file with a UTF16 encoding
TODO: refactor deprecated options
 */
class UTF16CSVRecordSupplier() : Supplier<Stream<CSVRecord>> {

    private var recordStream: Stream<CSVRecord> = Stream.empty<CSVRecord?>()
    constructor (aPath: Path) : this() {
        val utf16charset = Charset.forName("UTF-16")
        val parser = when(aPath.toString().endsWith("tsv")) {
              true -> CSVParser.parse(
                 aPath, utf16charset,
                CSVFormat.TDF.withFirstRecordAsHeader().withQuote(null).withIgnoreEmptyLines())
            false -> CSVParser.parse(
                aPath, utf16charset,
                CSVFormat.RFC4180.withFirstRecordAsHeader())
        }
            recordStream = parser.stream()
    }
    override fun get(): Stream<CSVRecord> = recordStream
}


