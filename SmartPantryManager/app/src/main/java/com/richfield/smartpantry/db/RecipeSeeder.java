package com.richfield.smartpantry.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/** Pre-loads the 15 recipes the first time the database is created. */
final class RecipeSeeder {

    private RecipeSeeder() { }

    /** Ingredient format: "name|quantity|unit". */
    private static void add(SQLiteDatabase db, String name, String description, int minutes,
                            int servings, String[] steps, String... ingredients) {
        ContentValues r = new ContentValues();
        r.put("name", name);
        r.put("description", description);
        r.put("prep_time_minutes", minutes);
        r.put("servings", servings);
        r.put("steps", TextUtils.join("\n", steps));
        long recipeId = db.insert(DatabaseHelper.T_RECIPES, null, r);

        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues i = new ContentValues();
            i.put("recipe_id", recipeId);
            i.put("name", p[0]);
            i.put("quantity", Double.parseDouble(p[1]));
            i.put("unit", p[2]);
            db.insert(DatabaseHelper.T_INGREDIENTS, null, i);
        }
    }

    static void seed(SQLiteDatabase db) {
        add(db, "Cheesy Tomato Omelette",
                "Fluffy eggs folded around juicy tomato and melted cheese.", 10, 1,
                new String[]{"Dice the tomato and grate the cheese.",
                        "Beat the eggs with the salt.",
                        "Melt the butter in a pan over medium heat.",
                        "Pour in the eggs and cook until the edges set.",
                        "Add tomato and cheese, fold over and cook for 1 more minute."},
                "egg|3|pcs", "tomato|1|pcs", "cheese|30|g", "butter|10|g", "salt|1|tsp");

        add(db, "Garlic Butter Pasta",
                "A quick, comforting pasta tossed in garlicky butter.", 15, 2,
                new String[]{"Boil the pasta in salted water until al dente, then drain.",
                        "Mince the garlic and melt the butter in a pan.",
                        "Fry the garlic for 1 minute without browning.",
                        "Toss in the pasta, cheese and black pepper and serve."},
                "pasta|200|g", "garlic|3|pcs", "butter|30|g", "cheese|20|g", "black pepper|1|tsp");

        add(db, "Classic Pancakes",
                "Light, golden pancakes made from basic baking staples.", 20, 4,
                new String[]{"Whisk the flour, sugar, milk and eggs into a smooth batter.",
                        "Melt a little butter in a non-stick pan.",
                        "Pour in a ladle of batter and cook until bubbles form.",
                        "Flip and cook for 1 more minute. Repeat with the rest."},
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|2|tbsp", "butter|20|g");

        add(db, "Veggie Fried Rice",
                "Turn leftover rice and a few veggies into a full meal.", 20, 2,
                new String[]{"Dice the onion and grate the carrot.",
                        "Heat the oil and fry the onion and carrot for 3 minutes.",
                        "Push veggies aside, scramble the eggs in the pan.",
                        "Add the rice and soy sauce and stir-fry on high heat for 5 minutes."},
                "rice|200|g", "egg|2|pcs", "carrot|1|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|2|tbsp");

        add(db, "Simple Tomato Soup",
                "Warm, rich tomato soup built from pantry basics.", 30, 3,
                new String[]{"Chop the tomatoes, onion and garlic.",
                        "Soften the onion and garlic in oil for 5 minutes.",
                        "Add tomatoes and stock, then simmer for 15 minutes.",
                        "Blend until smooth and season to taste."},
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "vegetable stock|500|ml", "oil|1|tbsp");

        add(db, "Buttery Mashed Potatoes",
                "Creamy mash - the perfect side or a comfort snack.", 30, 3,
                new String[]{"Peel and cube the potatoes.",
                        "Boil in salted water for 15-20 minutes until soft, then drain.",
                        "Mash with the butter and warm milk.",
                        "Season with salt and serve hot."},
                "potato|500|g", "butter|40|g", "milk|100|ml", "salt|1|tsp");

        add(db, "Grilled Cheese Toastie",
                "Crispy, buttery bread with a gooey cheese centre.", 10, 2,
                new String[]{"Butter one side of each bread slice.",
                        "Place cheese between the slices, buttered sides out.",
                        "Toast in a pan for 2-3 minutes per side until golden."},
                "bread|4|pcs", "cheese|80|g", "butter|20|g");

        add(db, "Banana Oat Porridge",
                "A warm, naturally sweet breakfast bowl.", 10, 1,
                new String[]{"Combine the oats and milk in a pot.",
                        "Cook on low heat for 5 minutes, stirring often.",
                        "Slice the banana and stir half in.",
                        "Serve topped with the remaining banana and a drizzle of honey."},
                "oats|80|g", "milk|250|ml", "banana|1|pcs", "honey|1|tbsp");

        add(db, "Chicken & Rice Bowl",
                "Seasoned chicken over fluffy rice - a filling dinner.", 35, 2,
                new String[]{"Cook the rice according to the packet.",
                        "Slice the chicken and season with salt.",
                        "Fry onion and garlic in oil, then add the chicken.",
                        "Cook for 8-10 minutes until golden and cooked through.",
                        "Serve over the rice."},
                "chicken breast|300|g", "rice|200|g", "onion|1|pcs", "garlic|2|pcs", "oil|1|tbsp", "salt|1|tsp");

        add(db, "Spaghetti Bolognese",
                "Hearty mince and tomato sauce over spaghetti.", 40, 3,
                new String[]{"Fry the onion and garlic in oil until soft.",
                        "Add the minced beef and brown it well.",
                        "Chop the tomatoes, add them and simmer for 20 minutes.",
                        "Boil the spaghetti, drain and top with the sauce."},
                "spaghetti|250|g", "minced beef|300|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|1|tbsp");

        add(db, "Avocado Toast",
                "Creamy smashed avocado with a squeeze of lemon.", 5, 1,
                new String[]{"Toast the bread.",
                        "Mash the avocado with lemon juice and salt.",
                        "Spread generously over the toast."},
                "bread|2|pcs", "avocado|1|pcs", "lemon|1|pcs", "salt|1|tsp");

        add(db, "Scrambled Eggs on Toast",
                "Soft, creamy scrambled eggs on buttered toast.", 10, 1,
                new String[]{"Whisk the eggs with the milk.",
                        "Melt the butter in a pan on low heat.",
                        "Pour in the eggs and stir gently until just set.",
                        "Serve on toasted bread."},
                "egg|3|pcs", "bread|2|pcs", "butter|10|g", "milk|30|ml");

        add(db, "Potato & Onion Hash",
                "Crispy pan-fried potato and onion topped with fried eggs.", 30, 2,
                new String[]{"Dice the potatoes and onion.",
                        "Fry the potatoes in oil for 12 minutes until golden.",
                        "Add the onion and salt and cook for 5 more minutes.",
                        "Fry the eggs separately and place on top."},
                "potato|400|g", "onion|1|pcs", "egg|2|pcs", "oil|2|tbsp", "salt|1|tsp");

        add(db, "Creamy Mushroom Pasta",
                "Silky cream sauce with sautéed mushrooms and garlic.", 25, 2,
                new String[]{"Cook the pasta and drain.",
                        "Slice the mushrooms and fry in butter until browned.",
                        "Add the garlic, then the cream, and simmer for 3 minutes.",
                        "Toss the pasta through the sauce and serve."},
                "pasta|200|g", "mushroom|200|g", "cream|150|ml", "garlic|2|pcs", "butter|20|g");

        add(db, "Red Lentil Curry",
                "A warming, plant-based curry made from store-cupboard staples.", 35, 3,
                new String[]{"Chop the onion, garlic and tomatoes.",
                        "Fry the onion and garlic in oil, then stir in the curry powder.",
                        "Add the lentils, tomatoes and coconut milk.",
                        "Simmer for 20 minutes until the lentils are soft."},
                "lentil|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|2|tsp",
                "oil|1|tbsp", "coconut milk|200|ml");
    }
}
