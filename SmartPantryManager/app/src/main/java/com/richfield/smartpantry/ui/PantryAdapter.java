package com.richfield.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.AppDateUtils;
import com.richfield.smartpantry.util.Format;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter that binds PantryItem rows (from SQLite) to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onItemClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        Context ctx = h.itemView.getContext();

        h.name.setText(Format.capitalize(item.getName()));
        h.quantity.setText(Format.quantity(item.getQuantity()) + " " + item.getUnit());

        Integer days = AppDateUtils.daysUntil(item.getExpiryDate());
        int color;
        String text;
        if (days == null) {
            text = "No expiry date";
            color = R.color.grey;
        } else if (days < 0) {
            text = "Expired " + (-days) + (days == -1 ? " day ago" : " days ago");
            color = R.color.red;
        } else if (days == 0) {
            text = "Expires today";
            color = R.color.orange;
        } else if (days <= AppDateUtils.EXPIRING_SOON_DAYS) {
            text = "Expires in " + days + (days == 1 ? " day" : " days");
            color = R.color.orange;
        } else {
            text = "Expires " + AppDateUtils.formatDisplay(item.getExpiryDate());
            color = R.color.grey;
        }
        h.expiry.setText(text);
        h.expiry.setTextColor(ContextCompat.getColor(ctx, color));

        h.itemView.setOnClickListener(v -> listener.onItemClick(item));
        h.delete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, quantity, expiry;
        final ImageButton delete;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_item_name);
            quantity = v.findViewById(R.id.tv_item_quantity);
            expiry = v.findViewById(R.id.tv_item_expiry);
            delete = v.findViewById(R.id.btn_item_delete);
        }
    }
}
