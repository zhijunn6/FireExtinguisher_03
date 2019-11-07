package com.example.fireextinguisher_03;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateLocationMutation;
import com.amazonaws.amplify.generated.graphql.DeleteExtinguisherMutation;
import com.amazonaws.amplify.generated.graphql.DeleteLocationMutation;
import com.amazonaws.amplify.generated.graphql.ExtinguisherOnLocationMutation;
import com.amazonaws.amplify.generated.graphql.GetLocationQuery;
import com.amazonaws.amplify.generated.graphql.NewExtinguisherOnLocationSubscription;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import fragment.Location;
import type.DeleteLocationInput;

public class ViewLocationActivity extends AppCompatActivity {
    public static final String TAG = ViewLocationActivity.class.getSimpleName();

    private static Location location;
    private TextView name, address, extinguishers;
    private EditText newExtinguisher_extinguisherNo, newExtinguisher_subLocation, newExtinguisher_manufDate, newExtinguisher_expDate;
    private AppSyncSubscriptionCall<NewExtinguisherOnLocationSubscription.Data> subscriptionWatcher;
    private DatePickerDialog manufDatePick, expDatePick;

    public static void startActivity(Context context, Location l) {
        location = l;
        Intent intent = new Intent(context, ViewLocationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);

        name = (TextView)findViewById(R.id.viewName);
        address = (TextView)findViewById(R.id.viewAddress);
        newExtinguisher_extinguisherNo = (EditText)findViewById(R.id.new_extinguisher_extinguisherNumber);
        newExtinguisher_subLocation = (EditText)findViewById(R.id.new_extinguisher_subLocation);
        newExtinguisher_manufDate = (EditText)findViewById(R.id.new_extinguisher_manufacturingDate);
        newExtinguisher_expDate = (EditText)findViewById(R.id.new_extinguisher_expiryDate);
        extinguishers = (TextView)findViewById(R.id.extinguishers);

        name.setText(location.name());
        address.setText(location.address());

        Log.d(TAG, "onCreate x called");

        newExtinguisher_manufDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                manufDatePick = new DatePickerDialog(ViewLocationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                newExtinguisher_manufDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                manufDatePick.show();
            }
        });

        newExtinguisher_expDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day1 = cldr.get(Calendar.DAY_OF_MONTH);
                int month1 = cldr.get(Calendar.MONTH);
                int year1 = cldr.get(Calendar.YEAR);
                // date picker dialog
                expDatePick = new DatePickerDialog(ViewLocationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                newExtinguisher_expDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year1, month1, day1);
                expDatePick.show();
            }
        });

        refreshExtinguishers(true);
        startSubscription();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String message = intent.getStringExtra(SnapActivity.EXTRA_MESSAGE);
//        Log.d(TAG, message);
        newExtinguisher_extinguisherNo.setText(message);

        Log.d(TAG, "onResume x called");
    }

    @Override
    protected void onStop() {
        super.onStop();

            if(subscriptionWatcher != null) {
                subscriptionWatcher.cancel();
            }
    }

    public void toSnapImageActivity(View view) {
        Intent intent = new Intent(this, SnapActivity.class);
        startActivity(intent);
    }

    private void startSubscription() {
        NewExtinguisherOnLocationSubscription subscription = NewExtinguisherOnLocationSubscription.builder().locationId(location.id()).build();

        subscriptionWatcher = ClientFactory.getInstance(this.getApplicationContext()).subscribe(subscription);
        subscriptionWatcher.execute(subscriptionCallback);

    }

    private AppSyncSubscriptionCall.Callback<NewExtinguisherOnLocationSubscription.Data> subscriptionCallback = new AppSyncSubscriptionCall.Callback<NewExtinguisherOnLocationSubscription.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<NewExtinguisherOnLocationSubscription.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ViewLocationActivity.this, response.data().subscribeToLocationExtinguishers().locationId().substring(0, 5) + response.data().subscribeToLocationExtinguishers().extinguisherNumber(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Subscription response: " + response.data().toString());
                    NewExtinguisherOnLocationSubscription.SubscribeToLocationExtinguishers extinguisher = response.data().subscribeToLocationExtinguishers();

                    addExtinguisher(extinguisher.extinguisherNumber());

                    addExtinguisherToCache(extinguisher);

                    refreshExtinguishers(true);
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Subscription failure", e);
        }

        @Override
        public void onCompleted() {
            Log.d(TAG, "Subscription completed");
        }
    };

    /**
     * UI triggered method to add a comment. This will read the text box and submit a new comment.
     * @param view
     */
    public void addExtinguisher(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newExtinguisher_extinguisherNo.getWindowToken(), 0);

        Toast.makeText(this, "Creating extinguisher", Toast.LENGTH_SHORT).show();

        ExtinguisherOnLocationMutation extinguisher = ExtinguisherOnLocationMutation.builder()
                .locationId(location.id())
                .extinguisherNumber(newExtinguisher_extinguisherNo.getText().toString())
                .subLocation(newExtinguisher_subLocation.getText().toString())
                .manufacturingDate(newExtinguisher_manufDate.getText().toString())
                .expiryDate(newExtinguisher_expDate.getText().toString())
                .build();

        ClientFactory.getInstance(view.getContext())
                .mutate(extinguisher)
                .enqueue(addExtinguisherCallback);
    }

    public void deleteLocation(View view){
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());

        DeleteLocationInput input = DeleteLocationInput.builder()
                .id(location.id())
                .build();

//        DeleteExtinguisherInput input2 = DeleteExtinguisherInput.builder()
//                .id(location.id())
//                .build();

        DeleteLocationMutation deleteLocationMutation = DeleteLocationMutation.builder()
                .input(input)
                .build();

//        DeleteExtinguisherMutation deleteExtinguisherMutation = DeleteExtinguisherMutation.builder()
//                .input(input2)
//                .build();

        awsAppSyncClient.mutate(deleteLocationMutation).enqueue(mutateCallback);
//        awsAppSyncClient.mutate(deleteExtinguisherMutation).enqueue(mutateCallback2);
    }

    // Mutation callback code
    private GraphQLCall.Callback<DeleteLocationMutation.Data> mutateCallback = new GraphQLCall.Callback<DeleteLocationMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteLocationMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ViewLocationActivity.this, "Added location", Toast.LENGTH_SHORT).show();
                    ViewLocationActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddLocationMutation", e);
                    Toast.makeText(ViewLocationActivity.this, "Failed to add location", Toast.LENGTH_SHORT).show();
                    ViewLocationActivity.this.finish();
                }
            });
        }
    };

//    // Mutation callback code
//    private GraphQLCall.Callback<DeleteExtinguisherMutation.Data> mutateCallback2 = new GraphQLCall.Callback<DeleteExtinguisherMutation.Data>() {
//        @Override
//        public void onResponse(@Nonnull final Response<DeleteExtinguisherMutation.Data> response) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(ViewLocationActivity.this, "Added location", Toast.LENGTH_SHORT).show();
//                    ViewLocationActivity.this.finish();
//                }
//            });
//        }
//
//        @Override
//        public void onFailure(@Nonnull final ApolloException e) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.e("", "Failed to perform AddLocationMutation", e);
//                    Toast.makeText(ViewLocationActivity.this, "Failed to add location", Toast.LENGTH_SHORT).show();
//                    ViewLocationActivity.this.finish();
//                }
//            });
//        }
//    };

    /**
     * Service response subscriptionCallback confirming receipt of new comment triggered by UI.
     */
    private GraphQLCall.Callback<ExtinguisherOnLocationMutation.Data> addExtinguisherCallback = new GraphQLCall.Callback<ExtinguisherOnLocationMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ExtinguisherOnLocationMutation.Data> response) {
            Log.d(TAG, response.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearExtinguishers();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to make extinguisher mutation", e);
            Log.e(TAG, e.getMessage());
        }
    };

    /**
     * Adds the new comment to the event in the cache.
     * @param extinguisher
     */
    private void addExtinguisherToCache(NewExtinguisherOnLocationSubscription.SubscribeToLocationExtinguishers extinguisher){
        try {
            //Read old location data
            GetLocationQuery getLocationQuery = GetLocationQuery.builder().id(location.id()).build();
            //GetLocationQuery.Data readData = ClientFactory.appSyncClient().getStore().read(getLocationQuery).execute();
            GetLocationQuery.Data readData = ClientFactory.getInstance(ViewLocationActivity.this).getStore().read(getLocationQuery).execute();
            Location location = readData.getLocation().fragments().location();

            //Create new extinguisher object
            Location.Item newExtinguisher = new Location.Item(
                    extinguisher.__typename(),
                    extinguisher.locationId(),
                    extinguisher.extinguisherId(),
                    extinguisher.extinguisherNumber(),
                    extinguisher.subLocation(),
                    extinguisher.manufacturingDate(),
                    extinguisher.expiryDate()
            );

            //Create new extinguisher list attached to the location
            List<Location.Item> items = new LinkedList<>(location.extinguisher().items());
            items.add(0, newExtinguisher);

            //Create new location data
            GetLocationQuery.Data madeData = new GetLocationQuery.Data(new GetLocationQuery.GetLocation(readData.getLocation().__typename(),
                    new GetLocationQuery.GetLocation.Fragments(new Location(readData.getLocation().fragments().location().__typename(),
                            location.id(),
                            location.name(),
                            location.address(),
                            new Location.Extinguisher(readData.getLocation().fragments().location().extinguisher().__typename(), items)))));

            //Write new location data
            //ClientFactory.appSyncClient().getStore().write(getLocationQuery, madeData).execute();
            ClientFactory.getInstance(ViewLocationActivity.this).getStore().write(getLocationQuery, madeData).execute();
            Log.d(TAG, "Wrote extinguisher to database");
        } catch (ApolloException e) {
            Log.e(TAG, "Failed to update local database", e);
        }
    }

    /**
     * Refresh the event object to latest from service.
     */
    private void refreshExtinguishers(final boolean cacheOnly) {
        Log.d(TAG, "refreshExtinguishers called");
        GetLocationQuery getLocationQuery = GetLocationQuery.builder().id(location.id()).build();

        //ClientFactory.appSyncClient()
        ClientFactory.getInstance(getApplicationContext())
                .query(getLocationQuery)
                .responseFetcher(cacheOnly ? AppSyncResponseFetchers.CACHE_ONLY : AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(refreshExtinguisherCallback);
    }

    private GraphQLCall.Callback<GetLocationQuery.Data> refreshExtinguisherCallback = new GraphQLCall.Callback<GetLocationQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<GetLocationQuery.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response.errors().size() < 1) {
                        location = response.data().getLocation().fragments().location();
                        refreshExtinguishers();
                    } else {
                        Log.e(TAG, "Failed to get event.");
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, "Failed to get event.");
        }
    };

    private void addExtinguisher(final String extinguisher) {
        extinguishers.setText(extinguisher + "\n-----------\n");
    }

    private void refreshExtinguishers() {
        Log.d(TAG, "refreshExtinguishers: called!");
        StringBuilder stringBuilder = new StringBuilder();
        for (Location.Item i : location.extinguisher().items()) {
            stringBuilder.append(i.extinguisherNumber() + "\n---------\n");
        }

        extinguishers.setText(stringBuilder.toString());
    }

    private void clearExtinguishers() {
        newExtinguisher_extinguisherNo.setText("");
        newExtinguisher_subLocation.setText("");
        newExtinguisher_manufDate.setText("");
        newExtinguisher_expDate.setText("");
        ViewLocationActivity.this.finish();}
}
