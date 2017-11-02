package com.github.complate

import org.http4k.template.TemplateRenderer
import org.http4k.template.Templates
import org.http4k.template.ViewModel
import java.io.IOException
import java.io.StringWriter


class ComplateTemplates(private val configure: (ComplateTemplateEngine) -> ComplateTemplateEngine = { it }) : Templates {
  override fun Caching(baseTemplateDir: String): TemplateRenderer {
    TODO("not implemented")
  }

  override fun CachingClasspath(baseClasspathPackage: String): TemplateRenderer {
    TODO("not implemented")
  }

  override fun HotReload(baseTemplateDir: String): TemplateRenderer {
    return ComplateTemplateRenderer(configure(ComplateTemplateEngine(baseTemplateDir)))
  }

  private class ComplateTemplateRenderer(private val engine: ComplateTemplateEngine) : TemplateRenderer {
    override fun invoke(viewModel: ViewModel): String {
      val writer = ComplateStreamImpl()
      engine.invoke(template(viewModel), writer, viewModel)
      return writer.toString()
    }

    private fun template(viewModel: ViewModel) = viewModel::class.java.simpleName
  }
}

class ComplateStreamImpl : ComplateStream {
  private val stringWriter = StringWriter()
  private val lineSeparator = System.getProperty("line.separator")

  override fun write(s: String) = stringWriter.write(s)

  override fun writeln(line: String) = write(line + lineSeparator)

  @Throws(IOException::class)
  override fun flush() = stringWriter.flush()

  override fun toString() = stringWriter.toString()
}
