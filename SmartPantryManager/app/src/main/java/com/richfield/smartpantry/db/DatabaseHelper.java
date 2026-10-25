package com.richfield.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite database (SQLiteOpenHelper). Three tables:
 *
 *   pantry_items       (user data  - full CRUD)
 *   recipes            (seeded on first run, read-only in the UI)
 *   recipe_ingredients (child of recipes via foreign key, ON DELETE CASCADE)
 *
 * Data is stored in the app's private database file, so it survives closing the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public static final String T_PANTRY = "pantry_items";
    public static final String T_RECIPES = "recipes";
    public static final String T_INGREDIENTS = "recipe_ingredients";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT, "
                + "prep_time_minutes INTEGER NOT NULL, "
                + "servings INTEGER NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_ing_recipe ON " + T_INGREDIENTS + "(recipe_id)");

        // Pre-load the recipe collection on first run.
        RecipeSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ===================== PANTRY: CREATE / READ / UPDATE / DELETE =====================

    /** CREATE - returns the new row id, or -1 on failure. */
    public long addPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ (list) - all pantry items, alphabetically. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(readPantry(c));
        }
        return list;
    }

    /** READ (single) - used to pre-fill the edit form. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readPantry(c) : null;
        }
    }

    /** UPDATE - returns the number of rows changed. */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE - returns the number of rows removed. */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    public int deleteAllPantryItems() {
        return getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        if (item.getExpiryDate() == null) v.putNull("expiry_date");
        else v.put("expiry_date", item.getExpiryDate());
        return v;
    }

    private PantryItem readPantry(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(c.getColumnIndexOrThrow("expiry_date")) ? null
                        : c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // ===================== RECIPES (read-only) =====================

    /** All recipes with their ingredients (2 queries, no N+1). */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> map = new LinkedHashMap<>();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = readRecipe(c);
                map.put(r.getId(), r);
            }
        }
        try (Cursor c = db.query(T_INGREDIENTS, null, null, null, null, null, "id ASC")) {
            while (c.moveToNext()) {
                Recipe r = map.get(c.getLong(c.getColumnIndexOrThrow("recipe_id")));
                if (r != null) r.getIngredients().add(readIngredient(c));
            }
        }
        return new ArrayList<>(map.values());
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe = null;
        try (Cursor c = db.query(T_RECIPES, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) recipe = readRecipe(c);
        }
        if (recipe == null) return null;
        try (Cursor c = db.query(T_INGREDIENTS, null, "recipe_id = ?", new String[]{String.valueOf(id)},
                null, null, "id ASC")) {
            while (c.moveToNext()) recipe.getIngredients().add(readIngredient(c));
        }
        return recipe;
    }

    /** Distinct ingredient names used by recipes - powers the auto-complete in the Add form. */
    public List<String> getKnownIngredientNames() {
        List<String> names = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT name FROM " + T_INGREDIENTS + " ORDER BY name COLLATE NOCASE", null)) {
            while (c.moveToNext()) names.add(c.getString(0));
        }
        return names;
    }

    private Recipe readRecipe(Cursor c) {
        Recipe r = new Recipe();
        r.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        r.setName(c.getString(c.getColumnIndexOrThrow("name")));
        r.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        r.setPrepTimeMinutes(c.getInt(c.getColumnIndexOrThrow("prep_time_minutes")));
        r.setServings(c.getInt(c.getColumnIndexOrThrow("servings")));
        r.setSteps(c.getString(c.getColumnIndexOrThrow("steps")));
        return r;
    }

    private RecipeIngredient readIngredient(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")));
    }
}
