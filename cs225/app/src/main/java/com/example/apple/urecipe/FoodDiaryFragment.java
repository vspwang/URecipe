package com.example.apple.urecipe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.apple.urecipe.db.DatabaseAccess;
import com.example.apple.urecipe.db.Recommendation;
import com.example.apple.urecipe.module.Food;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FoodDiaryFragment extends Fragment {

    private TextView result;
    private Button query_button;
    private EditText cal;
    public static final String TAG = "Urecipe";

    private ListView recommend_view;
    private List<Food> recommend_list;
    private TextView pref_view;
    public ArrayList<String> result_list = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_diary, container, false);

        Recommendation recommend = new Recommendation(getContext());

        recommend_list = recommend.recmdFood();

        for (Food food: recommend_list) {
            result_list.add(food.getName()+", "+food.getCalories()+" calories");
        }

        recommend_view = (ListView) view.findViewById(R.id.recommend);

        ArrayAdapter adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_1,
                result_list);

        recommend_view.setAdapter(adapter);

        String pref = recommend.getPref();
        Log.i(TAG, "Pref: " + pref);
        pref_view = view.findViewById(R.id.pref);
        pref_view.setText("Preference: " + pref);

        return view;
    }
}