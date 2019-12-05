package com.example.smarteye

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
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.core.Anchor




open class CustomArFragment: ArFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = true
        arSceneView.isLightEstimationEnabled = false
        setOnTapArPlaneListener(this::handleTap)
        initializeSession()
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
}