# URecipe

URecipe is a health management application that can keep track of your step count and calories expended every day. Also, record your food diary, Urecipe could recommend food based on your preference.

---

Class Explain

MainActivity:
- Subscribe the Google Fit API, including step count and expended calories
- Contains three Fragments, HomeFragment, UserFragment, FoodDiaryFragment

HomeFragment:
- Read the step count and calories, extract daily and within one week for personal model
- Show the recipe that user have for each day
- User can record waht he/she eat by "ADD NEW DIARY" button, it will run the SearchRecipeActivity

UserFragment:
- Record user profile, including name, gender, age, height, weight, and BMI, for personal model
- Use SharedPreferences to store data

FoodDiaryFragment:


SearchRecipeActivity:
- Search query in Food database we have
- Record what user eat each day

EditPersonalModelActivity:
- Update the information of personal model

module.Food:
- Define object Food, which includes different properties of food, includes calories and other nutrition informations

db
DatabaseOpenHelper:
- connect to database

DatabaseAccess:
- Include different query for searching database

Recommendation:
- Assess user's health state
- Provide personalized recommendations according to user's health state and personal model
