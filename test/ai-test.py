print("脚本开始运行了！")
import requests

# 这里换成你的 DeepSeek API Key
API_KEY = "sk-838ca294a7644336961604339f57c63a"
url = "https://api.deepseek.com/v1/chat/completions"

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

data = {
    "model": "deepseek-chat",
    "messages": [{"role": "user", "content": "你好"}],
    "temperature": 0.7
}

try:
    response = requests.post(url, headers=headers, json=data, timeout=15)
    print("状态码:", response.status_code)
    print("返回内容:", response.json())
except Exception as e:
    print("出错了:", e)