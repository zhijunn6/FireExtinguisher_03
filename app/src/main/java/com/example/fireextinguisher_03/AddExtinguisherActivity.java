package com.example.fireextinguisher_03;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.DeleteExtinguisherMutation;
import com.amazonaws.amplify.generated.graphql.GetExtinguisherbyLocationQuery;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
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
    private ListView extinguisherListView;
    private List<GetExtinguisherbyLocationQuery.Item> extinguishers = new ArrayList<>();
    private AWSAppSyncClient mAWSAppSyncClient;
    private AlertDialog.Builder askUser;

    public static void startActivity(Context context, Location l) {
        location = l;
        Intent intent = new Intent(context, AddExtinguisherActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_extinguisher);

        extinguisherAdapter = new ExtinguisherAdapter(this, extinguishers);
        extinguisherListView = (ListView) findViewById(R.id.list_view_extinguisher);
        extinguisherListView.setAdapter(extinguisherAdapter);

//        extinguisherListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                final GetExtinguisherbyLocationQuery.Item post = (GetExtinguisherbyLocationQuery.Item) extinguishers.get(position);
//
//                askUser = new AlertDialog.Builder(AddExtinguisherActivity.this);
//                askUser.setTitle("Update/Delete extinguisher");
//                askUser.setMessage("Do you want to update/delete the extinguisher record?");
//
//                askUser.setNegativeButton("Update", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        EditExtinguisherActivity.startActivity(getBaseContext(), post.fragments().extinguisher());
//                        dialog.cancel();
//                    }
//                });
//
//                askUser.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        DeleteExtinguisherMutation deleteExtinguisherMutation = DeleteExtinguisherMutation.builder()
//                                .locationId(location.id())
//                                .extinguisherId(post.fragments().extinguisher().extinguisherId())
//                                .build();
//
//                        ClientFactory.getInstance(getApplicationContext())
//                                .mutate(deleteExtinguisherMutation)
//                                .enqueue(deleteExtinguisherCallback);
//
//                        dialog.cancel();
//                    }
//                });
//
//                AlertDialog alert = askUser.create();
//                alert.show();
//
//                return true;
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_extinguisher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addExtinguisher) {
            Toast.makeText(AddExtinguisherActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            ViewLocationActivity.startActivity(getBaseContext(), location);
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
