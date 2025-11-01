package com.batodev.bn_automation.automations

import org.apache.logging.log4j.LogManager

class BoarAutomation : AnimalAutomation(LogManager.getLogger(BoarAutomation::class.java)) {
    override fun getEncounterPatternPath(): String {
        return "/patterns/boar_encounter.png"
    }
    override fun performFinalOkClick() {
        click("/patterns/green_ok_button.png")
        Thread.sleep(5000)
    }
}
