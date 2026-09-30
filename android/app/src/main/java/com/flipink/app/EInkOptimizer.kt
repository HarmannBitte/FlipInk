package com.flipink.app

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

/**
 * High-performance image processor for converting Android screen bitmaps
 * into 1-bit monochome buffers optimized for fast E-Ink refresh.
 */
object EInkOptimizer {

    /**
     * Converts a full-color 32-bit ARGB Android Bitmap to a 1-bit packed bitmap
     * using Floyd-Steinberg error diffusion dithering.
     *
     * In 1-bit packed representation: 1 = White, 0 = Black.
     * 8 pixels are packed into 1 byte.
     */
    fun convertTo1BitFloydSteinberg(source: Bitmap, targetWidth: Int, targetHeight: Int): ByteArray {
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        val width = scaled.width
        val height = scaled.height

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale 2D array of floats for error diffusion
        val gray = Array(height) { FloatArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = pixels[y * width + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                // Standard luminance formula
                gray[y][x] = (0.299f * r + 0.587f * g + 0.114f * b)
            }
        }

        val outBufferSize = (width * height) / 8
        val outBytes = ByteArray(outBufferSize)
        var byteIndex = 0
        var bitOffset = 7
        var currentByte = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val oldVal = gray[y][x]
                val newVal = if (oldVal > 128f) 255f else 0f
                val error = oldVal - newVal

                if (newVal == 255f) {
                    currentByte = currentByte or (1 shl bitOffset)
                }

                bitOffset--
                if (bitOffset < 0) {
                    outBytes[byteIndex++] = currentByte.toByte()
                    currentByte = 0
                    bitOffset = 7
                }

                // Distribute quantization error:
                //         X     7/16
                //  3/16  5/16   1/16
                if (x + 1 < width) {
                    gray[y][x + 1] += error * (7f / 16f)
                }
                if (y + 1 < height) {
                    if (x > 0) {
                        gray[y + 1][x - 1] += error * (3f / 16f)
                    }
                    gray[y + 1][x] += error * (5f / 16f)
                    if (x + 1 < width) {
                        gray[y + 1][x + 1] += error * (1f / 16f)
                    }
                }
            }
        }

        return outBytes
    }

    /**
     * Contrast-boosted direct thresholding (ideal for pure text / e-readers, Moon+, Kindle)
     * Provides sharper font edges without dithering grain.
     */
    fun convertTextModeThreshold(source: Bitmap, targetWidth: Int, targetHeight: Int, threshold: Int = 160): ByteArray {
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        val width = scaled.width
        val height = scaled.height

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val outBytes = ByteArray((width * height) / 8)
        var byteIndex = 0
        var bitOffset = 7
        var currentByte = 0

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            if (lum >= threshold) {
                currentByte = currentByte or (1 shl bitOffset)
            }

            bitOffset--
            if (bitOffset < 0) {
                outBytes[byteIndex++] = currentByte.toByte()
                currentByte = 0
                bitOffset = 7
            }
        }

        return outBytes
    }
}
