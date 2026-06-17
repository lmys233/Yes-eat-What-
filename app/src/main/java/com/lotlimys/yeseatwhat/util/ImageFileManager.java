package com.lotlimys.yeseatwhat.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 管理菜品图片的本地文件存储。
 * 图片保存到 app 内部存储 files/recipe_images/ 目录，以 recipeKey 命名。
 * 访问图片时优先返回本地路径，不存在则返回 null（由调用方兜底占位图）。
 */
public class ImageFileManager {

    private static final String TAG = "ImageFileManager";
    private static final String IMAGE_DIR = "recipe_images";

    private final File imageDir;
    private final OkHttpClient client;

    public ImageFileManager(Context context) {
        this.imageDir = new File(context.getFilesDir(), IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    /**
     * 根据 recipeKey 获取本地图片文件路径。
     *
     * @param recipeKey 菜品唯一标识
     * @return 本地图片文件路径，不存在返回 null
     */
    public String getLocalPath(String recipeKey) {
        if (recipeKey == null) return null;
        File file = new File(imageDir, sanitizeFileName(recipeKey) + ".png");
        return file.exists() ? file.getAbsolutePath() : null;
    }

    /**
     * 从 URL 下载图片并保存到本地。
     *
     * @param imageUrl  图片 URL
     * @param recipeKey 菜品唯一标识
     * @return 本地文件绝对路径，失败返回 null
     */
    public String saveImageFromUrl(String imageUrl, String recipeKey) {
        if (imageUrl == null || imageUrl.isEmpty() || recipeKey == null) return null;

        // 已经是本地路径则不重复保存
        if (imageUrl.startsWith("/") || imageUrl.startsWith("file://")) {
            return imageUrl;
        }

        File targetFile = new File(imageDir, sanitizeFileName(recipeKey) + ".png");

        try {
            Log.d(TAG, "下载图片: " + imageUrl + " -> " + targetFile.getAbsolutePath());

            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();

            try (Response response = client.newCall(request).execute();
                 InputStream input = response.body() != null ? response.body().byteStream() : null;
                 FileOutputStream output = new FileOutputStream(targetFile)) {

                if (!response.isSuccessful()) {
                    throw new IOException("下载失败: HTTP " + response.code());
                }

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
            }

            Log.d(TAG, "图片已保存到本地: " + targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "保存图片失败: " + recipeKey + " - " + e.getMessage(), e);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            return null;
        }
    }

    /**
     * 删除某菜品的本地图片。
     */
    public boolean deleteImage(String recipeKey) {
        if (recipeKey == null) return false;
        File file = new File(imageDir, sanitizeFileName(recipeKey) + ".png");
        return file.exists() && file.delete();
    }

    /**
     * 清理 recipeKey 中的非法文件名字符。
     * 仅在单层目录中使用，不包含路径分隔符所以无路径穿越风险。
     */
    private String sanitizeFileName(String recipeKey) {
        return recipeKey.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }
}
