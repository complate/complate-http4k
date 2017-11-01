# complate HTTP4K Adapter

[![Build Status](https://travis-ci.org/complate/complate-http4k.svg?branch=master)](https://travis-ci.org/complate/complate-http4k)
[![codecov](https://codecov.io/gh/complate/complate-http4k/branch/master/graph/badge.svg)](https://codecov.io/gh/complate/complate-http4k)

This adapter connects [HTTP4K](https://http4k.org) with the templating engine [complate](https://github.com/complate). 

## Usage

### Compiling and Installation

Unless this library becomes available via maven central (or others), it can be built and installed to the local maven repo:

    ./gradlew clean build
    ./gradlew install

### Dependency

Library import:

```gradle
dependencies {
  compile 'com.github.complate:complate-http4k:0.1.0'
}
```

### HTTP4K Example

All that is required to move an existing _HTTP4K_ based application from another templating engine towards complate
can be achieved by switching the templates to `com.github.complate.ComplateTemplates`.
  
Here is a more-or-less complete (working) base that shows how to use.

```gradle
dependencies {
  // Kotlin
  compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
  
  // HTTP4K
  compile "org.http4k:http4k-core:2.35.1"
  compile "org.http4k:http4k-server-netty:2.35.1"
  
  // complate Adapter
  compile 'com.github.complate:complate-http4k:0.1.0'
}
```
  
```kotlin
package com.github.complate.http4k.demo
  
import com.github.complate.ComplateTemplates
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel
import kotlin.concurrent.thread
  
  
val bundles = "src/main/resources/frontend"
  
fun main(args: Array<String>) {
  val app = DemoApplication()
  app.start()
  Runtime.getRuntime().addShutdownHook(thread(start = false) { app.stop() })
}
  
data class Person(val name: String, val age: Int) : ViewModel
  
class DemoApplication {
  private val server: Http4kServer
  private val renderer: TemplateRenderer = ComplateTemplates().HotReload(bundles)
  
  private val routes = org.http4k.routing.routes(
    "/person" bind Method.GET to {
      val viewModel = Person("Bob", 45)
      val renderedView = renderer(viewModel)
      Response(Status.OK).body(renderedView)
    }
  )
  
  init {
    server = routes.asServer(Netty(9000))
  }
  
  fun start() = server.start()
    
  fun stop() = server.stop()
}
```

## TODOs

Caching (file & class path) is not implemented at the moment:

```kotlin
class ComplateTemplates : Templates {
  override fun Caching(baseTemplateDir: String): TemplateRenderer {
    TODO("not implemented")
  }

  override fun CachingClasspath(baseClasspathPackage: String): TemplateRenderer {
    TODO("not implemented")
  }
}
```

This will hopefully added in a future version of the adapter (most likely v0.2.0).
