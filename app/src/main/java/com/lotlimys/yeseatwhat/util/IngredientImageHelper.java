package com.lotlimys.yeseatwhat.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 管理食材按钮图片的本地文件存储。
 * 图片保存到 app 内部存储 files/ingredient_images/ 目录，以 UUID 为文件名。
 */
public class IngredientImageHelper {

    private static final String TAG = "IngredientImageHelper";
    private static final String IMAGE_DIR = "ingredient_images";

    private final File imageDir;
    private final OkHttpClient client;

    public IngredientImageHelper(Context context) {
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
     * 从 URL 下载食材图片并保存到本地，使用 UUID 作为文件名避免重名冲突。
     *
     * @param imageUrl        图片 URL
     * @return 本地文件绝对路径，失败返回 null
     */
    public String saveImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;

        if (imageUrl.startsWith("/") || imageUrl.startsWith("file://")) {
            return imageUrl;
        }

        File targetFile = new File(imageDir, UUID.randomUUID().toString() + ".png");

        try {
            Log.d(TAG, "下载食材图片 <- " + imageUrl);

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

            Log.d(TAG, "食材图片已保存: " + targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "保存食材图片失败 - " + e.getMessage(), e);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            return null;
        }
    }
}
