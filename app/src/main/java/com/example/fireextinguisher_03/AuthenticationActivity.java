package com.example.fireextinguisher_03;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

public class AuthenticationActivity extends AppCompatActivity {

    private final String TAG = AuthenticationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (AWSMobileClient.getInstance().getConfiguration() != null) {

            UserStateDetails userStateDetails = AWSMobileClient.getInstance().currentUserState();
            showSignInForUser(userStateDetails);
        } else {

            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    showSignInForUser(userStateDetails);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
        }
    }

    private void showSignInForUser(UserStateDetails userStateDetails) {
        Log.i(TAG, userStateDetails.getUserState().toString());
        switch (userStateDetails.getUserState()) {
            case SIGNED_IN:
                Intent i = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(i);
                break;
            case SIGNED_OUT:
                showSignIn();
                break;
            default:
                AWSMobileClient.getInstance().signOut();
                showSignIn();
                break;
        }
    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(MainActivity.class).build(),
                    new Callback<UserStateDetails>() {
                        @Override
                        public void onResult(UserStateDetails result) {
                            Log.d(TAG, "Showing Signin UI: ");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: ", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}

