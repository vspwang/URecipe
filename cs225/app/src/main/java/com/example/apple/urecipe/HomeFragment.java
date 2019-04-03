package com.example.apple.urecipe;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.apple.urecipe.common.logger.LogView;
import com.example.apple.urecipe.common.logger.LogWrapper;
import com.example.apple.urecipe.common.logger.MessageOnlyLogFilter;
import com.example.apple.urecipe.db.DatabaseAccess;
import com.example.apple.urecipe.db.Recommendation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

// import com.example.apple.urecipe.common.logger.Log;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    public static final String TAG = "Urecipe";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private float expended_calories = 0;
    private int step_count = 0;
    private float user_bmr = 0.0f;
    private TextView result;
    private Button query_button;
    private EditText cal;

    private TextView expended_calories_view;
    private TextView step_count_view;
    private TextView user_bmr_view;
    private TextView week_step_count_view;
    private TextView week_calories_view;
    private TextView health_view;
    private TextView breakfast_view;
    private TextView lunch_view;
    private TextView dinner_view;

    private Button add_new_diary;

    public int week_step_count = 0;
    public float week_expended_calories = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // initializeLogging();

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        } else {
            subscribeCalories();
            subscribeStepCount();
        }

        readCaloriesData();
        readStepCountData();

        readStepCountHistoryData();
        readCaloriesHistoryData();

        SharedPreferences sharedPref = HomeFragment.this.getActivity().getSharedPreferences(
                "com.example.apple.urecipe.user_personal_model", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        user_bmr = sharedPref.getFloat("user_bmr", 0.0f);
        editor.putFloat("user_expendedCal", expended_calories);

        Recommendation recommend = new Recommendation(getContext());

        String health = recommend.getHealthState();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
        databaseAccess.open();

        step_count_view = view.findViewById(R.id.step_count);
        step_count_view.setText("Step Count: " + String.valueOf(step_count));
        week_step_count_view = view.findViewById(R.id.week_step_count);
        week_step_count_view.setText("Week Step Count: " + String.valueOf(week_step_count));
        expended_calories_view = view.findViewById(R.id.expanded_calories);
        expended_calories_view.setText("Expended Calories today: " + String.valueOf(expended_calories));
        week_calories_view = view.findViewById(R.id.week_calories);
        week_calories_view.setText("Week Calories: " + String.valueOf(week_expended_calories));
        user_bmr_view = view.findViewById(R.id.user_bmr);
        user_bmr_view.setText("BMR: " + String.valueOf(user_bmr));
        health_view = view.findViewById(R.id.health);
        health_view.setText("Health State: " + health);

        breakfast_view = view.findViewById(R.id.result_breakfast);
        breakfast_view.setText(databaseAccess.getFoodNameHistory(0, "breakfast"));
        lunch_view = view.findViewById(R.id.result_lunch);
        lunch_view.setText(databaseAccess.getFoodNameHistory(0, "lunch"));
        dinner_view = view.findViewById(R.id.result_dinner);
        dinner_view.setText(databaseAccess.getFoodNameHistory(0, "dinner"));
        databaseAccess.close();

        add_new_diary = (Button) view.findViewById(R.id.add_new_diary);

        View.OnClickListener listener =new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(), SearchRecipeActivity.class);
                startActivity(intent);
            }
        };

        add_new_diary.setOnClickListener(listener);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribeCalories();
                subscribeStepCount();
            }
        }
    }


    /** Records step data by requesting a subscription to background step data. */
    public void subscribeCalories() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /** Records step data by requesting a subscription to background step data. */
    public void subscribeStepCount() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */

    private void readCaloriesData() {
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                 expended_calories =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                                Log.i(TAG, "Total calories: " + expended_calories);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the calories.", e);
                            }
                        });
    }

    private void readStepCountData() {
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                step_count =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                Log.i(TAG, "Step Count: " + step_count);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the calories.", e);
                            }
                        });
    }

    /**
     * Asynchronous task to read the history data. When the task succeeds, it will print out the data.
     */
    private Task<DataReadResponse> readCaloriesHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryCaloriesData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                // reasons.
                                cumulateCalories(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }

    /**
     * Asynchronous task to read the history data. When the task succeeds, it will print out the data.
     */
    private Task<DataReadResponse> readStepCountHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryStepCountData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                // reasons.
                                cumulateStepCount(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }


    /** Returns a {@link DataReadRequest} for all step count changes in the past week. */
    public static DataReadRequest queryCaloriesData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();
        // [END build_read_data_request]

        return readRequest;
    }


    /** Returns a {@link DataReadRequest} for all step count changes in the past week. */
    public static DataReadRequest queryStepCountData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();
        // [END build_read_data_request]

        return readRequest;
    }

    public void cumulateCalories(DataReadResponse dataReadResult) {
        week_expended_calories = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket: dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet: dataSets) {
                    week_expended_calories += dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                }
            }
        }
        Log.i(TAG, "week_calories: " + String.valueOf(week_expended_calories));
    }

    public void cumulateStepCount(DataReadResponse dataReadResult) {
        week_step_count = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket: dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet: dataSets) {
                    week_step_count += dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                }
            }
        }
        Log.i(TAG, "week_step_count: " + String.valueOf(week_step_count));
    }



    // for testing
    public static void printData(DataReadResponse dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(
                    TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }


    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }
}
