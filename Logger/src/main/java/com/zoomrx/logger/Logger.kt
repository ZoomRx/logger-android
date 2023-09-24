package com.zoomrx.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.nio.charset.Charset

object Logger {
    private var mLogger: org.slf4j.Logger? = null
    const val LOG_PREFIX = "Zlog"
    private val transports = arrayListOf<Transport>()

    fun addTransport(transport: Transport) {
        transports.add(transport)
    }

    fun configureLogger(logDirectoryPath: String) {

        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()

        val rollingFileAppender = RollingFileAppender<ILoggingEvent>()
        rollingFileAppender.context = loggerContext
        rollingFileAppender.isAppend = true
        rollingFileAppender.file = "$logDirectoryPath/$LOG_PREFIX-latest.log"

        val rollingPolicy = FixedWindowRollingPolicy()
        rollingPolicy.context = loggerContext
        rollingPolicy.setParent(rollingFileAppender)
        rollingPolicy.fileNamePattern = "$logDirectoryPath/$LOG_PREFIX.%i.log"
        rollingPolicy.minIndex = 1
        rollingPolicy.maxIndex = 3
        rollingPolicy.start()

        val triggeringPolicy = SizeBasedTriggeringPolicy<ILoggingEvent>()
        triggeringPolicy.context = loggerContext
        triggeringPolicy.maxFileSize = FileSize.valueOf("512KB")
        triggeringPolicy.start()

        val encoder = PatternLayoutEncoder()
        encoder.context = loggerContext
        encoder.charset = Charset.forName("UTF-8")
        encoder.pattern = "%date %level [%thread] %msg%n"
        encoder.start()

        val consoleEncoder = PatternLayoutEncoder()
        consoleEncoder.context = loggerContext
        consoleEncoder.charset = Charset.forName("UTF-8")
        consoleEncoder.pattern = "%level [%thread] %msg%n"
        consoleEncoder.start()

        rollingFileAppender.triggeringPolicy = triggeringPolicy
        rollingFileAppender.encoder = encoder
        rollingFileAppender.rollingPolicy = rollingPolicy
        rollingFileAppender.start()

        val logcatAppender = LogcatAppender()
        logcatAppender.context = loggerContext
        logcatAppender.encoder = consoleEncoder
        logcatAppender.start()

        // add the newly created appender to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.level = Level.DEBUG
        root.addAppender(rollingFileAppender)
        root.addAppender(logcatAppender)

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(loggerContext)
        mLogger = LoggerFactory.getLogger(LOG_PREFIX)
    }

    fun debug(message: String) {
        mLogger?.debug(message)
        transports.forEach {
            it.debug(message)
        }
//        Log.d(LOG_PREFIX, message)
    }

    fun info(message: String) {
        mLogger?.info(message)
        transports.forEach {
            it.info(message)
        }
//        Log.i(LOG_PREFIX, message)
    }

    fun warn(message: String) {
        mLogger?.warn(message)
        transports.forEach {
            it.warn(message)
        }
//        Log.w(LOG_PREFIX, message)
    }

    fun warn(exception: Exception) {
        mLogger?.error(exception.message)
        transports.forEach {
            it.warn(exception)
        }
//        Log.w(LOG_PREFIX, exception)
    }

    fun error(message: String) {
        mLogger?.error(message)
        transports.forEach {
            it.error(message)
        }
//        Log.e(LOG_PREFIX, message)
    }

    fun error(error: Error) {
        mLogger?.error(error.message)
        transports.forEach {
            it.error(error)
        }
//        Log.e(LOG_PREFIX, error.message, error)
    }
}