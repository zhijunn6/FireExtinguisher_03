package com.example.fireextinguisher_03;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.DeleteExtinguisherMutation;
import com.amazonaws.amplify.generated.graphql.ExtinguisherOnLocationMutation;
import com.amazonaws.amplify.generated.graphql.UpdateExtinguisherMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import fragment.Extinguisher;
import fragment.Location;

public class EditExtinguisherActivity extends AppCompatActivity {
    public static final String TAG = EditExtinguisherActivity.class.getSimpleName();

    private static Extinguisher extinguisher;
    private EditText edit_extinguisherNo, edit_subLocation, edit_manufDate, edit_expDate;

    public static void startActivity(Context context, Extinguisher e) {
        extinguisher = e;
        Intent intent = new Intent(context, EditExtinguisherActivity.class);
        context.startActivity(intent);
        //change is here owo
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_extinguisher);

        Log.d(TAG, "onCreate: " + extinguisher);

        edit_extinguisherNo = (EditText)findViewById(R.id.edit_extinguisher_extinguisherNumber);
        edit_subLocation = (EditText)findViewById(R.id.edit_extinguisher_subLocation);
        edit_manufDate = (EditText)findViewById(R.id.edit_extinguisher_manufacturingDate);
        edit_expDate = (EditText)findViewById(R.id.edit_extinguisher_expiryDate);

        edit_extinguisherNo.setText(extinguisher.extinguisherNumber());
        edit_subLocation.setText(extinguisher.subLocation());
        edit_manufDate.setText(extinguisher.manufacturingDate());
        edit_expDate.setText(extinguisher.expiryDate());
    }

    public void editExtinguisher(View v){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit_extinguisherNo.getWindowToken(), 0);

        UpdateExtinguisherMutation extinguisherMutation = UpdateExtinguisherMutation.builder()
                .locationId(extinguisher.locationId())
                .extinguisherId(extinguisher.extinguisherId())
                .extinguisherNumber(edit_extinguisherNo.getText().toString())
                .subLocation(edit_subLocation.getText().toString())
                .manufacturingDate(edit_manufDate.getText().toString())
                .expiryDate(edit_expDate.getText().toString())
                .build();

        ClientFactory.getInstance(v.getContext())
                .mutate(extinguisherMutation)
                .enqueue(editExtinguisherCallback);
    }

    private GraphQLCall.Callback<UpdateExtinguisherMutation.Data> editExtinguisherCallback = new GraphQLCall.Callback<UpdateExtinguisherMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateExtinguisherMutation.Data> response) {
            Log.d(TAG, response.toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make extinguisher mutation", e);
            Log.e(TAG, e.getMessage());
        }
    };

    public void deleteExtinguisher(View v){
        DeleteExtinguisherMutation deleteExtinguisherMutation = DeleteExtinguisherMutation.builder()
                                .locationId(extinguisher.locationId())
                                .extinguisherId(extinguisher.extinguisherId())
                                .build();

                        ClientFactory.getInstance(getApplicationContext())
                                .mutate(deleteExtinguisherMutation)
                                .enqueue(deleteExtinguisherCallback);
    }

    private GraphQLCall.Callback<DeleteExtinguisherMutation.Data> deleteExtinguisherCallback = new GraphQLCall.Callback<DeleteExtinguisherMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<DeleteExtinguisherMutation.Data> response) {
            Log.d(TAG, response.toString());
            finish();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make extinguisher mutation", e);
            Log.e(TAG, e.getMessage());
        }
    };

}
