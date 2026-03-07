package com.batodev.bn_automation

import com.batodev.bn_automation.automations.BoarAutomation
import com.batodev.bn_automation.automations.MammothAutomation
import com.batodev.bn_automation.automations.RaptorAutomation
import com.batodev.bn_automation.logging.logInfo
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Dimension
import java.awt.Robot
import java.awt.Toolkit

val automation = MammothAutomation()
val logger: Logger = LogManager.getLogger("main")

fun main() {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    wheelDown(screenSize)
    scrollToTop(screenSize)
    logInfo("Scrolled to top", logger)
    if (checkForAnimalEncounters()) {
        fightAnimalsUntilVisible()
        scrollToTop(screenSize)
    } else {
        scrollToBottom(screenSize)
        logInfo("Scrolled to bottom", logger)
        if (checkForAnimalEncounters()) {
            fightAnimalsUntilVisible()
            scrollToBottom(screenSize)
        }
    }
}

private fun scrollToBottom(screenSize: Dimension) {
    Thread.sleep(2000)
    automation.dragMouse(screenSize.width / 2, screenSize.height - 1, screenSize.width / 2, 0)
}

private fun scrollToTop(screenSize: Dimension) {
    Thread.sleep(2000)
    automation.dragMouse(screenSize.width / 2, 0, screenSize.width / 2, screenSize.height - 1)
}

private fun fightAnimalsUntilVisible() {
    automation.automate()
    while (checkForAnimalEncounters()) {
        automation.automate()
    }
}

private fun checkForAnimalEncounters(): Boolean {
    logInfo("Starting check for animal encounters", logger)
    Thread.sleep(2000)
    val falsePositives = mutableListOf<Pair<Int, Int>>()
    for (i in 0..9) {
        val confidence = 0.08f + i * 0.01f
        logInfo("Attempt ${i + 1}: trying confidence $confidence, excluded ${falsePositives.size} regions", logger)
        val match = automation.isVisible(automation.getEncounterPatternPath(), 1, confidence, falsePositives)
        if (match != null) {
            automation.clickXY(match.x, match.y)
            val fightVisible = automation.isVisible("/patterns/fight_button.png", 1)
            if (fightVisible != null) {
                logInfo("Animal encounter found - fight button visible", logger)
                return true
            }
            logInfo("False positive at (${match.x}, ${match.y}), dismissing", logger)
            falsePositives.add(Pair(match.x, match.y))
            automation.clickXY(10, 10) // click empty area to dismiss any popup
            Thread.sleep(1000)
        }
    }
    logInfo("No animal encounters found", logger)
    return false
}

private fun wheelDown(screenSize: Dimension, amount: Int = 10) {
    val robot = Robot()
    val x = screenSize.width / 2
    val y = screenSize.height / 2
    robot.mouseMove(x, y)
    repeat(amount) {
        robot.mouseWheel(1)
        Thread.sleep(30)
    }
}
