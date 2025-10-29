package com.batodev.bn_automation.imagematch

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.platform.commons.logging.Logger
import org.junit.platform.commons.logging.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.stream.Stream
import javax.imageio.ImageIO

class ImageMatcherTest {
    val logger: Logger = LoggerFactory.getLogger(ImageMatcherTest::class.java)

    companion object {
        @JvmStatic
        fun imagePairsProvider(): Stream<Array<Any>> = Stream.of(
            arrayOf("raptor_nest.png", "raptor_encounter.png", 15),
            arrayOf("raptor_nest.png", "missions.png", 15),
            arrayOf("sample_big.png", "sample.png", 1),
            arrayOf("raptor_battle.png", "raptor_encounter.png", 15),
            arrayOf("blank.png", "black.png", 15),
        )
    }

    @ParameterizedTest
    @MethodSource("imagePairsProvider")
    fun find(haystackPath: String, needlePath: String, radius: Int) {
        var imageStream = javaClass.getResourceAsStream("/" + haystackPath)
        val haystack: BufferedImage = ImageIO.read(imageStream)
        imageStream = javaClass.getResourceAsStream("/" + needlePath)
        val needle: BufferedImage = ImageIO.read(imageStream)

        val result = ImageMatcher.find(needle, haystack)
        logger.info { "Match found at (${result.x}, ${result.y}) with score ${result.score}" }

        // Draw a green dot at the match location
        val g2d = haystack.createGraphics()
        g2d.color = Color.GREEN
        g2d.fillOval(result.x - radius, result.y - radius, radius * 2, radius * 2)
        g2d.dispose()

        // Output the haystack image with the dot to the build folder
        val outputFile = File("build/${haystackPath}_${needlePath}")
        ImageIO.write(haystack, "png", outputFile)
        logger.info { "Output image written to: ${outputFile.absolutePath}" }
    }
}