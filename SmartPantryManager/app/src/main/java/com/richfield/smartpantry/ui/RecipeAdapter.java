package com.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.Format;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the Suggested Recipes list (used for both the strict and the "almost there" lists). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener { void onRecipeClick(Recipe recipe); }

    private final List<MatchResult> results = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final MatchResult result = results.get(position);
        final Recipe recipe = result.getRecipe();

        h.name.setText(recipe.getName());
        h.meta.setText(recipe.getPrepTimeMinutes() + " min  \u2022  " + recipe.getServings()
                + (recipe.getServings() == 1 ? " serving" : " servings")
                + "  \u2022  " + recipe.getIngredients().size() + " ingredients");

        if (result.isStrictMatch()) {
            h.status.setText("\u2713 You have everything you need");
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.green_primary));
        } else {
            StringBuilder sb = new StringBuilder("Missing: ");
            for (int i = 0; i < result.getMissing().size(); i++) {
                RecipeIngredient ing = result.getMissing().get(i);
                if (i > 0) sb.append(", ");
                sb.append(Format.quantity(ing.getQuantity())).append(' ').append(ing.getUnit())
                        .append(' ').append(ing.getName());
            }
            h.status.setText(sb.toString());
            h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.orange));
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() { return results.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, meta, status;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_recipe_name);
            meta = v.findViewById(R.id.tv_recipe_meta);
            status = v.findViewById(R.id.tv_recipe_status);
        }
    }
}
