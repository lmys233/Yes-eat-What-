#!/bin/bash
# AI API 连接测试脚本
# 用法: bash test_api.sh <provider> <api_key>
# provider: qwen | minimax | deepseek
# 示例: bash test_api.sh qwen sk-your-key-here

set -e

PROVIDER=$1
API_KEY=$2

if [ -z "$PROVIDER" ] || [ -z "$API_KEY" ]; then
    echo "用法: bash test_api.sh <qwen|minimax|deepseek> <api_key>"
    echo "示例: bash test_api.sh qwen sk-your-key"
    exit 1
fi

case $PROVIDER in
    qwen)
        URL="https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
        MODEL="qwen-turbo"
        ;;
    minimax)
        URL="https://api.minimax.chat/v1/chat/completions"
        MODEL="MiniMax-Text-01"
        ;;
    deepseek)
        URL="https://api.deepseek.com/v1/chat/completions"
        MODEL="deepseek-chat"
        ;;
    *)
        echo "未知供应商: $PROVIDER (可选: qwen, minimax, deepseek)"
        exit 1
        ;;
esac

echo "================================================"
echo "测试供应商: $PROVIDER"
echo "API URL: $URL"
echo "模型: $MODEL"
echo "================================================"

# 发送简单测试请求
RESPONSE=$(curl -s -w "\n%{http_code}" "$URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_KEY" \
  -d '{
    "model": "'"$MODEL"'",
    "messages": [
      {"role": "system", "content": "你是一个助手。"},
      {"role": "user", "content": "你好吗？请简单回答。只用一句话。"}
    ]
  }' 2>&1)

# 分离 body 和 status code
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo ""
echo "HTTP 状态码: $HTTP_CODE"
echo ""
echo "原始响应:"
echo "$BODY" | head -c 500
echo ""

if [ "$HTTP_CODE" = "200" ]; then
    echo ""
    echo "✅ 请求成功！"
    echo ""
    echo "AI 回答:"
    echo "$BODY" | python3 -c "
import sys, json
resp = json.load(sys.stdin)
content = resp['choices'][0]['message']['content']
print(content)
" 2>/dev/null || echo "$BODY" | python -c "
import sys, json
resp = json.load(sys.stdin)
content = resp['choices'][0]['message']['content']
print(content)
" 2>/dev/null || echo "(无法解析响应，请查看上面的原始响应)"
else
    echo ""
    echo "❌ 请求失败"
fi
