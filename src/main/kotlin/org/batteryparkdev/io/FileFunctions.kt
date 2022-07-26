package org.batteryparkdev.io

import arrow.core.Either
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.batteryparkdev.property.service.FilesPropertyService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Path
import java.util.zip.GZIPInputStream

/*
Represents a collection of file related utility functions
Copied over from a legacy project
 */


object FileFunctions {
    private val fileSeparator = System.getProperty("file.separator")
    val compressedFileExtensions = listOf<String>("gz", "zip")
    private val baseDataDir = FilesPropertyService.baseDataPath
    private val baseDataSubDirectory = FilesPropertyService.baseDataSubdirectory

    @JvmStatic
            /*
            Function to delete a directory recursively
            */
    fun deleteDirectoryRecursively(path: Path): Either<Exception, String> {
        return try {
            FileUtils.deleteDirectory(path.toFile())
            Either.Right("${path.fileName} and children have been deleted")
        } catch (e: Exception) {
            Either.Left(e)
        }
    }

    /*
  Function to access a remote file via anonymous FTP and copy its contents to
  the local filesystem at a specified location.
  Parameters: ftpUrl - Complete URL for remote file
  Returns: Either whose Left side is an Exception, and whose Right side contains a success message
   */
    fun retrieveRemoteFileByDatafileProperty(propertyPair: Pair<String, String>): Either<Exception, String> {
        val urlConnection = URL(propertyPair.second)
        val localFilePath = resolveLocalFileNameFromPropertyPair(propertyPair)
        urlConnection.openConnection()
        return try {
            FileUtils.copyInputStreamToFile(urlConnection.openStream(), File(localFilePath))
            if (FilenameUtils.getExtension(localFilePath) in compressedFileExtensions) {
                gunzipFile(localFilePath)
            }
            Either.Right("${propertyPair.second} downloaded to  $localFilePath")
        } catch (e: Exception) {
            Either.Left(e)
        }
    }

    fun resolveLocalFileNameFromPropertyPair(propertyPair: Pair<String, String>): String {
        val subdirectory = resolveDataSubDirectoryFromPropertyName(propertyPair.first)
        return subdirectory + fileSeparator + resolveSourceFileName(propertyPair.second)
    }

    fun resolveDataSubDirectoryFromPropertyName(propertyName: String): String {
        if (propertyName.startsWith(FilesPropertyService.baseDataSubdirectory)) {
            return baseDataDir + fileSeparator + propertyName.replace(".", fileSeparator)
        }
        return baseDataDir + fileSeparator + baseDataSubDirectory +
                fileSeparator + propertyName.replace(".", fileSeparator)
    }
    fun resolveSourceFileName(remotePath: String) =
        remotePath.split(fileSeparator).last()

    /*
    unzip a compressed file
    the expanded file is given the same filename without the .gz or .zip extension
    and the compressed file is deleted
    this code is a simple refactoring of a Java example
     */
    //TODO: make this asynchronous
    fun gunzipFile(compressedFile: String): Either<Exception, String> {
        val buffer = ByteArray(1024)
        val expandedFile = FilenameUtils.removeExtension(compressedFile)
        val gzis = GZIPInputStream(FileInputStream(compressedFile))
        val out = FileOutputStream(expandedFile)
        try {
            var len: Int
            while (true) {
                len = gzis.read(buffer)
                if (len > 0) {
                    out.write(buffer, 0, len)
                } else {
                    //delete compressed file
                    FileUtils.forceDelete(File(compressedFile))
                    return Either.Right("$compressedFile expanded to $expandedFile")
                }
            }
        } catch (e: Exception) {
            return Either.Left(e)
        } finally {
            gzis.close()
            out.close()
        }
    }

}