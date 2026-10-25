# Smart Pantry Manager

A Java Android application (Android Studio) that reduces food waste by tracking the ingredients you
actually have at home and suggesting recipes you can cook using **strictly** those ingredients - no
shopping trip required.

*Mobile App Development 700 - Practical Assignment, Richfield Graduate Institute of Technology.*

## Features
- **Pantry management** - add, edit and delete ingredients (name, quantity, unit, optional expiry date).
- **Pantry list** - RecyclerView + custom adapter bound to the SQLite database; expiry colour-coding and banner.
- **15 pre-loaded recipes** - seeded into the database on first run.
- **Suggested Recipes** - runs the strict-matching rule; shows only recipes you can make right now.
- **Almost There (bonus)** - separate tab listing recipes missing exactly one ingredient.
- **Recipe detail** - full ingredient list (tick/cross against your pantry) and method.
- **Settings** - expiring-soon alerts, ignore-expired-items, default unit, clear pantry.
- Friendly empty states, e.g. *"No recipes match your pantry yet - add more ingredients."*
- No Google Maps, no location/GPS, no payments.

## The strict-matching rule (`util/RecipeMatcher.java`)
A recipe is suggested **only if every ingredient is in the pantry in at least the required quantity**
(4 of 5 ingredients = not suggested). Matching is robust to real-world messiness:
- case / whitespace / plurals: `Tomato` = `tomatoes` = ` TOMATOES `
- unit conversion: `1 kg` flour satisfies `200 g`; `2 tbsp` satisfies `30 ml`
- duplicate pantry rows of the same ingredient are added together
- expired items can be ignored (Settings)

Weights and volumes are treated as one "amount" dimension (1 g ~ 1 ml); counted items (`pcs`) are
never converted into weights.

## Database choice: SQLite (`SQLiteOpenHelper`)
SQLite is built into Android, works fully offline, needs no server or account, and is ideal for a
single-user pantry. The data is stored on the device so it persists after the app is closed.

| Table | Purpose |
|-------|---------|
| `pantry_items` | `id`, `name`, `quantity`, `unit`, `expiry_date` - full CRUD |
| `recipes` | `id`, `name`, `description`, `prep_time_minutes`, `servings`, `steps` - seeded |
| `recipe_ingredients` | `id`, `recipe_id` (FK -> recipes, ON DELETE CASCADE), `name`, `quantity`, `unit` |

## Setup / run instructions
1. Install **Android Studio** (Koala or newer) with JDK 17 (bundled).
2. `File > Open` and select the project folder (the one containing `settings.gradle`).
3. Let Gradle sync (needs internet the first time).
4. Create/start an emulator (API 24+) or connect a phone with USB debugging.
5. Press **Run** (green triangle). The recipes are created automatically on first launch.
6. Unit tests: `./gradlew test` or right-click `RecipeMatcherTest` > Run.

**Requirements:** minSdk 24, compileSdk/targetSdk 34, Android Gradle Plugin 8.5.2, Gradle 8.7, Java 8 source level.

## Project structure
```
app/src/main/java/com/richfield/smartpantry/
  model/   PantryItem, Recipe, RecipeIngredient, MatchResult
  db/      DatabaseHelper (SQLite CRUD), RecipeSeeder (15 recipes)
  util/    RecipeMatcher (core logic), IngredientNormalizer, UnitConverter, AppDateUtils, Format
  ui/      PantryActivity, AddEditIngredientActivity, SuggestedRecipesActivity,
           RecipeDetailActivity, SettingsActivity, BaseActivity, adapters, AppPreferences
```
