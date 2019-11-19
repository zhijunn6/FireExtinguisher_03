package com.example.fireextinguisher_03;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateLocationMutation;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateLocationInput;

public class AddLocationActivity extends AppCompatActivity {

    private static final String TAG = AddLocationActivity.class.getSimpleName();
    private EditText locationName, locationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        locationName = (EditText) findViewById(R.id.editTxt_name);
        locationAddress = (EditText) findViewById(R.id.editText_address);

        Button btnAddItem = findViewById(R.id.btn_save);
        btnAddItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void save() {
        final String name = locationName.getText().toString();
        final String address = locationAddress.getText().toString();
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());

        if(locationName.getText().toString().equals("")){
            locationName.setError("Location Name is required!");
            return;
        }

        CreateLocationInput input = CreateLocationInput.builder()
                .name(name)
                .address(address)
                .build();

        CreateLocationMutation addLocationMutation = CreateLocationMutation.builder()
                .input(input)
                .build();
        awsAppSyncClient.mutate(addLocationMutation).enqueue(mutateCallback);
    }

    // Mutation callback code
    private GraphQLCall.Callback<CreateLocationMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateLocationMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateLocationMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddLocationActivity.this, "Added location", Toast.LENGTH_SHORT).show();
                    AddLocationActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddLocationMutation", e);
                    Toast.makeText(AddLocationActivity.this, "Failed to add location", Toast.LENGTH_SHORT).show();
                    AddLocationActivity.this.finish();
                }
            });
        }
    };
}
