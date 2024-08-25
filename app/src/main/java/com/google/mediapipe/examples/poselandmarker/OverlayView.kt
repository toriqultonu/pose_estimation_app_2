
package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.absoluteValue

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1



    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 36f
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun calculateAngle(firstPoint: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
                               midPoint: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
                               lastPoint: com.google.mediapipe.tasks.components.containers.NormalizedLandmark): Double {
        val angle = Math.toDegrees(
            (atan2(lastPoint.y() - midPoint.y(), lastPoint.x() - midPoint.x()) -
                    atan2(firstPoint.y() - midPoint.y(), firstPoint.x() - midPoint.x())).toDouble()
        ).absoluteValue

        return if (angle > 180) 360 - angle else angle
    }

    object PoseLandmark {

        const val NOSE = 0
        const val LEFT_EYE_INNER = 1
        const val LEFT_EYE = 2
        const val LEFT_EYE_OUTER = 3
        const val RIGHT_EYE_INNER = 4
        const val RIGHT_EYE = 5
        const val RIGHT_EYE_OUTER = 6
        const val LEFT_EAR = 7
        const val RIGHT_EAR = 8
        const val MOUTH_LEFT = 9
        const val MOUTH_RIGHT = 10
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_PINKY = 17
        const val RIGHT_PINKY = 18
        const val LEFT_INDEX = 19
        const val RIGHT_INDEX = 20
        const val LEFT_THUMB = 21
        const val RIGHT_THUMB = 22
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
        const val LEFT_HEEL = 29
        const val RIGHT_HEEL = 30
        const val LEFT_FOOT_INDEX = 31
        const val RIGHT_FOOT_INDEX = 32
    }




    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for(landmark in poseLandmarkerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                }


                val leftElbow = landmark[PoseLandmark.LEFT_ELBOW]
                val leftShoulder = landmark[PoseLandmark.LEFT_SHOULDER]
                val leftWrist = landmark[PoseLandmark.LEFT_WRIST]

                val rightElbow = landmark[PoseLandmark.RIGHT_ELBOW]
                val rightShoulder = landmark[PoseLandmark.RIGHT_SHOULDER]
                val rightWrist = landmark[PoseLandmark.RIGHT_WRIST]



                // Calculate and draw angles
//                val leftElbow = landmark[PoseLandmarker.LEFT_ELBOW]
//                val leftShoulder = landmark[PoseLandmarker.LEFT_SHOULDER]
//                val leftWrist = landmark[PoseLandmarker.LEFT_WRIST]
//
//                val rightElbow = landmark[PoseLandmarker.RIGHT_ELBOW]
//                val rightShoulder = landmark[PoseLandmarker.RIGHT_SHOULDER]
//                val rightWrist = landmark[PoseLandmarker.RIGHT_WRIST]

                val leftElbowAngle = calculateAngle(landmark[PoseLandmark.LEFT_SHOULDER], landmark[PoseLandmark.LEFT_ELBOW], landmark[PoseLandmark.LEFT_WRIST])
                val rightElbowAngle = calculateAngle(landmark[PoseLandmark.RIGHT_SHOULDER], landmark[PoseLandmark.RIGHT_ELBOW], landmark[PoseLandmark.RIGHT_WRIST])



                // Draw left elbow angle
                canvas.drawText(
                    String.format("%.0f°", leftElbowAngle),
                    leftElbow.x() * imageWidth * scaleFactor,
                    leftElbow.y() * imageHeight * scaleFactor,
                    textPaint
                )

                // Draw right elbow angle
                canvas.drawText(
                    String.format("%.0f°", rightElbowAngle),
                    rightElbow.x() * imageWidth * scaleFactor,
                    rightElbow.y() * imageHeight * scaleFactor,
                    textPaint
                )
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}