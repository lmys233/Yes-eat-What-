package com.lotlimys.yeseatwhat.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private AppPreferences appPrefs;
    private ShapeableImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvDescription;
    private String currentTheme = "";

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAvatar(uri);
                }
            });

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        String savedTheme = appPrefs.getThemeColor();
                        if (!savedTheme.equals(currentTheme)) {
                            requireActivity().recreate();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appPrefs = AppPreferences.getInstance(requireContext());
        currentTheme = appPrefs.getThemeColor();

        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvDescription = view.findViewById(R.id.tv_description);

        loadAvatar();
        loadProfile();

        // Long press avatar to change
        ivAvatar.setOnLongClickListener(v -> {
            imagePickerLauncher.launch("image/*");
            return true;
        });

        // Click avatar — hint to long press
        ivAvatar.setOnClickListener(v ->
                Toast.makeText(getContext(), "长按设置头像", Toast.LENGTH_SHORT).show());

        // Click to edit nickname
        tvNickname.setOnClickListener(v -> showEditDialog(
                "设置昵称", "请输入昵称", appPrefs.getNickname(),
                value -> {
                    appPrefs.setNickname(value);
                    tvNickname.setText(value.isEmpty() ? "点击设置昵称" : value);
                }));

        // Click to edit description
        tvDescription.setOnClickListener(v -> showEditDialog(
                "设置个人介绍", "请输入个人介绍", appPrefs.getSelfDescription(),
                value -> {
                    appPrefs.setSelfDescription(value);
                    tvDescription.setText(value.isEmpty() ? "添加个人介绍" : value);
                }));

        // Menu click listeners
        view.findViewById(R.id.ll_history).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_generation).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GenerationHistoryActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_favorites).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavoritesActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_portrait).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PortraitActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_settings).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            settingsLauncher.launch(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        loadAvatar();
    }

    // ===== Avatar =====

    private void loadAvatar() {
        File avatarFile = getAvatarFile();
        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            if (bitmap != null) {
                ivAvatar.setImageBitmap(bitmap);
                return;
            }
        }
        ivAvatar.setImageResource(R.drawable.ic_person);
    }

    private void saveAvatar(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return;

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap == null) return;

            // Crop to square (center)
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            int x = (bitmap.getWidth() - size) / 2;
            int y = (bitmap.getHeight() - size) / 2;
            Bitmap squared = Bitmap.createBitmap(bitmap, x, y, size, size);

            // Save to internal storage
            File dir = new File(requireContext().getFilesDir(), "avatar");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "avatar.jpg");

            FileOutputStream fos = new FileOutputStream(file);
            squared.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            // Reload
            loadAvatar();
            Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "头像更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    private File getAvatarFile() {
        return new File(requireContext().getFilesDir(), "avatar/avatar.jpg");
    }

    // ===== Profile =====

    private void loadProfile() {
        String nickname = appPrefs.getNickname();
        String description = appPrefs.getSelfDescription();

        tvNickname.setText(nickname.isEmpty() ? "点击设置昵称" : nickname);
        tvDescription.setText(description.isEmpty() ? "添加个人介绍" : description);
    }

    // ===== Edit Dialog =====

    private void showEditDialog(String title, String hint, String currentValue,
                                ValueCallback callback) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setText(currentValue);
        input.setSelection(currentValue.length());
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    callback.onValue(value);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @FunctionalInterface
    interface ValueCallback {
        void onValue(String value);
    }
}
