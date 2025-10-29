package com.batodev.bn_automation.imagematch

import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

fun screenshot(): BufferedImage {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val rectangle = Rectangle(screenSize)
    val robot = Robot()
    val image: BufferedImage = robot.createScreenCapture(rectangle)
    return image
}