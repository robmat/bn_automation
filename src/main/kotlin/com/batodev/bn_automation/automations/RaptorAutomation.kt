package com.batodev.bn_automation.automations

import org.apache.logging.log4j.LogManager

class RaptorAutomation : AnimalAutomation(LogManager.getLogger(RaptorAutomation::class.java)) {
    override fun getEncounterPatternPath(): String {
        return "/patterns/raptor_encounter.png"
    }
}
