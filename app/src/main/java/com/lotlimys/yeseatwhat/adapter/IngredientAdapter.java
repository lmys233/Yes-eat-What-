package com.lotlimys.yeseatwhat.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.model.IngredientItem;
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
        View view = LayoutInflater.from(parent.getContext())
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
            int ingredientId = item.getId();
            if (ingredientId == 0) {
                // 加号按钮：添加自定义食材
                holder.icon.setImageResource(R.drawable.ic_add);
                holder.name.setVisibility(ImageView.GONE);
                holder.card.setStrokeWidth(0);

            } else {
                holder.name.setVisibility(ImageView.VISIBLE);
                if (item instanceof IngredientItem && ((IngredientItem) item).isCustom()) {
                    // 自定义食材：直接使用 imagePath，绕过 drawable 查找（避免 Room 自增 ID 与系统食材 drawable 冲突）
                    String imagePath = ((IngredientItem) item).getImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(imagePath)
                                .placeholder(R.drawable.apic)
                                .into(holder.icon);
                    } else {
                        holder.icon.setImageResource(R.drawable.apic);
                    }
                } else {
                    // 系统食材：通过食材 ID 查找内置图片资源（ingredient_{id}.png）
                    int resId = holder.itemView.getContext().getResources()
                            .getIdentifier("ingredient_" + ingredientId, "drawable",
                                    holder.itemView.getContext().getPackageName());
                    if (resId != 0) {
                        holder.icon.setImageResource(resId);
                    } else {
                        holder.icon.setImageResource(R.drawable.apic);
                    }
                }
            }
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("method".equals(categoryType)) {
            holder.name.setVisibility(ImageView.VISIBLE);
            holder.icon.setImageResource(R.drawable.ic_wok);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("cuisine".equals(categoryType)) {
            holder.name.setVisibility(ImageView.VISIBLE);
            holder.icon.setImageResource(R.drawable.ic_share);
            holder.icon.setVisibility(ImageView.VISIBLE);
        } else if ("meal_type".equals(categoryType)) {
            holder.name.setVisibility(ImageView.VISIBLE);
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
            holder.card.setCardBackgroundColor(Color.parseColor("#FFF5F5F5"));
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
        MaterialCardView card;
        ImageView icon;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_ingredient);
            icon = itemView.findViewById(R.id.iv_ingredient_icon);
            name = itemView.findViewById(R.id.tv_ingredient_name);
        }
    }
}
