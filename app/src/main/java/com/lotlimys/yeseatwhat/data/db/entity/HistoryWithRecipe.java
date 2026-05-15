package com.lotlimys.yeseatwhat.data.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HistoryWithRecipe {
    @Embedded
    public History history;

    @Relation(
            parentColumn = "recipe_key",
            entityColumn = "recipe_key"
    )
    public GeneratedRecipe recipe;
}
