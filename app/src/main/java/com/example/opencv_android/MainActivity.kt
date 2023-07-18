package com.example.opencv_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.core.view.isVisible
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.Collections

class MainActivity : CameraActivity() {

     private lateinit var mOpenCvCameraView : CameraBridgeViewBase

    private val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    mOpenCvCameraView.enableView()
                    // OpenCV initialization is successful
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById(R.id.opencv_surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener)
        mOpenCvCameraView.setCameraOrientation(90)//fix camera orientation problem
//        if (OpenCVLoader.initDebug()) {
//            Log.d("OpenCV", "OpenCV initialized.")
//            // check opencv succeeded
//        }

    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(mOpenCvCameraView)
    }

    private var cvCameraViewListener : CvCameraViewListener2 = object :
        CvCameraViewListener2 {
        private var prevFrame: Mat? = null
        override fun onCameraViewStarted(width: Int, height: Int) {
            prevFrame = Mat(Size(width.toDouble(), height.toDouble()), CvType.CV_8UC4)
        }

        override fun onCameraViewStopped() {
            prevFrame?.release()
            prevFrame = null
        }

        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
            val frame = inputFrame.rgba()
            val corners = MatOfPoint()

            // Convert the frame to grayscale for corner detection
            val gray = Mat()
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY)

            // Perform corner detection using Imgproc.goodFeaturesToTrack
            Imgproc.goodFeaturesToTrack(
                gray, corners, 100, 0.01, 20.0
            )

            // Draw green dots on the corners
            val points = corners.toArray()
            points.forEach { point ->
                val center = Point(point.x, point.y)
                Imgproc.circle(frame, center, 5, Scalar(0.0, 255.0, 0.0), -1)
            }

            // Release the grayscale frame and corners
            gray.release()
            corners.release()
            Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)

            return frame
        }
    }

    override fun onPause() {
        super.onPause()
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView()
        }
    }

    override fun onResume() {
        super.onResume()
        if(!OpenCVLoader.initDebug()){
            Log.d("OpenCV", "OpenCV not found, initializing...")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback)
        }
        else{
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView()
        }
    }

}