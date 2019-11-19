package com.example.fireextinguisher_03;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.amazonaws.amplify.generated.graphql.DeleteExtinguisherMutation;
import com.amazonaws.amplify.generated.graphql.UpdateExtinguisherMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.Calendar;
import javax.annotation.Nonnull;
import fragment.Extinguisher;

public class EditExtinguisherActivity extends AppCompatActivity {
    public static final String TAG = EditExtinguisherActivity.class.getSimpleName();

    private static Extinguisher extinguisher;
    private EditText edit_extinguisherNo, edit_subLocation, edit_manufDate, edit_expDate;
    private TextView text_extinguisherNo, text_subLocation, text_manufDate, text_expDate;
    private DatePickerDialog editManufDatePick, editExpDatePick;
    private ViewSwitcher viewSwitcher;
    private Button toEditSwitch;

    public static void startActivity(Context context, Extinguisher e) {
        extinguisher = e;
        Intent intent = new Intent(context, EditExtinguisherActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_extinguisher);

        // ViewSwitcher
        viewSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);

        // XML variables
        text_extinguisherNo = (TextView)findViewById(R.id.text_extinguisher_extinguisherNumber);
        text_subLocation = (TextView)findViewById(R.id.text_extinguisher_subLocation);
        text_manufDate = (TextView)findViewById(R.id.text_extinguisher_manufacturingDate);
        text_expDate = (TextView)findViewById(R.id.text_extinguisher_expiryDate);
        edit_extinguisherNo = (EditText)findViewById(R.id.edit_extinguisher_extinguisherNumber);
        edit_subLocation = (EditText)findViewById(R.id.edit_extinguisher_subLocation);
        edit_manufDate = (EditText)findViewById(R.id.edit_extinguisher_manufacturingDate);
        edit_expDate = (EditText)findViewById(R.id.edit_extinguisher_expiryDate);
        toEditSwitch = (Button)findViewById(R.id.toEditSwitch);

        text_extinguisherNo.setText(extinguisher.extinguisherNumber());
        text_subLocation.setText(extinguisher.subLocation());
        text_manufDate.setText(extinguisher.manufacturingDate());
        text_expDate.setText(extinguisher.expiryDate());
        edit_extinguisherNo.setText(extinguisher.extinguisherNumber());
        edit_subLocation.setText(extinguisher.subLocation());
        edit_manufDate.setText(extinguisher.manufacturingDate());
        edit_expDate.setText(extinguisher.expiryDate());

        edit_manufDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                editManufDatePick = new DatePickerDialog(EditExtinguisherActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                edit_manufDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                editManufDatePick.show();
            }
        });

        edit_expDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                editExpDatePick = new DatePickerDialog(EditExtinguisherActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                edit_expDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                editExpDatePick.show();
            }
        });

        toEditSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewSwitcher.showNext();
            }
        });
    }

    public void editExtinguisher(View v){

        text_extinguisherNo.setText(edit_extinguisherNo.getText().toString());
        text_subLocation.setText(edit_subLocation.getText().toString());
        text_manufDate.setText(edit_manufDate.getText().toString());
        text_expDate.setText(edit_expDate.getText().toString());
        viewSwitcher.showNext();

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

    @Override
    public void onBackPressed() {
        finish();
    }

}
