package com.batodev.bn_automation

import com.batodev.bn_automation.automations.BoarAutomation
import com.batodev.bn_automation.automations.RaptorAutomation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Toolkit

val automation = BoarAutomation()
val logger: Logger = LogManager.getLogger("Main")

fun main() {
    if (checkForAnimalEncounters(logger)) {
        fightAnimalsUntilVisible(logger)
    } else {
        // Drag mouse from top to bottom
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        automation.dragMouse(screenSize.width / 2, 0, screenSize.width / 2, screenSize.height - 1)
        if (checkForAnimalEncounters(logger)) {
            fightAnimalsUntilVisible(logger)
        } else {
            // Drag mouse from bottom to top
            automation.dragMouse(screenSize.width / 2, screenSize.height - 1, screenSize.width / 2, 0)
            if (checkForAnimalEncounters(logger)) {
                fightAnimalsUntilVisible(logger)
            }
        }
    }
}

private fun fightAnimalsUntilVisible(logger: Logger) {
    automation.automate()
    while (checkForAnimalEncounters(logger)) {
        automation.automate()
    }
}

private fun checkForAnimalEncounters(logger: Logger): Boolean =
    automation.ifThereClickIt(automation.getEncounterPatternPath(), 1, 1, 0.06f)
