package com.lotlimys.yeseatwhat.ai;

import com.google.gson.annotations.SerializedName;
import com.lotlimys.yeseatwhat.model.RecipeItem;

import java.util.List;

/**
 * AI 返回的菜品列表。
 * 包含一个 dishes 数组，按食材匹配度从高到低排序。
 */
public class RecipeResponse {
    @SerializedName("dishes")
    private List<RecipeItem> dishes;

    public RecipeResponse() {}

    public RecipeResponse(List<RecipeItem> dishes) {
        this.dishes = dishes;
    }

    public List<RecipeItem> getDishes() { return dishes; }
    public void setDishes(List<RecipeItem> dishes) { this.dishes = dishes; }
}
