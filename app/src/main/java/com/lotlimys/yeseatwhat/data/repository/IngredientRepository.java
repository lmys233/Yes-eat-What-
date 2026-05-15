package com.lotlimys.yeseatwhat.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.dao.CategoryDao;
import com.lotlimys.yeseatwhat.data.db.dao.IngredientDao;
import com.lotlimys.yeseatwhat.data.db.entity.Category;
import com.lotlimys.yeseatwhat.data.db.entity.CustomIngredient;
import com.lotlimys.yeseatwhat.data.db.entity.SystemIngredient;
import com.lotlimys.yeseatwhat.model.CategoryItem;
import com.lotlimys.yeseatwhat.model.IngredientItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class IngredientRepository {
    private final CategoryDao categoryDao;
    private final IngredientDao ingredientDao;
    private final ExecutorService executor;

    public IngredientRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.categoryDao = db.categoryDao();
        this.ingredientDao = db.ingredientDao();
        this.executor = AppDatabase.getDatabaseWriteExecutor();
    }

    public LiveData<List<CategoryItem>> getCategories() {
        MutableLiveData<List<CategoryItem>> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAll();
            List<CategoryItem> items = new ArrayList<>();
            for (Category c : categories) {
                items.add(new CategoryItem(c.getId(), c.getName(), c.getIcon(), c.getType()));
            }
            result.postValue(items);
        });
        return result;
    }

    public LiveData<List<IngredientItem>> getIngredients() {
        MutableLiveData<List<IngredientItem>> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<IngredientItem> items = new ArrayList<>();
            List<Category> categories = categoryDao.getAll();
            for (Category category : categories) {
                List<SystemIngredient> systemIngredients = ingredientDao.getSystemByCategory(category.getId());
                for (SystemIngredient si : systemIngredients) {
                    items.add(new IngredientItem(si.getId(), si.getName(), si.getIcon(),
                            si.getCategoryId(), category.getName(), false, false));
                }
                List<CustomIngredient> customIngredients = ingredientDao.getCustomByCategory(category.getId());
                for (CustomIngredient ci : customIngredients) {
                    items.add(new IngredientItem(ci.getId(), ci.getName(), ci.getIcon(),
                            ci.getCategoryId(), category.getName(), false, true));
                }
            }
            result.postValue(items);
        });
        return result;
    }

    public void addCategory(Category category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void addIngredient(CustomIngredient ingredient) {
        executor.execute(() -> ingredientDao.insertCustom(ingredient));
    }

    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDao.delete(category));
    }

    public void deleteIngredient(CustomIngredient ingredient) {
        executor.execute(() -> ingredientDao.deleteCustom(ingredient));
    }
}
