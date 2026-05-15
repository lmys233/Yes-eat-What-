package com.lotlimys.yeseatwhat.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lotlimys.yeseatwhat.data.db.dao.CategoryDao;
import com.lotlimys.yeseatwhat.data.db.dao.FavoriteDao;
import com.lotlimys.yeseatwhat.data.db.dao.GenerationRecordDao;
import com.lotlimys.yeseatwhat.data.db.dao.HistoryDao;
import com.lotlimys.yeseatwhat.data.db.dao.IngredientDao;
import com.lotlimys.yeseatwhat.data.db.dao.RecipeDao;
import com.lotlimys.yeseatwhat.data.db.entity.Category;
import com.lotlimys.yeseatwhat.data.db.entity.CustomIngredient;
import com.lotlimys.yeseatwhat.data.db.entity.Favorite;
import com.lotlimys.yeseatwhat.data.db.entity.GeneratedRecipe;
import com.lotlimys.yeseatwhat.data.db.entity.GenerationRecord;
import com.lotlimys.yeseatwhat.data.db.entity.History;
import com.lotlimys.yeseatwhat.data.db.entity.SystemIngredient;
import com.lotlimys.yeseatwhat.data.db.seed.DataSeeder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Category.class,
        SystemIngredient.class,
        CustomIngredient.class,
        GeneratedRecipe.class,
        Favorite.class,
        History.class,
        GenerationRecord.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "yeseatwhat.db";
    private static volatile AppDatabase instance;

    public abstract CategoryDao categoryDao();
    public abstract IngredientDao ingredientDao();
    public abstract RecipeDao recipeDao();
    public abstract FavoriteDao favoriteDao();
    public abstract HistoryDao historyDao();
    public abstract GenerationRecordDao generationRecordDao();

    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private static volatile boolean seedChecked = false;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            .addCallback(new SeedCallback())
                            .fallbackToDestructiveMigration()
                            .build();

                    ensureSeeded();
                }
            }
        }
        return instance;
    }

    private static void ensureSeeded() {
        if (seedChecked) return;
        seedChecked = true;
        databaseWriteExecutor.execute(() -> {
            if (instance.categoryDao().getAll().isEmpty()) {
                DataSeeder.seedCategories(instance.categoryDao());
                DataSeeder.seedIngredients(instance.ingredientDao());
            }
        });
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    private static class SeedCallback extends Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    }
}
