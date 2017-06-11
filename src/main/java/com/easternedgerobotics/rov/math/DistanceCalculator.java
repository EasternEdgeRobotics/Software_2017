package com.easternedgerobotics.rov.math;

import com.easternedgerobotics.rov.config.DistanceCalculatorConfig;
import com.easternedgerobotics.rov.io.ValueStore;
import com.easternedgerobotics.rov.value.AxisValue;
import com.easternedgerobotics.rov.value.CameraCalibrationValue;
import com.easternedgerobotics.rov.value.PointValue;

import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DistanceCalculator {
    private static final double X_START = 0.1;

    private static final double X_END = 1.9;

    private static final double X_DELTA = 0.001;

    private final DistanceCalculatorConfig config;

    private final ValueStore<CameraCalibrationValue> store;

    /**
     * Create a distance calculator which is able to determine the ground plane of an image
     * and map image points onto this ground plane.
     *
     * @param config
     * @param store
     */
    public DistanceCalculator(
        final DistanceCalculatorConfig config,
        final ValueStore<CameraCalibrationValue> store
    ) {
        this.config = config;
        this.store = store;
    }

    @SuppressWarnings({"checkstyle:LocalFinalVariableName", "checkstyle:MethodLength"})
    public Optional<List<PointValue>> calculate(
        final AxisValue axis,
        final List<PointValue> points,
        final String cameraName
    ) {
        return store.get(cameraName).map(calibration -> {
            /**
             * Construct the initial axis image to object map.
             * Used by solve PnP to figure out projections from object plane to image plane.
             */
            final double w = calibration.getWidth();
            final double h = calibration.getHeight();
            final double[][] axisPixels = {
                {(w / 2) + w * axis.getOrigin().getX(), (h / 2) +  h * axis.getOrigin().getY()},
                {(w / 2) + w * axis.getX().getX(), (h / 2) +  h * axis.getX().getY()},
                {(w / 2) + w * axis.getY().getX(), (h / 2) + h * axis.getY().getY()},
            };
            final Mat axisPixelsMat = new Mat(axisPixels.length, 1, opencv_core.CV_64FC2);
            final DoubleIndexer axisPixelsIndexer = axisPixelsMat.createIndexer();
            for (int j = 0; j < axisPixels.length; j++) {
                axisPixelsIndexer.put(j, 0, 0, axisPixels[j][0]);
                axisPixelsIndexer.put(j, 0, 1, axisPixels[j][1]);
            }
            final double[][] axisObject = {
                {0, 0},
                {1, 0},
                {0, 1},
            };
            final Mat axisObjectMat = new Mat(1, axisPixels.length, opencv_core.CV_64FC3);
            final DoubleIndexer axisObjectIndexer = axisObjectMat.createIndexer();
            for (int j = 0; j < axisPixels.length; j++) {
                axisObjectIndexer.put(0, j, 0, axisObject[j][0]);
                axisObjectIndexer.put(0, j, 1, axisObject[j][1]);
                axisObjectIndexer.put(0, j, 2, 0);
            }

            /**
             * Construct a point on the z axis.
             * This point when projected as a ray in the object coordinate system
             * should intersect with the z-axis found by the PnP system.
             * If this point does not intersect with the z-axis then we know that
             * the scale of the object axis was not estimated correctly.
             */
            final Mat cameraMatrix = calibration.getCameraMatrix();
            final Mat zAxisPoint = new Mat(3, 1, cameraMatrix.type());
            final DoubleIndexer zAxisPointIndexer = zAxisPoint.createIndexer();
            zAxisPointIndexer.put(0, 0, (w / 2) + w * axis.getZ().getX());
            zAxisPointIndexer.put(1, 0, (h / 2) + h * axis.getZ().getY());
            zAxisPointIndexer.put(2, 0, 1);

            /**
             * Begin with the assumption that the x axis is in the range of 1-2 times the size of
             * the y axis. Edit the value in the object coordinate map to this estimate and discover
             * a transformation for the object to image coordinates. This equation follows the format
             *            s * Pc = M * (R * Pw + T)
             *            s = scalar
             *            Pc = imagePoint
             *            Pw = objectPoint
             *            M = intrinsicMatrix
             *            R = rotationMatrix
             *            T = translationVector
             *
             * Reverse standard mapping to figure out a mapping of image to object coordinates.
             *           Pw = s * inv(R) * inv(M) * Pc - inv(R) * T
             *           Pw = s * a - b
             *
             * When a pixel is projected into object space it creates a ray in the scene.
             * This leaves a linear equation of:
             *            [[Pw_0],         [[a_0],    [[b_0],
             *             [Pw_1],  =  s *  [a_1],  -  [b_1],
             *             [Pw_2]]          [a_2]]     [b_2]]
             *
             * We know that this ray must intersect at some point with the z-axis ray in object space.
             * This means that we can constrain the system to the following:
             *            [[0],          [[a_0],    [[b_0],
             *             [0],   =  s *  [a_1],  -  [b_1],
             *           [Pw_2]]          [a_2]]     [b_2]]
             *
             * This leaves the system with two solutions for s:
             *              0 = s * a_0 - b_0
             *              0 = s * a_1 - b_1
             *
             * For this equation to be solvable it means that:
             *              s = b_0 / a_0
             *              s = b_1 / a_1
             *              b_0 / a_0 = b_1 / a_1
             *
             * The value of x which is the closest to being able to satisfy this equation will be considered the
             * correct value of the object space coordinate moving forward.
             *
             * Start by initializing the best matches such that the first iteration overwrites them.
             */
            double bestDiffS = Double.MAX_VALUE;
            double bestX = 0;

            for (double x = X_START; x < X_END; x += X_DELTA) {
                axisObjectIndexer.put(0, 2, 1, x);

                final Mat rotationalVector = new Mat();
                final Mat translationalVector = new Mat();
                opencv_calib3d.solvePnP(
                    axisObjectMat,
                    axisPixelsMat,
                    cameraMatrix,
                    new Mat(),
                    rotationalVector,
                    translationalVector);

                final Mat R = new Mat();
                opencv_calib3d.Rodrigues(rotationalVector, R);
                final Mat R_inv = R.inv().asMat();
                final Mat T = translationalVector;
                final Mat M = cameraMatrix;
                final Mat M_inv = M.inv().asMat();

                final Mat M_inv_Pc = new Mat();
                opencv_core.gemm(M_inv, zAxisPoint, 1, new Mat(), 0, M_inv_Pc);

                final Mat R_inv_M_inv_Pc = new Mat();
                opencv_core.gemm(R_inv, M_inv_Pc, 1, new Mat(), 0, R_inv_M_inv_Pc);

                final Mat R_inv_T = new Mat();
                opencv_core.gemm(R_inv, T, 1, new Mat(), 0, R_inv_T);

                final DoubleIndexer lhsIndexer = R_inv_M_inv_Pc.createIndexer();
                final DoubleIndexer rhsIndexer = R_inv_T.createIndexer();

                final double s0 = rhsIndexer.get(0, 0) / lhsIndexer.get(0, 0);
                final double s1 = rhsIndexer.get(1, 0) / lhsIndexer.get(1, 0);

                final double s = Math.abs(s0 - s1);
                if (s < bestDiffS) {
                    bestX = x;
                    bestDiffS = s;
                }
            }

            /**
             * Calculate the image to object coordinate mapping using the best s from the previous algorithm.
             */
            axisObjectIndexer.put(0, 2, 1, bestX);

            final Mat rotationalVector = new Mat();
            final Mat translationalVector = new Mat();
            opencv_calib3d.solvePnP(
                axisObjectMat,
                axisPixelsMat,
                cameraMatrix,
                new Mat(),
                rotationalVector,
                translationalVector);

            final Mat R = new Mat();
            opencv_calib3d.Rodrigues(rotationalVector, R);
            final Mat R_inv = R.inv().asMat();
            final Mat T = translationalVector;
            final Mat M = cameraMatrix;
            final Mat M_inv = M.inv().asMat();

            /**
             * Calculate the image to object coordinate mapping by assuming that
             * the point is on the ground plane. This constrains the ray equation to:
             *          [[Pw_0],          [[a_0],    [[b_0],
             *           [Pw_1],   =  s *  [a_1],  -  [b_1],
             *            [0]]             [a_2]]     [b_2]]
             *
             * This leaves us with a single solution for s:
             *             0 = s * a_2 - b_2
             *             s = b_2 / a_2
             *
             * Plug this value back into the first equation to solve for x and y
             *             x = Pw_0 = (b_2 / a_2) * a_0 - b_0
             *             y = Pw_1 = (b_2 / a_2) * a_1 - b_1
             *
             */
            return points.stream().map(point -> {
                final Mat imagePoint = new Mat(3, 1, cameraMatrix.type());
                final DoubleIndexer imagePointIndexer = imagePoint.createIndexer();
                imagePointIndexer.put(0, 0, (w / 2) + w * point.getX());
                imagePointIndexer.put(1, 0, (h / 2) + h * point.getY());
                imagePointIndexer.put(2, 0, 1);

                final Mat M_inv_Pc = new Mat();
                opencv_core.gemm(M_inv, imagePoint, 1, new Mat(), 0, M_inv_Pc);

                final Mat R_inv_M_inv_Pc = new Mat();
                opencv_core.gemm(R_inv, M_inv_Pc, 1, new Mat(), 0, R_inv_M_inv_Pc);

                final Mat R_inv_T = new Mat();
                opencv_core.gemm(R_inv, T, 1, new Mat(), 0, R_inv_T);

                final DoubleIndexer lhsIndexer = R_inv_M_inv_Pc.createIndexer();
                final DoubleIndexer rhsIndexer = R_inv_T.createIndexer();
                final double s = rhsIndexer.get(2, 0) / lhsIndexer.get(2, 0);

                final Mat Rinv_Cinv_s_P = opencv_core.multiply(s, R_inv_M_inv_Pc).asMat();
                final Mat objectPoint = opencv_core.subtract(Rinv_Cinv_s_P, R_inv_T).asMat();

                final DoubleIndexer objectPointIndexer = objectPoint.createIndexer();

                return new PointValue(objectPointIndexer.get(0, 0), objectPointIndexer.get(1, 0));
            }).collect(Collectors.toList());
        });
    }
}
