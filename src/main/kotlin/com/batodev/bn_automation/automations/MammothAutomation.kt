package com.batodev.bn_automation.automations

import org.apache.logging.log4j.LogManager

class MammothAutomation : AnimalAutomation(LogManager.getLogger(MammothAutomation::class.java)) {
    override fun getEncounterPatternPath(): String {
        return "/patterns/mammoth_encounter.png"
    }
}
