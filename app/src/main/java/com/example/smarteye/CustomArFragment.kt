package com.example.smarteye

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode


open class CustomArFragment: ArFragment() {
    var shouldAddModel: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        arSceneView.isLightEstimationEnabled = false
        initializeSession()
        return view
    }

    override fun getSessionConfiguration(session: Session): Config {
        Log.d("SetupAugImgDb", "Success")
        return super.getSessionConfiguration(session).also {
            it.lightEstimationMode = Config.LightEstimationMode.DISABLED
            it.focusMode = Config.FocusMode.AUTO

            if (!setupAugmentedImagesDb(it, session)) {
                Toast.makeText(requireContext(), "Could not setup augmented image database", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setupAugmentedImagesDb(config: Config, session: Session): Boolean {
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        val bitmap = loadAugmentedImage()
        augmentedImageDatabase.addImage("tiger", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame
        Log.i("onUpdate","frame updated")
        val augmentedImages = frame!!.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in augmentedImages) {
            if (augmentedImage.trackingState == TrackingState.TRACKING) {
                if (augmentedImage.name == "tiger" && shouldAddModel) {
                    Log.i("onUpdate","detected")

                    placeObject(
                        augmentedImage.createAnchor(augmentedImage.getCenterPose()),
                        Uri.parse("haunter.sfb")
                    )
                    shouldAddModel = false
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun placeObject(anchor: Anchor, uri: Uri) {
        ModelRenderable.builder()
            .setSource(context!!, uri)
            .build()
            .thenAccept { modelRenderable -> addNodeToScene(anchor, modelRenderable) }
            .exceptionally { throwable ->
                Toast.makeText(context, "Error:" + throwable.message, Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun addNodeToScene(anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arSceneView.scene.addChild(anchorNode)
        node.select()
    }
    private fun loadAugmentedImage(): Bitmap {
        val inputStream = requireContext().assets.open("sample.jpg")
        return BitmapFactory.decodeStream(inputStream)
    }
}