package com.batodev.bn_automation.imagematch

import com.batodev.bn_automation.automations.AutomationException
import java.awt.image.BufferedImage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object ImageMatcher {
    private val logger: Logger = LogManager.getLogger(ImageMatcher::class.java)

    @JvmRecord
    data class MatchResult(val x: Int, val y: Int, val score: Double)

    fun find(needleImage: BufferedImage, haystackImage: BufferedImage): MatchResult {
        // Save images to temp files for OpenCV
        val tempDir = System.getProperty("java.io.tmpdir")
        val haystackFile = java.io.File.createTempFile("haystack", ".png", java.io.File(tempDir))
        val needleFile = java.io.File.createTempFile("needle", ".png", java.io.File(tempDir))
        javax.imageio.ImageIO.write(haystackImage, "png", haystackFile)
        javax.imageio.ImageIO.write(needleImage, "png", needleFile)

        // Load OpenCV
        nu.pattern.OpenCV.loadLocally()
        val haystackMat = org.opencv.imgcodecs.Imgcodecs.imread(haystackFile.absolutePath, org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR)
        val needleMat = org.opencv.imgcodecs.Imgcodecs.imread(needleFile.absolutePath, org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR)
        require(!haystackMat.empty()) { "Failed to load haystack image as Mat" }
        require(!needleMat.empty()) { "Failed to load needle image as Mat" }

        // Template matching using TM_SQDIFF_NORMED
        val resultCols = haystackMat.cols() - needleMat.cols() + 1
        val resultRows = haystackMat.rows() - needleMat.rows() + 1
        val result = org.opencv.core.Mat(resultRows, resultCols, org.opencv.core.CvType.CV_32FC1)
        org.opencv.imgproc.Imgproc.matchTemplate(haystackMat, needleMat, result, org.opencv.imgproc.Imgproc.TM_SQDIFF_NORMED)
        val mmr = org.opencv.core.Core.minMaxLoc(result)
        val matchLoc = mmr.minLoc
        val score = mmr.minVal
        val centerX = (matchLoc.x + needleMat.cols() / 2.0).toInt()
        val centerY = (matchLoc.y + needleMat.rows() / 2.0).toInt()
        logger.info("OpenCV TM_SQDIFF_NORMED match at ($centerX, $centerY) with score $score")

        val matchResult = MatchResult(centerX, centerY, score)
        writeMatchFile(matchResult, haystackFile, needleFile)
        haystackFile.delete()
        needleFile.delete()
        return matchResult
    }

    fun writeMatchFile(matchResult: MatchResult, haystackFile: File, needleFile: File) {
        // Load OpenCV
        nu.pattern.OpenCV.loadLocally()
        val haystackMat = org.opencv.imgcodecs.Imgcodecs.imread(haystackFile.absolutePath, org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR)
        val needleMat = org.opencv.imgcodecs.Imgcodecs.imread(needleFile.absolutePath, org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR)
        if (haystackMat.empty() || needleMat.empty()) return

        // Draw rectangle at match location
        val topLeft = org.opencv.core.Point((matchResult.x - needleMat.cols() / 2.0), (matchResult.y - needleMat.rows() / 2.0))
        val bottomRight = org.opencv.core.Point((matchResult.x + needleMat.cols() / 2.0), (matchResult.y + needleMat.rows() / 2.0))
        org.opencv.imgproc.Imgproc.rectangle(
            haystackMat,
            topLeft,
            bottomRight,
            org.opencv.core.Scalar(0.0, 255.0, 0.0),
            3
        )
        // Print confidence (score) on the image
        val confidenceText = String.format("conf: %.4f", matchResult.score)
        org.opencv.imgproc.Imgproc.putText(
            haystackMat,
            confidenceText,
            org.opencv.core.Point(topLeft.x, topLeft.y - 10),
            org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX,
            0.7,
            org.opencv.core.Scalar(0.0, 255.0, 0.0),
            2
        )
        // Find next available filename with 000X suffix
        val buildDir = File("build")
        if (!buildDir.exists()) buildDir.mkdirs()
        var idx = 0
        var outFile: File
        do {
            outFile = File(buildDir, "match_%04d.png".format(idx))
            idx++
        } while (outFile.exists())
        org.opencv.imgcodecs.Imgcodecs.imwrite(outFile.absolutePath, haystackMat)
        logger.info("Wrote match result to ${outFile.absolutePath}")
    }
}
