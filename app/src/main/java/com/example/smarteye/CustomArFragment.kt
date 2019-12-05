package com.example.smarteye

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.ar.core.Pose
import com.google.ar.core.Anchor




open class CustomArFragment: ArFragment() {
    var shouldAddModel: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = true
        arSceneView.isLightEstimationEnabled = false
        setOnTapArPlaneListener(this::handleTap)
        initializeSession()
//        val session = getArSceneView().getSession()
//        val pos = floatArrayOf(0f, 0f, -1f)
//        val rotation = floatArrayOf(0f, 0f, 0f, 1f)
//        val anchor = session!!.createAnchor(Pose(pos, rotation))
//        //val anchorNode = AnchorNode(anchor)
//        placeObject(
//            anchor,
//            Uri.parse("box.sfb")
//                    )
        return view
    }
    private fun handleTap(hitResult: HitResult, plane: Plane , motionEvent: MotionEvent ) {
        val anchor = hitResult.createAnchor()
        Log.i("tap","tap")
        placeObject(
            anchor,
            Uri.parse("box.sfb")
        )
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
//        val augmentedImageDatabase = AugmentedImageDatabase(session)
//        val bitmap = loadAugmentedImage()
//        augmentedImageDatabase.addImage("tiger", bitmap)
//        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame
        Log.i("onUpdate","frame updated")
        val planes = frame!!.getUpdatedTrackables(Plane::class.java)
//        for (plane in planes) {
//            if (plane.trackingState == TrackingState.TRACKING) {
//                if (shouldAddModel) {
//                    //Get all added anchors to the frame
//                    val iterableAnchor = frame.updatedAnchors.iterator()
//
//                    //place the first object only if no previous anchors were added
//                    if(!iterableAnchor.hasNext()) {
//                        //Perform a hit test at the center of the screen to place an object without tapping
//                        val hitTest = frame.hitTest(0F, 0F)
//
//                        //iterate through all hits
//                        val hitTestIterator = hitTest.iterator()
//                        while(hitTestIterator.hasNext()) {
//                            val hitResult = hitTestIterator.next()
//                            Log.i("onUpdate","datected")
//                            //Create an anchor at the plane hit
//                            val modelAnchor = plane.createAnchor(hitResult.hitPose)
//                            placeObject(
//                                modelAnchor,
//                                Uri.parse("box.sfb")
//                            )
//                        }
//                    }
//
//                    shouldAddModel = false
//                }
//            }
//        }
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