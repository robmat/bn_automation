package com.batodev.bn_automation.imagematch

import java.awt.image.BufferedImage
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ImageMatcher {
    private val logger: Logger = LogManager.getLogger(ImageMatcher::class.java)

    @JvmRecord
    data class MatchResult(val x: Int, val y: Int, val score: Double)

    fun find(needleImage: BufferedImage, haystackImage: BufferedImage) : MatchResult {
        val step = 3
        val needleSample: Array<IntArray>? = sample(needleImage, step)
        val haystackSample: Array<IntArray>? = sample(haystackImage, step)

        val startTime = System.currentTimeMillis()
        val matches = match(needleSample, haystackSample, step)
        val endTime = System.currentTimeMillis()
        logger.info("match() took {} ms", (endTime - startTime))
        return matches[0]
    }

    fun match(
        needleSample: Array<IntArray>?,
        haystackSample: Array<IntArray>?,
        step: Int,
    ): MutableList<MatchResult> {
        var step = step
        if (needleSample == null || haystackSample == null) return mutableListOf()
        step = max(1, step)

        val nRows = needleSample.size
        val nCols = if (nRows == 0) 0 else needleSample[0].size
        val hRows = haystackSample.size
        val hCols = if (hRows == 0) 0 else haystackSample[0].size

        if (nRows == 0 || nCols == 0) return mutableListOf()
        if (hRows < nRows || hCols < nCols) return mutableListOf()

        val maxStartY = hRows - nRows
        val maxStartX = hCols - nCols

        val numThreads = Runtime.getRuntime().availableProcessors()
        val results: MutableList<MatchResult> =
            Collections.synchronizedList(ArrayList())

        val sampleCount = nRows.toLong() * nCols
        val maxPerSample = 3.0 * 255.0 * 255.0
        val needleWidth = nCols * step
        val needleHeight = nRows * step

        val executor: ExecutorService = Executors.newFixedThreadPool(numThreads)
        try {
            for (sy in 0..maxStartY) {
                executor.execute {
                    for (sx in 0..maxStartX) {
                        var raw = 0L
                        for (r in 0..<nRows) {
                            val hayRow = haystackSample[sy + r]
                            val needleRow = needleSample[r]
                            for (c in 0..<nCols) {
                                val hc = hayRow[sx + c]
                                val nc = needleRow[c]

                                val hr = (hc shr 16) and 0xFF
                                val hg = (hc shr 8) and 0xFF
                                val hb = hc and 0xFF

                                val nr = (nc shr 16) and 0xFF
                                val ng = (nc shr 8) and 0xFF
                                val nb = nc and 0xFF

                                val dr = hr - nr
                                val dg = hg - ng
                                val db = hb - nb

                                raw += dr.toLong() * dr + dg.toLong() * dg + db.toLong() * db
                            }
                        }

                        val normalized = raw.toDouble() / (maxPerSample * sampleCount)
                        val centerX = sx * step + needleWidth / 2
                        val centerY = sy * step + needleHeight / 2
                        results.add(MatchResult(centerX, centerY, normalized))
                    }
                }
            }
        } finally {
            try {
                executor.shutdown()
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    System.err.println("${ImageMatcher::class.simpleName}: Executor did not terminate in time.")
                    executor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                System.err.println("${ImageMatcher::class.simpleName}: Matching was interrupted. ${e.message}")
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }

        // The synchronized list is thread-safe for adds, but sorting should be done after all threads are finished.
        // A final sort on the now-complete list is safe.
        results.sortWith(
            Comparator.comparingDouble(MatchResult::score)
        )

        return results
    }

    fun sample(img: BufferedImage?, step: Int): Array<IntArray>? {
        var step = step
        step = max(1, step)

        if (img == null) return null

        val w = img.width
        val h = img.height
        val cols = (w + step - 1) / step
        val rows = (h + step - 1) / step

        val samples = Array(rows) { IntArray(cols) }
        for (r in 0..<rows) {
            var y = r * step
            if (y >= h) y = h - 1
            for (c in 0..<cols) {
                var x = c * step
                if (x >= w) x = w - 1
                samples[r][c] = img.getRGB(x, y)
            }
        }
        return samples
    }
}
