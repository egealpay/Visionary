package com.example.ozturkse.x

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import io.fotoapparat.Fotoapparat
import io.fotoapparat.FotoapparatSwitcher
import io.fotoapparat.facedetector.processor.FaceDetectorProcessor
import io.fotoapparat.parameter.selector.LensPositionSelectors.back
import io.fotoapparat.parameter.selector.LensPositionSelectors.front
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private lateinit var fotoapparatBack: Fotoapparat

    private lateinit var fotoapparatFront: Fotoapparat

    private lateinit var fotoapparatSwitcher: FotoapparatSwitcher

    private lateinit var processor: FaceDetectorProcessor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        createProcessor()
        createFotoApparat()

        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(fotoapparatBack)  //For switching between back & front camera
    }

    private fun createProcessor() {
        processor = FaceDetectorProcessor.with(this)
                .listener { faces ->
                    rectanglesView.setRectangles(faces)// (Optional) Show detected faces on the view.
                }
                .build()

    }


    private fun createFotoApparat() {
        fotoapparatBack = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(back())
                .build()

        fotoapparatFront = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(front())
                .build()
    }

    @OnClick(R.id.activity_main_button_switchcamera)
    fun switchCamera() {
        if (fotoapparatSwitcher.currentFotoapparat == fotoapparatFront) {
            fotoapparatSwitcher.switchTo(fotoapparatBack)
        } else {
            fotoapparatSwitcher.switchTo(fotoapparatFront)
        }
    }

    override fun onResume() {
        super.onResume()
        fotoapparatSwitcher.start()
    }

    override fun onPause() {
        fotoapparatSwitcher.stop()
        super.onPause()
    }
}