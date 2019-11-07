package com.example.fireextinguisher_03;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListLocationsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton btnAddLocation = findViewById(R.id.btn_addPet);
//        btnAddLocation.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                Intent addLocationIntent = new Intent(MainActivity.this, AddLocationActivity.class);
//                MainActivity.this.startActivity(addLocationIntent);
//            }
//        });
        FloatingActionButton btnSignout = findViewById(R.id.btn_signOut);
        btnSignout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AWSMobileClient.getInstance().signOut();
                Intent authIntent = new Intent(MainActivity.this, AuthenticationActivity.class);
                finish();
                startActivity(authIntent);
            }
        });

        mAdapter = new MyAdapter(this, locations);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addLocation) {
            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            Intent addLocationIntent = new Intent(MainActivity.this, AddLocationActivity.class);
            MainActivity.this.startActivity(addLocationIntent);
            return true;
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
    public void query(){
        Log.d(TAG, "query: Called");
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

            Log.d(TAG, "Locations are here: " + locations);
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
            Log.e(TAG, "Failed to make events api call", e);
            Log.e(TAG, e.getMessage());
        }
    };
}
