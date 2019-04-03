package com.example.apple.urecipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.util.JsonWriter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InputStream;


public class EditPersonalModelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_model);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
    public void save_personal_model(View view){

        EditText input_name = (EditText) findViewById(R.id.input_name);
        EditText input_gender = (EditText) findViewById(R.id.input_gender);
        EditText input_age = (EditText) findViewById(R.id.input_age);
        EditText input_weight = (EditText) findViewById(R.id.input_weight);
        EditText input_height = (EditText) findViewById(R.id.input_height);

        String name = input_name.getText().toString();
        String gender = input_gender.getText().toString();
        String age = input_age.getText().toString();
        String weight = input_weight.getText().toString();
        String height = input_height.getText().toString();

        SharedPreferences sharedPref = EditPersonalModelActivity.this.getSharedPreferences(
                "com.example.apple.urecipe.user_personal_model", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_name", name);
        editor.putString("user_gender", gender);
        editor.putString("user_age", age);
        editor.putString("user_weight", weight);
        editor.putString("user_height", height);
        float bmi = Float.parseFloat(weight)/(Float.parseFloat(height)/100 * Float.parseFloat(height)/100);
        editor.putFloat("user_bmi", bmi);
        float bmr ;
        if(gender.toLowerCase().equals("female")){
            bmr = 655 + (9.6f * Float.parseFloat(weight)) + 1.8f * (Float.parseFloat(height)) - (4.7f*(Float.parseFloat(age)));
        }else{
            bmr = 66 + (13.7f * Float.parseFloat(weight)) + 5 * (Float.parseFloat(height)) - (6.8f*(Float.parseFloat(age)));

        }
        editor.putFloat("user_bmr", bmr);

        editor.commit();
        onBackPressed();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
