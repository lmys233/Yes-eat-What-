package com.lotlimys.yeseatwhat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.ui.home.HomeFragment;
import com.lotlimys.yeseatwhat.ui.order.OrderFragment;
import com.lotlimys.yeseatwhat.ui.profile.ProfileFragment;
import com.lotlimys.yeseatwhat.util.Constants;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this::onNavItemSelected);

        // Apply theme color to bottom nav
        applyThemeToBottomNav();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home");
        }
    }

    private void applyThemeToBottomNav() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        // Selected item uses primary color, default stays gray
        ColorStateList tint = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        primaryColor,
                        Color.parseColor("#FF9E9E9E")
                }
        );
        bottomNav.setItemIconTintList(tint);
        bottomNav.setItemTextColor(tint);
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            loadFragment(new HomeFragment(), "home");
            return true;
        } else if (id == R.id.nav_order) {
            loadFragment(new OrderFragment(), "order");
            return true;
        } else if (id == R.id.nav_profile) {
            loadFragment(new ProfileFragment(), "profile");
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment, tag);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (current instanceof HomeFragment) {
            finishAffinity();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
