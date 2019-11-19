package com.example.fireextinguisher_03;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListLocationsQuery;
import com.amazonaws.amplify.generated.graphql.NewExtinguisherOnLocationSubscription;
import com.amazonaws.amplify.generated.graphql.OnCreateLocationSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private MyAdapter mAdapter;
    private List<ListLocationsQuery.Item> locations = new ArrayList<>();
    private AWSAppSyncClient mAWSAppSyncClient;
    private AppSyncSubscriptionCall subscriptionWatcher;

    public static void startActivity(Context appContext) {
        Intent intent = new Intent(appContext, MainActivity.class);
        appContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.refresh_locations);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query();
            }
        });

        mAdapter = new MyAdapter(this, locations);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_addLocation) {
            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            Intent addLocationIntent = new Intent(MainActivity.this, AddLocationActivity.class);
            MainActivity.this.startActivity(addLocationIntent);
            return true;
        } else if (id == R.id.options) {
            AWSMobileClient.getInstance().signOut();
            Intent authIntent = new Intent(MainActivity.this, AuthenticationActivity.class);
            finish();
            startActivity(authIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Query list data when we return to the screen
        Log.d(TAG, "onResume: Called");
        query();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.exit_string)
                .setPositiveButton(R.string.yes_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public void query(){
        if(mAWSAppSyncClient == null) {
            mAWSAppSyncClient = ClientFactory.getInstance(this);
        }
        mAWSAppSyncClient.query(ListLocationsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListLocationsQuery.Data> queryCallback = new GraphQLCall.Callback<ListLocationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListLocationsQuery.Data> response) {
            if(response.data() != null) {
                locations = response.data().listLocations().items();
            } else {
                locations = new ArrayList<>();
            }

            mAdapter.setItems(locations);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Notifying the data set has changed");
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make FireEX api call", e);
            Log.e(TAG, e.getMessage());
        }
    };
}
