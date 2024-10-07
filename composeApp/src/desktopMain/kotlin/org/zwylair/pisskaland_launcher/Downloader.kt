package org.zwylair.pisskaland_launcher

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import okhttp3.*
import sun.java2d.pipe.NullPipe

class Downloader(val url: String, val outputFile: File) {
    private val client = OkHttpClient()

    fun startDownload(
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onError(e) }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError(IOException("Failed to download file: ${response.message}"))
                    return
                }

                println("Downloading: ${outputFile.name} with url: $url")
                if (outputFile.parent != null) { File(outputFile.parent).mkdirs() }
                val fileLength = response.body?.contentLength() ?: -1
                val inputStream = response.body?.byteStream()
                val outputStream = FileOutputStream(outputFile)

                try {
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Long = 0
                    var read: Int

                    while (inputStream?.read(buffer).also { read = it!! } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesRead += read
                        if (fileLength > 0) {
                            val progress = (bytesRead.toFloat() / fileLength.toFloat()) * 100
                            onProgress(progress / 100f)
                        }
                    }

                    outputStream.flush()
                    onComplete()
                } catch (e: Exception) {
                    onError(e)
                } finally {
                    outputStream.close()
                    inputStream?.close()
                }
            }
        })
    }
}
