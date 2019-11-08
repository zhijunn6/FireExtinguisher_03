package com.example.fireextinguisher_03;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;

public class ClientFactory {
    private static volatile AWSAppSyncClient client;

    public synchronized static AWSAppSyncClient getInstance(Context context) {
        if (client == null) {
            AWSConfiguration awsConfig = new AWSConfiguration(context);

            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(awsConfig)
                    .cognitoUserPoolsAuthProvider(new CognitoUserPoolsAuthProvider() {
                        @Override
                        public String getLatestAuthToken() {
                            try {
                                return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                            } catch (Exception e) {
                                Log.e("APPSYNC_ERROR", e.getLocalizedMessage());
                                return e.getLocalizedMessage();
                            }
                        }
                    })
                    .build();
        }

        return client;
    }
//    private static volatile AWSAppSyncClient client;
//
//        public synchronized static AWSAppSyncClient getInstance(Context context) {
//            if (client == null) {
//                AWSConfiguration awsConfig = new AWSConfiguration(context);
//
//                client = AWSAppSyncClient.builder()
//                        .context(context)
//                        .awsConfiguration(awsConfig)
//                        .build();
//            }
//            return client;
//        }
}
