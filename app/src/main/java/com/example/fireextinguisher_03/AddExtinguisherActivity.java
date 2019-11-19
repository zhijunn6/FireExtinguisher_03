package com.example.fireextinguisher_03;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.GetExtinguisherbyLocationQuery;
import com.amazonaws.amplify.generated.graphql.NewExtinguisherOnLocationSubscription;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import fragment.Location;

public class AddExtinguisherActivity extends AppCompatActivity {
    public static final String TAG = AddExtinguisherActivity.class.getSimpleName();

    private static Location location;
    private ExtinguisherAdapter extinguisherAdapter;
    private List<GetExtinguisherbyLocationQuery.Item> extinguishers = new ArrayList<>();
    private AWSAppSyncClient mAWSAppSyncClient;
    private AlertDialog.Builder askUser;
    private AppSyncSubscriptionCall<NewExtinguisherOnLocationSubscription.Data> subscriptionWatcher;

    public static void startActivity(Context context, Location l) {
        location = l;
        Intent intent = new Intent(context, AddExtinguisherActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_extinguisher);

        FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.refresh_extinguisher);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query();
            }
        });

        FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.scan_extinguisher);
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddExtinguisherActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        extinguisherAdapter = new ExtinguisherAdapter(this, extinguishers);
        ListView extinguisherListView = (ListView) findViewById(R.id.list_view_extinguisher);
        extinguisherListView.setAdapter(extinguisherAdapter);

        startSubscription();
    }

    private void startSubscription() {
        NewExtinguisherOnLocationSubscription subscription = NewExtinguisherOnLocationSubscription.builder().locationId(location.id()).build();
        subscriptionWatcher = ClientFactory.getInstance(this.getApplicationContext()).subscribe(subscription);
        subscriptionWatcher.execute(subscriptionCallback);
    }

    private AppSyncSubscriptionCall.Callback<NewExtinguisherOnLocationSubscription.Data> subscriptionCallback = new AppSyncSubscriptionCall.Callback<NewExtinguisherOnLocationSubscription.Data>() {
        @Override
        public void onResponse(final @Nonnull Response<NewExtinguisherOnLocationSubscription.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(ViewEventActivity.this, response.data().subscribeToEventComments().eventId().substring(0, 5) + response.data().subscribeToEventComments().content(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Subscription response: " + response.data().toString());
                    NewExtinguisherOnLocationSubscription.SubscribeToLocationExtinguishers exty = response.data().subscribeToLocationExtinguishers();
                }
            });
        }

        @Override
        public void onFailure(final @Nonnull ApolloException e) {
            Log.e(TAG, "Subscription failure", e);
        }

        @Override
        public void onCompleted() {
            Log.d(TAG, "Subscription completed");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extinguisher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_addExtinguisher) {
            ViewLocationActivity.startActivity(AddExtinguisherActivity.this, location);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Query list data when we return to the screen
        query();
    }

    public void query(){
        if(mAWSAppSyncClient == null) {
            mAWSAppSyncClient = ClientFactory.getInstance(this);
        }

        GetExtinguisherbyLocationQuery getExtinguisherbyLocationQuery = GetExtinguisherbyLocationQuery.builder().locationId(location.id()).build();

        mAWSAppSyncClient.query(getExtinguisherbyLocationQuery)
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<GetExtinguisherbyLocationQuery.Data> queryCallback = new GraphQLCall.Callback<GetExtinguisherbyLocationQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetExtinguisherbyLocationQuery.Data> response) {
            if(response.data() != null) {
                extinguishers = response.data().getLocationExtinguisher().items();
            } else {
                extinguishers = new ArrayList<>();
            }

            Log.d(TAG, "onResponse: " + extinguishers);

            extinguisherAdapter.setItems(extinguishers);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Notifying the data set has changed");
                    extinguisherAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make events api call", e);
            Log.e(TAG, e.getMessage());
        }
    };
}
