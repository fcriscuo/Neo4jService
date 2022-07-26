package org.batteryparkdev.datamining

import arrow.core.Either
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.batteryparkdev.io.FileFunctions
import org.batteryparkdev.io.RefinedFilePath
import org.batteryparkdev.property.service.FilesPropertyService
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.URL

const val FTP_USER = "anonymous"
// The password for an anonymous ftp connection is, by practice, the user's email
val ftpPassword = FilesPropertyService.ftpUserEmail
const val FTP_PORT = 21

private val logger = KotlinLogging.logger {}

data class FtpClient(val server: String) {
    private val ftp = FTPClient()

    init {
        ftp.addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
        ftp.enterLocalPassiveMode()
    }

    fun downloadRemoteFile(remoteFilePath: String, localFilePath: RefinedFilePath): Either<Exception, String> {
        ftp.connect(server, FTP_PORT)
        val replyCode = ftp.replyCode
        if (FTPReply.isPositiveCompletion(replyCode)) {
            ftp.login(FTP_USER, ftpPassword)
            ftp.setFileType(FTP.ASCII_FILE_TYPE)
            try {
                val outputStream = FileOutputStream(localFilePath.getPath().toFile(),false)
                ftp.retrieveFile(remoteFilePath,outputStream)
                when (localFilePath.exists()){
                    true -> return Either.Right("Remote file: $remoteFilePath has been downloaded to ${localFilePath.filePathName}")
                    false -> return Either.Left(IOException("Download of remote file: $remoteFilePath to ${localFilePath.filePathName} failed"))
                }
            } catch (e: Exception) {
                return Either.Left(e)
            } finally {
                ftp.logout()
                ftp.disconnect()
            }
        }
        return Either.Left(IOException("FTP server $server refused anonymous connection"))
    }

    /*
Function to access a remote file via anonymous FTP and copy its contents to
the local filesystem at a specified location.
Parameters: ftpUrl - Complete URL for remote file
            localFilePath - local filesystem location
Returns: An Either - Left is an Exception, Right is a success message
 */
    fun retrieveRemoteFileByFtpUrl(ftpUrl: String, localFilePath: RefinedFilePath): Either<Exception, String> {
        val urlConnection = URL(ftpUrl)
        urlConnection.openConnection()
        // the FileUtils method closes the input stream
        try {
            FileUtils.copyInputStreamToFile(urlConnection.openStream(), localFilePath.getPath().toFile())
            if (FilenameUtils.getExtension(localFilePath.filePathName) in FileFunctions.compressedFileExtensions) {
                FileFunctions.gunzipFile(localFilePath.filePathName)
            }
            return Either.Right("$ftpUrl downloaded to  $localFilePath")
        } catch (e: Exception) {
            return Either.Left(e)
        }
    }
}
