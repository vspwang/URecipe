package com.example.apple.urecipe.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.apple.urecipe.module.Food;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

public class DatabaseAccess {
    private SQLiteAssetHelper openHelper;
    private SQLiteDatabase db;
    private static  DatabaseAccess instance;
    Cursor c = null;

    private DatabaseAccess(Context context){
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static  DatabaseAccess getInstance(Context context){
        if(instance == null)
            instance = new DatabaseAccess(context);
        return instance;
    }

    public void open(){
        this.db=openHelper.getWritableDatabase();
    }

    public void close(){
        if (db!=null){
            this.db.close();
        }
    }

    // Query
    public String getPreference(){
        String[] Ftype = {"beef","chicken","pork","meat","egg","vegetable","salad","seafood"};
        int[] foodPre = {0,0,0,0,0,0,0,0};
        c = db.rawQuery("select beef,chicken,pork,meat,egg,vegetable,salad,seafood from UserIntake\n" +
                "left join Food_Nutri on UserIntake.Fid=Food_Nutri.Rid", new String[]{});
        while (c.moveToNext()){
            for (int i=0;i<8;i++){
                int temp = bolstrToint(c.getString(i));
                if (temp==1)
                    foodPre[i] += temp;
            }
        }
        int maxAt = 0;
        for (int i = 0; i < foodPre.length; i++) {
            maxAt = foodPre[i] > foodPre[maxAt] ? i : maxAt;
        }
        return Ftype[maxAt];
    }

    public int getHistoryByOption(String option, int dateBefore){
        // option{calories,fat,protein}
        // dateBefore:{0:today,1:yesterday,2:the day before yesterday...}
        c = db.rawQuery("SELECT sum(Food_Nutri."+option+") " +
                "FROM UserIntake,Food_Nutri "+
                "WHERE UserIntake.Fdate = date('now','-"+Integer.toString(dateBefore)+" days','localtime') AND  "+
                "UserIntake.Fid = Food_Nutri.Rid ",new String[]{});
        if (c.moveToNext()){
            return c.getInt(0);
        }
        else
            return -1;

    }

    public void addFoodHistory(Food food, String F_type){
        // Ftype: {"breakfast","lunch","dinner"}
        String id = Integer.toString(food.getId());
        c = db.rawQuery("INSERT OR REPLACE INTO UserIntake (Fdate, Ftype,Fid) VALUES (date('now','localtime'),'"
                +F_type+"',"+id+")",new String[]{});
        c.moveToFirst();
        c.close();
    }

    public String getFoodHistory(int dateBefore, String F_type){
        // dateBefore:{0:today,1:yesterday,2:the day before yesterday...}
        // Ftype: {"breakfast","lunch","dinner"}
        c = db.rawQuery("SELECT Food_Nutri.Rid, name, rating, calories,protein,fat,breakfast,lunch,dinner,beef,chicken,pork,meat,egg,vegetable,salad,seafood " +
                "FROM UserIntake, Food_Nutri " +
                "WHERE UserIntake.Fid=Food_Nutri.Rid AND " +
                "Fdate=date('now','-"+Integer.toString(dateBefore)+" days','localtime') AND Ftype='"+
                F_type+"'",new String[]{});

        if (c.moveToNext())
            return readCursor(c).get(0).getName();
        else
            return "";
    }

    public String getFoodNameHistory(int dateBefore, String F_type){
        // dateBefore:{0:today,1:yesterday,2:the day before yesterday...}
        // Ftype: {"breakfast","lunch","dinner"}
        c = db.rawQuery("SELECT name " +
                "FROM UserIntake, Food_Nutri " +
                "WHERE UserIntake.Fid=Food_Nutri.Rid AND " +
                "Fdate=date('now','-"+Integer.toString(dateBefore)+" days','localtime') AND Ftype='"+
                F_type+"'",new String[]{});
        if (c.moveToNext())
            return c.getString(0);
        else
            return "";
    }



    public List<Food> getFoodsByName(String n){
        c = db.rawQuery("SELECT * FROM Food_Nutri WHERE name LIKE '%" + n + "%' ORDER BY rating DESC LIMIT 10", new String[]{});
        List<Food> result = readCursor(c);
//        if (result.size()==0)
//            return "empty";
//        else
//            return result.get(0).getName();
        return result;
    }

    public List<Food> getFoodsByRecmd(String pref, String option, int min, int max){

        c = db.rawQuery("SELECT * FROM Food_Nutri WHERE ("+pref+" = 'True') AND ("+option
                +" BETWEEN "+min+" AND "+max+") ORDER BY rating DESC limit 10", new String[]{});
        List<Food> result = readCursor(c);

        return result;
    }


    public List<Food> getFoodsByNutri(String option, Integer min, Integer max){
        //Calories, Protein, Fat
        if (option.toLowerCase() == "calories")
        {
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE calories BETWEEN "+min+" AND "+max+" ORDER BY rating DESC limit 5", new String[]{});
        }
        else if (option.toLowerCase() == "protein")
        {
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE protein BETWEEN "+min+" AND "+max+" ORDER BY rating DESC limit 5", new String[]{});
        }
        else if (option.toLowerCase() == "fat")
        {
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE fat BETWEEN "+min+" AND "+max+" ORDER BY rating DESC limit 5", new String[]{});
        }

        List<Food> result = readCursor(c);

        return result;
    }

    public List<Food> getFoodsByType(String... types){
        //At most 5 types
        //getFoodByType("option1", "option2", ...),
        //options = {breakfast, lunch, dinner, beef, chicken, pork, meat, egg, vegetable, salad, seafood}

        String type1 = types.length > 0 ? types[0] : null;
        String type2 = types.length > 1 ? types[1] : null;
        String type3 = types.length > 2 ? types[2] : null;
        String type4 = types.length > 3 ? types[3] : null;
        String type5 = types.length > 4 ? types[4] : null;

        if (type1 != null && type2 != null && type3 != null && type4 != null && type5 != null) {
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE "+type1+" AND "+type2+" AND "+type3+" AND "+type4+" AND "+type5+" ORDER BY rating DESC limit 3", new String[]{});
        }
        else if (type1 != null && type2 != null && type3 != null && type4 != null) {
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE " + type1 + " AND " + type2 + " AND " + type3 + " AND " + type4 + " ORDER BY rating DESC limit 3", new String[]{});
        }
        else if (type1 != null && type2 != null && type3 != null){
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE "+type1+" AND "+type2+" AND "+type3+" ORDER BY rating DESC limit 3", new String[]{});
        }
        else if (type1 != null && type2 != null){
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE "+type1+" AND "+type2+" ORDER BY rating DESC limit 3", new String[]{});
        }
        else if (type1 != null){
            c = db.rawQuery("SELECT * FROM Food_Nutri WHERE "+type1+" = 'True' ORDER BY rating DESC limit 3", new String[]{});
        }

        List<Food> result = readCursor(c);

        return result;
    }

    private List<Food> readCursor(Cursor c)
    {
        List<Food> foodlist = new ArrayList<Food>();
        while(c.moveToNext())
        {
            int id,calories, protein, fat;
            String name;
            float rating;
            boolean breakfast, lunch, dinner, beef, chicken, pork, meat, egg, vegetable, salad, seafood;

            id = c.getInt(0);
            name = c.getString(1);
            rating = c.getFloat(2);
            calories = c.getInt(3);
            protein = c.getInt(4);
            fat = c.getInt(5);
            breakfast = bolstrTobol(c.getString(6));
            lunch = bolstrTobol(c.getString(7));
            dinner = bolstrTobol(c.getString(8));
            beef = bolstrTobol(c.getString(9));
            chicken = bolstrTobol(c.getString(10));
            pork = bolstrTobol(c.getString(11));
            meat = bolstrTobol(c.getString(12));
            egg = bolstrTobol(c.getString(13));
            vegetable = bolstrTobol(c.getString(14));
            salad = bolstrTobol(c.getString(15));
            seafood = bolstrTobol(c.getString(16));

            Food newfood = new Food(id,name,rating,calories,protein,fat,breakfast,lunch,dinner,
                    beef, chicken, pork, meat, egg, vegetable, salad, seafood,0);
            foodlist.add(newfood);
        }
        c.close();
        return foodlist;

    }

    private int bolstrToint(String s){
        if (s.equals("True"))
            return 1;
        else
            return 0;
    }

    private boolean bolstrTobol (String s){
        if (s.equals("True"))
            return true;
        else
            return false;
    }

}