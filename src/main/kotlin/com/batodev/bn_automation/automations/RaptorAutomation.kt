package com.batodev.bn_automation.automations

import com.batodev.bn_automation.imagematch.ImageMatcher
import com.batodev.bn_automation.imagematch.screenshot
import com.batodev.bn_automation.logging.logInfo
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Robot
import java.awt.event.InputEvent
import javax.imageio.ImageIO

class RaptorAutomation {
    val logger: Logger = LogManager.getLogger(RaptorAutomation::class.java)

    fun automate() {
        click("/patterns/raptor_encounter.png")
        click("/patterns/available_button.png")
        click("/patterns/aircraft_button.png")
        click("/patterns/pelican_pick.png", 1)
        click("/patterns/pelican_pick.png", 1)
        click("/patterns/pelican_pick.png", 1)
        click("/patterns/fight_button.png")
        while (!ifThereClickIt("/patterns/ok_button.png", 1, 1, 0.05f)) {
            drag("/patterns/crosshair_icon.png", "/patterns/raptor_unit.png", 3, 2, 0.1f)
            click("/patterns/crosshair_icon.png")
            click("/patterns/pass_button.png")
            click("/patterns/pass_button.png")
            click("/patterns/pass_button.png")
            click("/patterns/pass_button.png")
        }
        click("/patterns/green_ok_button.png")
    }

    fun drag(fromPatternPath: String, toPatternPath: String, wait: Int = 2, step: Int = 3, maxConfidence: Float = 0.2f) {
        Thread.sleep(wait * 1000L)
        val screenshot = screenshot()
        val fromImage = ImageIO.read(this::class.java.getResourceAsStream(fromPatternPath))
        val toImage = ImageIO.read(this::class.java.getResourceAsStream(toPatternPath))
        val fromMatch = ImageMatcher.find(fromImage, screenshot, step, maxConfidence)
        val toMatch = ImageMatcher.find(toImage, screenshot, step, maxConfidence)
        logInfo("$fromPatternPath match at (${fromMatch.x}, ${fromMatch.y}) with score ${fromMatch.score}", logger)
        logInfo("$toPatternPath match at (${toMatch.x}, ${toMatch.y}) with score ${toMatch.score}", logger)
        val robot = Robot()
        robot.mouseMove(fromMatch.x, fromMatch.y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        Thread.sleep(500L)
        robot.mouseMove(toMatch.x, toMatch.y)
        Thread.sleep(500L)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        logInfo("Dragged from (${fromMatch.x}, ${fromMatch.y}) to (${toMatch.x}, ${toMatch.y})", logger)
    }

    fun click(patternImagePath: String, wait: Int = 3, step: Int = 3, maxConfidence: Float = 0.2f) {
        Thread.sleep(wait * 1000L)
        val screenshot = screenshot()
        val raptorEncounter = ImageIO.read(this::class.java.getResourceAsStream(patternImagePath))
        val match = ImageMatcher.find(raptorEncounter, screenshot, step, maxConfidence)
        logInfo("$patternImagePath match at (${match.x}, ${match.y}) with score ${match.score}", logger)
        val robot = Robot()
        robot.mouseMove(match.x, match.y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        Thread.sleep(500L)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        logInfo("Clicked at (${match.x}, ${match.y})", logger)
    }

    fun ifThereClickIt(patternImagePath: String, wait: Int = 3, step: Int = 3, maxConfidence: Float = 0.2f): Boolean {
        Thread.sleep(wait * 1000L)
        val screenshot = screenshot()
        val patternImage = ImageIO.read(this::class.java.getResourceAsStream(patternImagePath))
        val match = ImageMatcher.find(patternImage, screenshot, step, 1.0f) // Use high maxConfidence to always get a result
        logInfo("$patternImagePath match at (${match.x}, ${match.y}) with score ${match.score}", logger)
        if (match.score <= maxConfidence) {
            val robot = Robot()
            robot.mouseMove(match.x, match.y)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            Thread.sleep(500L)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            logInfo("Clicked at (${match.x}, ${match.y})", logger)
            return true
        } else {
            logInfo("No confident match for $patternImagePath (score: ${match.score}, threshold: $maxConfidence)", logger)
            return false
        }
    }
}
