package com.zoomrx.logger

import java.lang.Exception

interface Transport {

    fun debug(message: String)

    fun info(message: String)

    fun warn(message: String)

    fun warn(exception: Exception)

    fun error(message: String)

    fun error(error: Error)
}