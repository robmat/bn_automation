package com.batodev.bn_automation.logging

import org.apache.logging.log4j.Logger

fun logInfo(message: String, logger: Logger) {
    logger.info(message)
    println(message)
}

fun logDebug(message: String, logger: Logger) {
    logger.debug(message)
    println(message)
}