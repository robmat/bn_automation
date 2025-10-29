package com.batodev.bn_automation

import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

fun main() {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val rectangle = Rectangle(screenSize)
    val robot = Robot()
    val image: BufferedImage = robot.createScreenCapture(rectangle)
    ImageIO.write(image, "png", File("screenshot.png"))
}