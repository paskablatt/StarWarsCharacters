package com.example.starwarscharacters.data.repository

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.starwarscharacters.domain.DownloadedDocument
import com.example.starwarscharacters.domain.repository.ResumeRepository
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class ResumeRepositoryImpl(
    context: Context
) : ResumeRepository {

    private val applicationContext = context.applicationContext

    override suspend fun downloadResume(
        url: String
    ): DownloadedDocument = withContext(Dispatchers.IO) {
        val downloadContext = currentCoroutineContext()

        var connection: HttpURLConnection? = null
        var downloadedFile: File? = null

        try {
            var currentUrl = URL(url)
            var redirectCount = 0

            while (true) {
                downloadContext.ensureActive()

                require(
                    currentUrl.protocol.equals(
                        "https",
                        ignoreCase = true
                    ) ||
                            currentUrl.protocol.equals(
                                "http",
                                ignoreCase = true
                            )
                ) {
                    "Поддерживаются только ссылки http:// и https://"
                }

                val activeConnection =
                    currentUrl.openConnection() as HttpURLConnection

                connection = activeConnection

                activeConnection.apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = false
                }

                val responseCode = activeConnection.responseCode

                if (
                    responseCode in listOf(301, 302, 303, 307, 308)
                ) {
                    val location =
                        activeConnection.getHeaderField("Location")
                            ?: throw IOException(
                                "Сервер не указал адрес перенаправления"
                            )

                    if (redirectCount >= 5) {
                        throw IOException(
                            "Слишком много перенаправлений"
                        )
                    }

                    currentUrl = URL(currentUrl, location)
                    redirectCount++

                    activeConnection.disconnect()
                    connection = null
                    continue
                }

                if (responseCode !in 200..299) {
                    throw IOException(
                        "Ошибка скачивания: HTTP $responseCode"
                    )
                }

                val reportedMimeType =
                    activeConnection.contentType
                        ?.substringBefore(';')
                        ?.trim()
                        ?.lowercase(Locale.ROOT)

                val mimeType = if (
                    reportedMimeType.isNullOrBlank() ||
                    reportedMimeType == "application/octet-stream"
                ) {
                    URLConnection.guessContentTypeFromName(
                        currentUrl.path
                    ) ?: "application/octet-stream"
                } else {
                    reportedMimeType
                }

                if (
                    mimeType == "text/html" ||
                    mimeType == "application/xhtml+xml"
                ) {
                    throw IOException(
                        "По ссылке открывается веб-страница. " +
                                "Укажите прямую ссылку на файл резюме."
                    )
                }

                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType)
                    ?: "bin"

                val directory = File(
                    applicationContext.cacheDir,
                    "resumes"
                )

                if (!directory.exists() && !directory.mkdirs()) {
                    throw IOException(
                        "Не удалось создать папку для резюме"
                    )
                }

                val targetFile = File.createTempFile(
                    "resume_",
                    ".$extension",
                    directory
                )

                downloadedFile = targetFile

                activeConnection.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                        while (true) {
                            downloadContext.ensureActive()

                            val count = input.read(buffer)
                            if (count == -1) break

                            output.write(buffer, 0, count)
                        }
                    }
                }

                downloadContext.ensureActive()

                if (targetFile.length() == 0L) {
                    throw IOException(
                        "Сервер вернул пустой файл"
                    )
                }

                val contentUri = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.fileprovider",
                    targetFile
                )

                return@withContext DownloadedDocument(
                    uri = contentUri.toString(),
                    mimeType = mimeType
                )
            }

            @Suppress("UNREACHABLE_CODE")
            throw IOException("Не удалось скачать резюме")
        } catch (error: Throwable) {
            downloadedFile?.delete()
            throw error
        } finally {
            connection?.disconnect()
        }
    }
}