package com.easternedgerobotics.rov.value

import org.bytedeco.javacpp.indexer.DoubleIndexer
import org.bytedeco.javacpp.opencv_core

class CameraCalibrationValue(
    val validFileNames: List<String>,
    val rmsError: Double,
    val width: Int,
    val height: Int,
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
    val k1: Double,
    val k2: Double,
    val p1: Double,
    val p2: Double,
    val k3: Double
) {
    /**
     * @return The size of the calibrated image.
     */
    fun getImageSize(): opencv_core.Size {
        return opencv_core.Size(width, height)
    }

    /**
     * @return intrinsic camera matrix.
     */
    fun getCameraMatrix(): opencv_core.Mat {
        val matrix = opencv_core.Mat(3, 3, opencv_core.CV_64F)
        val indexer = matrix.createIndexer<DoubleIndexer>()
        indexer.put(0, 0, fx)
        indexer.put(1, 1, fy)
        indexer.put(0, 2, cx)
        indexer.put(1, 2, cy)
        indexer.put(2, 2, 1.0)
        return matrix
    }

    /**
     * @return distortion coefficients.
     */
    fun getDistortionCoeffs(): opencv_core.Mat {
        val matrix = opencv_core.Mat(1, 5, opencv_core.CV_64F)
        val indexer = matrix.createIndexer<DoubleIndexer>()
        indexer.put(0, 0, k1)
        indexer.put(0, 1, k2)
        indexer.put(0, 2, p1)
        indexer.put(0, 3, p2)
        indexer.put(0, 4, k3)
        return matrix
    }
}
