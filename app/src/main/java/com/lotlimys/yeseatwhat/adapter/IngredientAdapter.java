package com.lotlimys.yeseatwhat.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.model.SelectableItem;

import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private List<SelectableItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private int selectedBgColor;
    private int selectedTextColor;
    private int selectedStrokeColor;

    public IngredientAdapter() {
        // Default colors (will be overridden by fragment)
        this.selectedBgColor = 0xFFE8DEF8;
        this.selectedTextColor = 0xFF3700B3;
        this.selectedStrokeColor = 0xFF6200EE;
    }

    public void setThemeColors(int bgColor, int textColor, int strokeColor) {
        this.selectedBgColor = bgColor;
        this.selectedTextColor = textColor;
        this.selectedStrokeColor = strokeColor;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(SelectableItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<? extends SelectableItem> items) {
        this.items = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public List<SelectableItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectableItem item = items.get(position);
        holder.name.setText(item.getName());

        // Set icon based on item type
        String categoryType = item.getCategoryType();
        if ("ingredient".equals(categoryType)) {
            holder.icon.setImageResource(R.drawable.apic);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("method".equals(categoryType)) {
            holder.icon.setImageResource(R.drawable.ic_wok);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("cuisine".equals(categoryType)) {
            holder.icon.setImageResource(R.drawable.ic_share);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("meal_type".equals(categoryType)) {
            holder.icon.setImageResource(R.drawable.ic_plate);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else {
            holder.icon.setVisibility(ImageView.GONE);
        }

        if (item.isSelected()) {
            holder.card.setCardBackgroundColor(selectedBgColor);
            holder.card.setStrokeWidth(
                    holder.itemView.getResources().getDimensionPixelSize(R.dimen.stroke_selected));
            holder.card.setStrokeColor(ColorStateList.valueOf(selectedStrokeColor));
            holder.name.setTextColor(selectedTextColor);
        } else {
            holder.card.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FFF5F5F5"));
            holder.card.setStrokeWidth(0);
            holder.name.setTextColor(holder.itemView.getContext()
                    .getColor(android.R.color.tab_indicator_text));
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView card;
        ImageView icon;
        TextView name;

        ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_ingredient);
            icon = itemView.findViewById(R.id.iv_ingredient_icon);
            name = itemView.findViewById(R.id.tv_ingredient_name);
        }
    }
}
