package com.example.apple.urecipe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apple.urecipe.db.DatabaseAccess;
import com.example.apple.urecipe.module.Food;

import java.util.ArrayList;
import java.util.List;

public class SearchRecipeActivity extends AppCompatActivity {
    private ListView result;
    private Button query_button;
    private EditText food_name;
    public ArrayList<String> result_list = new ArrayList<String>();
    public List<Food> result_food;
    final String[] type_of_meal_str = {"breakfast", "lunch", "dinner"};
    public String type_of_meal_choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);
        Spinner type_of_meal_spinner = (Spinner) findViewById(R.id.type_of_meal);

        ArrayAdapter<CharSequence> mealList = ArrayAdapter.createFromResource(SearchRecipeActivity.this,
                R.array.type_of_meal,
                android.R.layout.simple_spinner_dropdown_item);
        type_of_meal_spinner.setAdapter(mealList);

        type_of_meal_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type_of_meal_choose = type_of_meal_str[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        food_name = (EditText) findViewById(R.id.cal);
        query_button = (Button) findViewById(R.id.query_button);
        result = (ListView) findViewById(R.id.result_list);

        query_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();

                result_food = databaseAccess.getFoodsByName(food_name.getText().toString());
                for (Food food: result_food) {
                    result_list.add(food.getName()+"     "+food.getCalories()+" Cal");
                }

                ArrayAdapter adapter = new ArrayAdapter<String>(SearchRecipeActivity.this,
                        android.R.layout.simple_list_item_1,
                        result_list);

                result.setAdapter(adapter);
                result.setOnItemClickListener(onClickListView);
                databaseAccess.close();
            }
        });

    }

    /***
     * 點擊ListView事件Method
     */

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(SearchRecipeActivity.this,"Store "+ result_list.get(position) + " which has " + String.valueOf(result_food.get(position).getCalories()) +" calories, to the " + type_of_meal_choose + " history.", Toast.LENGTH_SHORT).show();

            // insert to user database
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
            databaseAccess.open();
            databaseAccess.addFoodHistory(result_food.get(position), type_of_meal_choose);
            databaseAccess.close();
            onBackPressed();
        }

    };
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
