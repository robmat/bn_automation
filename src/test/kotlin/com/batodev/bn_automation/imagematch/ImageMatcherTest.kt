package com.batodev.bn_automation.imagematch

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.platform.commons.logging.Logger
import org.junit.platform.commons.logging.LoggerFactory
import org.opencv.core.Core
import org.opencv.core.Core.MinMaxLocResult
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
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
            arrayOf("boars_map.png", "patterns/boar_encounter.png", 15),
        )
    }

    @ParameterizedTest
    @MethodSource("imagePairsProvider")
    fun find(haystackPath: String, needlePath: String, radius: Int) {
        var imageStream = javaClass.getResourceAsStream("/$haystackPath")
        val haystack: BufferedImage = ImageIO.read(imageStream)
        imageStream = javaClass.getResourceAsStream("/$needlePath")
        val needle: BufferedImage = ImageIO.read(imageStream)

        val result = ImageMatcher.find(needle, haystack)
        logger.info { "Match found at (${result.x}, ${result.y}) with score ${result.score}" }

        // Draw a green dot at the match location
        val g2d = haystack.createGraphics()
        g2d.color = Color.GREEN
        g2d.fillOval(result.x - radius, result.y - radius, radius * 2, radius * 2)
        g2d.dispose()

        // Output the haystack image with the dot to the build folder
        val outputFile = File("build/find_${haystackPath}_${needlePath}")
        ImageIO.write(haystack, "png", outputFile)
        logger.info { "Output image written to: ${outputFile.absolutePath}" }
    }

    @ParameterizedTest
    @MethodSource("imagePairsProvider")
    fun openCvMatch(haystackPath: String, needlePath: String, radius: Int) {
        // Load OpenCV
        nu.pattern.OpenCV.loadLocally()

        // Use Java getResource to get absolute file paths for OpenCV
        val haystackMatUrl = javaClass.getResource("/$haystackPath")
        val needleMatUrl = javaClass.getResource("/$needlePath")
        requireNotNull(haystackMatUrl) { "Haystack image not found: $haystackPath" }
        requireNotNull(needleMatUrl) { "Needle image not found: $needlePath" }
        val haystackMatPath = java.io.File(haystackMatUrl.toURI()).absolutePath
        val needleMatPath = java.io.File(needleMatUrl.toURI()).absolutePath
        val sourceOrig = Imgcodecs.imread(haystackMatPath, Imgcodecs.IMREAD_COLOR)
        val template = Imgcodecs.imread(needleMatPath, Imgcodecs.IMREAD_COLOR)
        require(!sourceOrig.empty()) { "Failed to load haystack image as Mat: $haystackMatPath" }
        require(!template.empty()) { "Failed to load needle image as Mat: $needleMatPath" }

        val matchMethods = listOf(
            Imgproc.TM_SQDIFF to "TM_SQDIFF",
            Imgproc.TM_SQDIFF_NORMED to "TM_SQDIFF_NORMED",
            Imgproc.TM_CCORR to "TM_CCORR",
            Imgproc.TM_CCORR_NORMED to "TM_CCORR_NORMED",
            Imgproc.TM_CCOEFF to "TM_CCOEFF",
            Imgproc.TM_CCOEFF_NORMED to "TM_CCOEFF_NORMED"
        )

        for ((matchMethod, methodName) in matchMethods) {
            val source = sourceOrig.clone()
            val outputImage = Mat()
            Imgproc.matchTemplate(source, template, outputImage, matchMethod)
            val mmr: MinMaxLocResult = Core.minMaxLoc(outputImage)
            val matchLoc: Point = if (matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) mmr.minLoc else mmr.maxLoc

            // Draw rectangle on result image
            Imgproc.rectangle(
                source, matchLoc, Point(
                    matchLoc.x + template.cols(),
                    matchLoc.y + template.rows()
                ), Scalar(0.0, 255.0, 0.0), 3
            )
            // Print confidence (score) on the image
            val confidenceText = String.format("conf: %.3f", if (matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) mmr.minVal else mmr.maxVal)
            Imgproc.putText(
                source,
                confidenceText,
                Point(matchLoc.x, matchLoc.y - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.5,
                Scalar(0.0, 255.0, 0.0),
                2
            )
            // Print method name under the square
            Imgproc.putText(
                source,
                methodName,
                Point(matchLoc.x, matchLoc.y + template.rows() + 20),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.7,
                Scalar(0.0, 255.0, 0.0),
                2
            )
            // Save result
            Imgcodecs.imwrite("build/opencv_${haystackPath}_${methodName}_${needlePath.replace('/', '_')}", source)
            println("OpenCV $methodName match for $haystackPath/$needlePath at (${matchLoc.x}, ${matchLoc.y}) with score $confidenceText")
        }
    }
}