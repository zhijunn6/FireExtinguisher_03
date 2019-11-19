package com.example.fireextinguisher_03;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.GetExtinguisherByNumberQuery;
import com.amazonaws.amplify.generated.graphql.ListLocationsQuery;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = ScanActivity.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "com.example.fireextinguisher_03.MESSAGE";
    ImageView snapImageView;
    ImageButton snapCameraBtn;
    ImageButton snapDetectBtn;
    TextView snapTextView;
    Bitmap snapImageBitmap;

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        snapImageView = findViewById(R.id.snap_mImageView);
        snapCameraBtn = findViewById(R.id.snap_cameraButton);
        snapDetectBtn = findViewById(R.id.snap_detectButton);
        snapTextView = findViewById(R.id.snap_textView);

        snapDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectImg();
            }
        });

        openCamera();
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
            snapImageBitmap = (Bitmap) extras.get("data");
            snapImageView.setImageBitmap(snapImageBitmap);
        }
    }

    private void detectImg() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(snapImageBitmap);
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
//            Toast.makeText(ScanActivity.this, "Sorry, no text found", Toast.LENGTH_LONG).show();
//            finish();
            String txt = "EE18273H79";
            query(txt);
        }
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            String txt = block.getText();
            snapTextView.setTextSize(24);
            snapTextView.setText(txt);

//            Intent intent = new Intent(this,. class);
//            String message = snapTextView.getText().toString();
//            intent.putExtra(EXTRA_MESSAGE, message);
//            startActivity(intent);
//            finish();
            query(txt);
        }
    }

    private void query(String txt) {
        if (mAWSAppSyncClient == null) {
            mAWSAppSyncClient = ClientFactory.getInstance(this);
        }
        mAWSAppSyncClient.query(GetExtinguisherByNumberQuery.builder().extinguisherNumber(txt).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<GetExtinguisherByNumberQuery.Data> queryCallback = new GraphQLCall.Callback<GetExtinguisherByNumberQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetExtinguisherByNumberQuery.Data> response) {
            if (!response.data().getExtinguisherByNumber().items().isEmpty()) {
                Log.d(TAG, "onResponse: not empty");

                Log.d(TAG, "onResponse: " + response.data().toString());

                for(GetExtinguisherByNumberQuery.Item i: response.data().getExtinguisherByNumber().items()){
                    Log.d(TAG, "onResponse: " + i.fragments().extinguisher());
                    EditExtinguisherActivity.startActivity(ScanActivity.this, i.fragments().extinguisher());
                }
                finish();
            } else {
                Log.d(TAG, "empty");
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make FireEX api call", e);
            Log.e(TAG, e.getMessage());
        }
    };
}
