package com.example.starwarscharacters.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.starwarscharacters.domain.repository.ProfilePhotoRepository
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class ProfilePhotoRepositoryImpl(
    context: Context
) : ProfilePhotoRepository {

    private val applicationContext = context.applicationContext

    private val authority =
        "${applicationContext.packageName}.fileprovider"

    override suspend fun createCameraPhoto(): String =
        withContext(Dispatchers.IO) {
            val file = File.createTempFile(
                "camera_",
                ".jpg",
                photoDirectory()
            )

            try {
                FileProvider.getUriForFile(
                    applicationContext,
                    authority,
                    file
                ).toString()
            } catch (error: Throwable) {
                file.delete()
                throw error
            }
        }

    override suspend fun importPhoto(
        sourceUri: String
    ): String = withContext(Dispatchers.IO) {
        val resolver = applicationContext.contentResolver
        val uri = Uri.parse(sourceUri)
        val importContext = currentCoroutineContext()

        require(uri.scheme == "content") {
            "Не удалось получить выбранную фотографию"
        }

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Проверяем именно открытие потока.
        val boundsInput = resolver.openInputStream(uri)
            ?: throw IOException(
                "Не удалось открыть фотографию"
            )

        // Возвращаемый null здесь нормален:
        // результат проверки находится в bounds.
        boundsInput.use { input ->
            BitmapFactory.decodeStream(
                input,
                null,
                bounds
            )
        }

        require(
            bounds.outWidth > 0 &&
                    bounds.outHeight > 0
        ) {
            "Выбранный файл не удалось распознать как фотографию"
        }

        importContext.ensureActive()

        val extension = bounds.outMimeType?.let { mimeType ->
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
        } ?: "img"

        val targetFile = File.createTempFile(
            "avatar_",
            ".$extension",
            photoDirectory()
        )

        try {
            val photoInput = resolver.openInputStream(uri)
                ?: throw IOException(
                    "Не удалось прочитать фотографию"
                )

            photoInput.use { input ->
                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (true) {
                        importContext.ensureActive()

                        val count = input.read(buffer)
                        if (count == -1) break

                        output.write(buffer, 0, count)
                    }
                }
            }

            importContext.ensureActive()

            if (targetFile.length() == 0L) {
                throw IOException(
                    "Выбранная фотография оказалась пустой"
                )
            }

            FileProvider.getUriForFile(
                applicationContext,
                authority,
                targetFile
            ).toString()
        } catch (error: Throwable) {
            targetFile.delete()
            throw error
        }
    }

    override suspend fun discardPhoto(uri: String) {
        withContext(Dispatchers.IO) {
            val parsedUri = Uri.parse(uri)

            if (
                parsedUri.scheme != "content" ||
                parsedUri.authority != authority
            ) {
                return@withContext
            }

            val fileName = parsedUri.lastPathSegment
                ?: return@withContext

            val directory = photoDirectory().canonicalFile
            val file = File(directory, fileName).canonicalFile

            if (file.parentFile == directory) {
                file.delete()
            }
        }
    }

    private fun photoDirectory(): File {
        val directory = File(
            applicationContext.filesDir,
            "profile_photos"
        )

        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException(
                "Не удалось создать папку для фотографий"
            )
        }

        return directory
    }
}