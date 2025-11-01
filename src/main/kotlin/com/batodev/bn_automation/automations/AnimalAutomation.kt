package com.batodev.bn_automation.automations

import com.batodev.bn_automation.imagematch.ImageMatcher
import com.batodev.bn_automation.imagematch.screenshot
import com.batodev.bn_automation.logging.logInfo
import org.apache.logging.log4j.Logger
import java.awt.Robot
import java.awt.event.InputEvent
import javax.imageio.ImageIO

abstract class AnimalAutomation(protected val logger: Logger) {
    companion object {
        const val DEFAULT_CLICK_WAIT = 600L
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
        dragMouse(fromMatch.x, fromMatch.y, toMatch.x, toMatch.y)
    }

    fun dragMouse(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val robot = Robot()
        robot.mouseMove(fromX, fromY)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        // Stepwise drag for more human-like movement
        val steps = 20
        val dx = (toX - fromX).toDouble() / steps
        val dy = (toY - fromY).toDouble() / steps
        for (i in 1..steps) {
            val nx = (fromX + dx * i).toInt()
            val ny = (fromY + dy * i).toInt()
            robot.mouseMove(nx, ny)
            Thread.sleep(15)
        }
        Thread.sleep(DEFAULT_CLICK_WAIT)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        logInfo("Dragged from ($fromX, $fromY) to ($toX, $toY)", logger)
    }

    fun click(patternImagePath: String, wait: Int = 3, step: Int = 3, maxConfidence: Float = 0.2f) {
        Thread.sleep(wait * 1000L)
        val screenshot = screenshot()
        val raptorEncounter = ImageIO.read(this::class.java.getResourceAsStream(patternImagePath))
        try {
            val match = ImageMatcher.find(raptorEncounter, screenshot, step, maxConfidence)
            val x = match.x
            val y = match.y
            logInfo("$patternImagePath match at ($x, $y) with score ${match.score}", logger)
            clickXY(x, y)
        } catch (e: IllegalStateException) {
            throw AutomationException("Could not find pattern $patternImagePath on screen with confidence <= $maxConfidence", e)
        }
    }

    fun clickXY(x: Int, y: Int, wait: Int = 0) {
        Thread.sleep(1000L * wait)
        val robot = Robot()
        robot.mouseMove(x, y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        Thread.sleep(DEFAULT_CLICK_WAIT)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        logInfo("Clicked at ($x, $y)", logger)
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
            Thread.sleep(DEFAULT_CLICK_WAIT)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            logInfo("Clicked at (${match.x}, ${match.y})", logger)
            return true
        } else {
            logInfo("No confident match for $patternImagePath (score: ${match.score}, threshold: $maxConfidence)", logger)
            return false
        }
    }

    fun automate() {
        performEncounterDragSequence()
        performUnitSelectionAndFight()
        performBattleLoop()
        performFinalOkClick()
    }

    private fun performEncounterDragSequence() {
        Thread.sleep(2000)
        dragMouse(1850, 970, 100, 970)
        Thread.sleep(500)
        dragMouse(1850, 970, 100, 970)
    }

    private fun performUnitSelectionAndFight() {
        click("/patterns/heavy_tank_pick.png", 3)
        click("/patterns/heavy_tank_pick.png", 1)
        click("/patterns/heavy_tank_pick.png", 1)
        click("/patterns/umg_pick.png", 1)
        click("/patterns/umg_pick.png", 1)
        click("/patterns/umg_pick.png", 1)
        click("/patterns/fight_button.png")
    }

    private fun performBattleLoop() {
        while (!ifThereClickIt("/patterns/orange_ok_button.png", 1, 1, 0.05f)) {
            clickXY(537, 548, 6) // select umg 1
            clickXY(924, 335, 1) // fire
            clickXY(701, 623, 6) // select umg 2
            clickXY(924, 335, 1) // fire
            clickXY(824, 714, 6) // select umg 3
            clickXY(924, 335, 1) // fire
        }
    }

    open fun performFinalOkClick() {
        click("/patterns/green_ok_button.png", 1, 1, 0.05f)
    }

    abstract fun getEncounterPatternPath(): String
}
