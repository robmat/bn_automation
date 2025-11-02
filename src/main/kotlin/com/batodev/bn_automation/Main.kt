package com.batodev.bn_automation

import com.batodev.bn_automation.automations.BoarAutomation
import java.awt.Dimension
import java.awt.Toolkit

val automation = BoarAutomation()

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
    Thread.sleep(4000)
    return automation.ifThereClickIt(automation.getEncounterPatternPath(), 1, 0.03f)
}

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
