package com.batodev.bn_automation

import com.batodev.bn_automation.automations.BoarAutomation
import com.batodev.bn_automation.automations.RaptorAutomation

fun main() {
    for (i in 1..5) {
        //BoarAutomation().automate()
        RaptorAutomation().automate()
    }
}
