package com.example.myapplication;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class MainActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Erstelle ein neues ArFragment und füge es der Activity hinzu
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        // Lade das 3D-Modell aus der res/raw-Datei
        ModelRenderable.builder()
                .setSource(this, R.raw.my_3d_model)
                .build()
                .thenAccept(renderable -> {
                    modelRenderable = renderable;

                    // Erstelle ein neues TransformableNode für das 3D-Modell
                    TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                    modelNode.setParent(arFragment.getArSceneView().getScene());
                    modelNode.setRenderable(modelRenderable);

                    // Mache das 3D-Modell transformierbar und aktiviere die Rotation
                    modelNode.getRotationController().setEnabled(true);
                    modelNode.getTranslationController().setEnabled(false);
                    modelNode.getScaleController().setEnabled(false);
                    modelNode.select();

                    // Passe die Rotation des 3D-Modells anhand der x, y und z Werte an
                    float xRotation = ... // hier kannst du deine x-Werte einfügen
                    float yRotation = ... // hier kannst du deine y-Werte einfügen
                    float zRotation = ... // hier kannst du deine z-Werte einfügen
                    Quaternion quaternion = Quaternion.eulerAngles(new Vector3(xRotation, yRotation, zRotation));
                    modelNode.setLocalRotation(quaternion);
                })
                .exceptionally(throwable -> {
                    // Behandlung von Ausnahmen beim Laden des Modells
                    return null;
                });
    }
}
