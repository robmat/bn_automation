package com.batodev.bn_automation

import com.batodev.bn_automation.automations.BoarAutomation
import com.batodev.bn_automation.automations.RaptorAutomation
import java.awt.Dimension
import java.awt.Toolkit

val automation = RaptorAutomation()

fun main() {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    wheelDown(screenSize)
    scrollToTop(screenSize)
    if (checkForAnimalEncounters()) {
        fightAnimalsUntilVisible()
        scrollToTop(screenSize)
    } else {
        scrollToBottom(screenSize)
        if (checkForAnimalEncounters()) {
            fightAnimalsUntilVisible()
            scrollToBottom(screenSize)
        }
    }
}

private fun scrollToBottom(screenSize: Dimension) {
    automation.dragMouse(screenSize.width / 2, screenSize.height - 1, screenSize.width / 2, 0)
}

private fun scrollToTop(screenSize: Dimension) {
    automation.dragMouse(screenSize.width / 2, 0, screenSize.width / 2, screenSize.height - 1)
}

private fun fightAnimalsUntilVisible() {
    automation.automate()
    while (checkForAnimalEncounters()) {
        automation.automate()
    }
}

private fun checkForAnimalEncounters(): Boolean =
    automation.ifThereClickIt(automation.getEncounterPatternPath(), 1, 1, 0.055f)

private fun wheelDown(screenSize: Dimension, amount: Int = 10) {
    val robot = java.awt.Robot()
    val x = screenSize.width / 2
    val y = screenSize.height / 2
    robot.mouseMove(x, y)
    repeat(amount) {
        robot.mouseWheel(1)
        Thread.sleep(30)
    }
}
