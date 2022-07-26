package org.batteryparkdev.io

import arrow.core.Either
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.streams.asSequence

/**
 * Inline class and supporting functions to validate that a specified file path name
 * is valid for the local file system
 * n.b. specified file path names must be absolute (e.g. /tmp/xyz.txt not xyz.txt)
 * Imported from legacy project by fcriscuo on 2022Jul28
 */
@JvmInline
value class RefinedFilePath (val filePathName: String) {

    companion object: Refined<String> {
        override fun isValid(filePathName: String): Boolean {
            val dirPath = Paths.get(FilenameUtils.getFullPathNoEndSeparator(filePathName))
            val fileName = FilenameUtils.getName(filePathName)
            val filePrefix = FilenameUtils.getPrefix(filePathName)
            return (filePrefix == File.separator && fileName != null
                && Files.isWritable(dirPath))
        }
    }

    fun readFileAsStream(): Stream<String> = Files.lines(this.getPath())

    fun readFileAsSequence(): Sequence<String> =readFileAsStream().asSequence()

    fun getPath(): Path = Paths.get(filePathName)

    fun exists():Boolean = File(filePathName).exists()

    fun deleteFile(): Either<Exception, String> {
        try {
            Files.deleteIfExists(this.getPath())
            return Either.Right("$filePathName has been deleted")
        } catch (e: Exception){
            return Either.Left(e)
        }
    }
}

fun String.asRefinedFilePath(): RefinedFilePath? =
    if(RefinedFilePath.isValid(this)) RefinedFilePath(this)
    else null