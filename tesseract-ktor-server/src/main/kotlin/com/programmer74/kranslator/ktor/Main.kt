package com.programmer74.kranslator.ktor

import com.programmer74.kranslator.ktor.TesseractWrapper.doOCR
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KLogging
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream

object Main : KLogging() {

  /*
    curl -v --header 'X-MAGIC: TSX5w24M9bxjPHu6Tf9VrKr5' \
      --header 'Accept: application/json' -F 'data=@/home/hotaro/test.png' \
      http://127.0.0.1:8080/image/300/de/1/ocr
  */
  @JvmStatic
  fun main(args: Array<String>) {
    val host = getEnvOrDefault("HOST", "127.0.0.1")
    val port = getEnvOrDefault("PORT", "8080").toInt()
    val expectedKey = getEnvOrDefault("KEY", "TSX5w24M9bxjPHu6Tf9VrKr5")
    embeddedServer(Netty, host = host, port = port) {
      install(ContentNegotiation) {
        json(Json {
          prettyPrint = true
          isLenient = true
        })
      }
      routing {
        get("/") {
          call.respondText("Hello, world!")
        }

        post("/image/{dpi}/{lang}/{pil}/ocr") { _ ->
          val dpi = call.parameters["dpi"]
          val lang = call.parameters["lang"]
          val pil = call.parameters["pil"]
          val actualKey = call.request.headers["X-MAGIC"]
          if ((actualKey == null) || (actualKey != expectedKey) || (dpi == null) || (lang == null) || (pil == null)) {
            call.respond(HttpStatusCode.NotFound)
          } else {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
              if (part is PartData.FileItem) {
                val file = withContext(Dispatchers.IO) {
                  Files.createTempFile("img", "ktks.png").toFile()
                }
                part.streamProvider().use { its ->
                  file.outputStream().buffered().use {
                    its.copyTo(it)
                  }
                }
                try {
                  logger.debug { "Requested OCR for $lang with dpi $dpi and pil $pil" }
                  logger.debug { "File $file of size ${Files.size(file.toPath())}" }
                  call.respond(doOCR(file, lang, dpi, pil))
                } catch (e: Exception) {
                  logger.error(e) { "Error on file ${file.absolutePath} " }
                } finally {
                  withContext(Dispatchers.IO) {
                    Files.delete(file.toPath())
                  }
                  call.respond(HttpStatusCode.NotFound)
                }
              }
              part.dispose()
              call.respond(HttpStatusCode.NotFound)
            }
          }
        }
      }
    }.start(wait = true)
  }

  private fun getEnvOrDefault(key: String, default: String): String {
    val env = System.getenv()
    return env.getOrDefault(key, default)
  }
}
