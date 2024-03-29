package com.example.fireextinguisher_03;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import fragment.Location;

public class SnapActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.fireextinguisher_03.MESSAGE";
    private static Location location;
    ImageView mImageView;
    CropImageView cropImageView;
    ImageButton cameraBtn;
    ImageButton detectBtn;
    ImageButton cropBtn;
    TextView textView;
    Bitmap imageBitmap, cropped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);

        cropImageView = findViewById(R.id.cropImageView);
        //mImageView = findViewById(R.id.mImageView);
        cameraBtn = findViewById(R.id.cameraButton);
        detectBtn = findViewById(R.id.detectButton);
        textView = findViewById(R.id.textView);
        cropBtn = findViewById(R.id.cropButton);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectImg();
            }
        });
        cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.setImageBitmap(cropped);
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            cropImageView.setImageBitmap(imageBitmap);

            cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                @Override
                public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                }
            });

            cropped = cropImageView.getCroppedImage();
        }
    }



    private void detectImg() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(cropped);
        FirebaseVisionTextRecognizer textRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(SnapActivity.this, "Sorry, no text found", Toast.LENGTH_LONG).show();
            String txt = "Please key in the extinguisher number :(";
            Intent intent = new Intent(this, ViewLocationActivity.class);
            intent.putExtra(EXTRA_MESSAGE, txt);
            startActivity(intent);
            finish();
//            return;
        }
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            String txt = block.getText();
            textView.setTextSize(24);
            textView.setText(txt);

            Intent intent = new Intent(this, ViewLocationActivity.class);
            String message = textView.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
            finish();
        }
    }
}
