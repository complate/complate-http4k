package com.github.complate

import org.junit.Assert.assertEquals
import org.junit.Test

class Http4kAdapterTest {

  @Test
  fun complateStream_flushOnEmptyStream_streamIsEmpty() {
    val stream = ComplateStreamImpl()
    stream.flush()
    assertEquals(stream.toString(), "")
  }

  @Test
  fun complateStream_writeValue_stringEqualsValue() {
    val stream = ComplateStreamImpl()
    stream.write("test")
    stream.flush()
    assertEquals(stream.toString(), "test")
  }

  @Test
  fun complateStream_writelnValue_stringEqualsValueWithNewLine() {
    val stream = ComplateStreamImpl()
    stream.writeln("test")
    stream.flush()
    assertEquals(stream.toString(), "test" + System.getProperty("line.separator"))
  }
}
