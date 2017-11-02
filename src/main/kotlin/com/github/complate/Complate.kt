package com.github.complate

import jdk.nashorn.api.scripting.NashornException
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.http4k.template.ViewModel
import java.io.*
import java.util.*
import javax.script.ScriptException


interface ComplateStream {
  fun write(s: String)
  fun writeln(line: String)
  @Throws(IOException::class)
  fun flush()
}

class ComplateTemplateEngine(baseDirectory: String) {
  private val engine: NashornScriptEngine = create()
  private val bundlePath = baseDirectory + "/" + "views.js"
  private val functionName = "render"

  private val POLYFILLS = """
      if(typeof global === "undefined") {
        var global = this;
      }
      if(typeof console === "undefined") {
        var console = { log: print, error: print };
      }
    """

  companion object {
    private fun create(): NashornScriptEngine {
      val engine = NashornScriptEngineFactory().scriptEngine
      return if (engine == null) {
        throw ComplateException("Cannot instantiate NashornScriptEngine")
      } else {
        engine as NashornScriptEngine
      }
    }
  }

  @Throws(ComplateException::class)
  fun invoke(view: String, writer: ComplateStream, model: ViewModel) {
    this.invokeImpl(writer, view, model)
  }

  @Throws(ComplateException::class)
  private fun invokeImpl(vararg args: Any) {
    try {
      readerForScript(bundlePath).use { engine.eval(it) }
    } catch (err: Exception) {
      when (err) {
        is IOException -> throw ComplateException.loadingError(bundlePath, err)
        is ScriptException -> throw ComplateException.evalError(bundlePath, extractJavaScriptError(err), err)
        else -> throw err
      }
    }

    try {
      engine.invokeFunction(functionName, *args)
    } catch (err: Exception) {
      when (err) {
        is ScriptException, is NoSuchMethodException -> throw ComplateException.invokeError(bundlePath, functionName,
          extractJavaScriptError(err), err)
        else -> throw err
      }
    }
  }

  @Throws(IOException::class)
  private fun readerForScript(scriptLocation: String): Reader {
    val fileInputStream = FileInputStream(File(scriptLocation))
    val polyfilledStream = prependPolyfills(fileInputStream)
    val inputStreamReader = InputStreamReader(polyfilledStream)
    return BufferedReader(inputStreamReader)
  }

  private fun prependPolyfills(inputStream: InputStream): InputStream {
    val polyfillsIS = ByteArrayInputStream(POLYFILLS.toByteArray())
    return SequenceInputStream(polyfillsIS, inputStream)
  }

  private fun extractJavaScriptError(err: Exception): Optional<String> {
    val cause = err.cause

    return when (cause) {
      is NashornException -> Optional.of(cause.message + "\n" + NashornException.getScriptStackString(cause))
      else -> Optional.empty()
    }
  }
}

class ComplateException : RuntimeException {
  internal constructor(message: String, cause: Throwable) : super(message, cause)
  internal constructor(message: String) : super(message)

  companion object {
    fun loadingError(location: String, err: Exception) =
      ComplateException("failed to read script from resource '$location'", err)

    fun evalError(location: String, jsError: Optional<String>, err: Exception) =
      jsError
        .map {
          ComplateException("failed to evaluate script '$location'\n$it", err)
        }
        .orElseGet {
          ComplateException("failed to evaluate script '$location'", err)
        }

    fun invokeError(location: String, function: String, jsError: Optional<String>, err: Exception) =
      jsError
        .map {
          ComplateException("failed to invoke function '$function' in '$location'\n$it", err)
        }
        .orElseGet {
          ComplateException("failed to invoke function '$function' in '$location'", err)
        }
  }
}
