import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ingredient icon batch generator.
 * Usage: java IngredientImageGenerator --api-key=KEY [--output=DIR] [--api=dashscope|openai]
 */
public class IngredientImageGenerator {

    // 80 ingredients matching DataSeeder (id=1..80)
    private static final String[][] INGREDIENTS = {
            {"猪五花肉"}, {"猪里脊"}, {"猪排骨"}, {"鸡胸肉"}, {"鸡腿肉"},
            {"鸡翅"}, {"牛腩"}, {"牛里脊"}, {"羊肉"}, {"鸭肉"},
            {"白菜"}, {"菠菜"}, {"西兰花"}, {"菜花"}, {"生菜"},
            {"西红柿"}, {"黄瓜"}, {"土豆"}, {"胡萝卜"}, {"洋葱"},
            {"青椒"}, {"茄子"}, {"玉米"}, {"蘑菇"}, {"蒜苗"}, {"豆角"},
            {"苹果"}, {"香蕉"}, {"橙子"}, {"柠檬"}, {"草莓"},
            {"葡萄"}, {"芒果"}, {"菠萝"}, {"西瓜"}, {"蓝莓"},
            {"虾"}, {"鱼"}, {"螃蟹"}, {"蛤蜊"}, {"鱿鱼"}, {"扇贝"},
            {"酱油"}, {"醋"}, {"料酒"}, {"食用油"}, {"盐"},
            {"糖"}, {"生抽"}, {"老抽"}, {"蚝油"}, {"豆瓣酱"},
            {"辣椒"}, {"花椒"}, {"姜"}, {"蒜"}, {"葱"},
            {"大米"}, {"面条"}, {"面粉"}, {"饺子皮"}, {"糯米"}, {"馒头"}, {"小米"},
            {"豆腐"}, {"鸡蛋"}, {"牛奶"}, {"豆浆"}, {"酸奶"}, {"芝士"}, {"腐竹"},
            {"花生"}, {"核桃"}, {"红枣"}, {"枸杞"}, {"木耳"}, {"香菇"}, {"紫菜"}, {"海带"}, {"桂圆"}
    };

    private static final String DASHSCOPE_API_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";
    private static final String DASHSCOPE_QUERY_URL =
            "https://dashscope.aliyuncs.com/api/v1/tasks";
    private static final String IMAGE_MODEL = "qwen-image-plus";

    private String apiKey;
    private Path outputDir;
    private boolean useOpenAI;
    private String openaiBaseUrl;
    private int successCount;
    private int failCount;

    public IngredientImageGenerator(String apiKey, Path outputDir, boolean useOpenAI, String openaiBaseUrl) {
        this.apiKey = apiKey;
        this.outputDir = outputDir;
        this.useOpenAI = useOpenAI;
        this.openaiBaseUrl = openaiBaseUrl;
    }

    public static void main(String[] args) {
        String apiKey = null;
        String outputPath = "./output";
        boolean useOpenAI = false;
        String openaiBaseUrl = "https://api.openai.com/v1";

        for (String arg : args) {
            if (arg.startsWith("--api-key=")) {
                apiKey = arg.substring("--api-key=".length());
            } else if (arg.startsWith("--output=")) {
                outputPath = arg.substring("--output=".length());
            } else if (arg.equals("--api=openai")) {
                useOpenAI = true;
            } else if (arg.startsWith("--openai-url=")) {
                openaiBaseUrl = arg.substring("--openai-url=".length());
            } else if (arg.equals("--api=dashscope")) {
                useOpenAI = false;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            }
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: --api-key is required");
            printHelp();
            System.exit(1);
        }

        Path outputDir = Paths.get(outputPath);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("Error: cannot create output directory: " + outputPath);
            System.exit(1);
        }

        String providerName = useOpenAI ? "OpenAI" : "DashScope";
        System.out.println("============================================");
        System.out.println("  Ingredient Image Generator");
        System.out.println("  Provider: " + providerName);
        System.out.println("  Output:   " + outputDir.toAbsolutePath());
        System.out.println("  Count:    " + INGREDIENTS.length);
        System.out.println("  Start:    " + now());
        System.out.println("============================================");

        IngredientImageGenerator gen =
                new IngredientImageGenerator(apiKey, outputDir, useOpenAI, openaiBaseUrl);
        gen.run();

        System.out.println();
        System.out.println("============================================");
        System.out.println("  Done! Success: " + gen.successCount + " / Failed: " + gen.failCount);
        System.out.println("  End: " + now());
        System.out.println("============================================");
        System.out.println();
        System.out.println("Copy images to Android project:");
        System.out.println("  cp " + outputDir.toAbsolutePath() + "/*.png <project>/app/src/main/assets/ingredient_images/");
    }

    private void run() {
        for (int i = 0; i < INGREDIENTS.length; i++) {
            String name = INGREDIENTS[i][0];
            int id = i + 1;
            String filename = "ingredient_" + id + ".png";
            Path filePath = outputDir.resolve(filename);

            if (Files.exists(filePath) && filePath.toFile().length() > 0) {
                System.out.println("[" + id + "/" + INGREDIENTS.length + "] SKIP (exists): " + name);
                successCount++;
                continue;
            }

            System.out.print("[" + id + "/" + INGREDIENTS.length + "] " + name + " ... ");
            System.out.flush();

            try {
                String imageUrl = generateImage(name);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    downloadImage(imageUrl, filePath);
                    System.out.println("OK -> " + filename);
                    successCount++;
                } else {
                    System.out.println("FAIL (empty URL)");
                    failCount++;
                }
            } catch (Exception e) {
                System.out.println("FAIL: " + e.getMessage());
                failCount++;
            }

            if (i < INGREDIENTS.length - 1) {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private String generateImage(String ingredientName) throws Exception {
        String prompt = buildPrompt(ingredientName);
        if (useOpenAI) {
            return generateWithOpenAI(prompt);
        } else {
            return generateWithDashScope(prompt);
        }
    }

    private String buildPrompt(String name) {
        return "生成像素风格" + name + "的单一图标\n"
                + "不要高清逼真、拉高像素颗粒感\n"
                + "大幅降低分辨率、减少总像素点数量\n"
                + "背景：纯白色背景\n"
                + "画风要求：泰拉瑞亚物品风格（复古低像素、游戏道具风、极简方块像素、不写实）\n"
                + "单个物品图标居中展示，不要多余的装饰文字或图案";
    }

    // ===== DashScope =====

    private String generateWithDashScope(String prompt) throws Exception {
        String taskId = createDashScopeTask(prompt);
        return pollDashScopeResult(taskId);
    }

    private String createDashScopeTask(String prompt) throws Exception {
        String jsonBody = "{"
                + "\"model\":\"" + IMAGE_MODEL + "\","
                + "\"input\":{\"prompt\":\"" + escapeJson(prompt) + "\"},"
                + "\"parameters\":{\"size\":\"1024*1024\",\"n\":1,\"prompt_extend\":false,\"watermark\":false}"
                + "}";

        HttpURLConnection conn = createConnection(new URL(DASHSCOPE_API_URL), "POST");
        conn.setRequestProperty("X-DashScope-Async", "enable");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        sendBody(conn, jsonBody);

        int code = conn.getResponseCode();
        if (code == 429) throw new IOException("429 rate limited");
        if (code != 200) throw new IOException("HTTP " + code + ": " + readError(conn));

        String resp = readBody(conn);
        conn.disconnect();
        return extractJsonString(resp, "task_id");
    }

    private String pollDashScopeResult(String taskId) throws Exception {
        String url = DASHSCOPE_QUERY_URL + "/" + taskId;
        for (int i = 0; i < 60; i++) {
            HttpURLConnection conn = createConnection(new URL(url), "GET");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            int code = conn.getResponseCode();
            if (code != 200) { conn.disconnect(); throw new IOException("poll HTTP " + code); }

            String resp = readBody(conn);
            conn.disconnect();

            String status = extractJsonString(resp, "task_status");
            if ("SUCCEEDED".equals(status)) {
                String imgUrl = extractImageUrl(resp);
                if (imgUrl != null) return imgUrl;
                throw new IOException("no image URL in result");
            } else if ("FAILED".equals(status)) {
                throw new IOException("generation failed: " + extractJsonString(resp, "message"));
            } else if ("CANCELED".equals(status)) {
                throw new IOException("task cancelled");
            }
            Thread.sleep(2000);
        }
        throw new IOException("timeout (2 min)");
    }

    // ===== OpenAI =====

    private String generateWithOpenAI(String prompt) throws Exception {
        String apiUrl = openaiBaseUrl;
        if (!apiUrl.endsWith("/")) apiUrl += "/";
        apiUrl += "images/generations";

        String jsonBody = "{"
                + "\"model\":\"dall-e-3\","
                + "\"prompt\":\"" + escapeJson(prompt) + "\","
                + "\"n\":1,"
                + "\"size\":\"1024x1024\""
                + "}";

        HttpURLConnection conn = createConnection(new URL(apiUrl), "POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        sendBody(conn, jsonBody);

        int code = conn.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code + ": " + readError(conn));

        String resp = readBody(conn);
        conn.disconnect();

        String marker = "\"url\":\"";
        int start = resp.indexOf(marker);
        if (start == -1) throw new IOException("no url in response");
        start += marker.length();
        int end = resp.indexOf("\"", start);
        return resp.substring(start, end).replace("\\/", "/");
    }

    // ===== Helpers =====

    private void downloadImage(String imageUrl, Path target) throws Exception {
        HttpURLConnection conn = createConnection(new URL(imageUrl), "GET");
        conn.setInstanceFollowRedirects(true);
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection createConnection(URL url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.setDoInput(true);
        if ("POST".equals(method)) conn.setDoOutput(true);
        return conn;
    }

    private void sendBody(HttpURLConnection conn, String body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    private String readBody(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String readError(HttpURLConnection conn) {
        try { return readBody(conn); } catch (Exception e) { return ""; }
    }

    private String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    private String extractImageUrl(String json) {
        String marker = "\"url\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return null;
        start += marker.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end).replace("\\/", "/");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static void printHelp() {
        System.out.println("Usage: java IngredientImageGenerator [options]");
        System.out.println();
        System.out.println("Required:");
        System.out.println("  --api-key=KEY          AI API key");
        System.out.println();
        System.out.println("Optional:");
        System.out.println("  --output=DIR           Output directory (default: ./output)");
        System.out.println("  --api=dashscope        Use Aliyun DashScope (default)");
        System.out.println("  --api=openai           Use OpenAI DALL-E 3");
        System.out.println("  --openai-url=URL       OpenAI-compatible API URL");
        System.out.println("  --help, -h             Show this help");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java IngredientImageGenerator --api-key=sk-xxx");
        System.out.println("  java IngredientImageGenerator --api-key=sk-xxx --output=./icons");
    }
}
