#!/usr/bin/env python3
import RPi.GPIO as GPIO
from mfrc522 import SimpleMFRC522
import sys

# 初始化读卡器
reader = SimpleMFRC522()

try:
    # 阻塞程序，直到有卡片靠近
    id, text = reader.read()
    
    # 打印读取到的纯数字 ID（Java 代码会抓取这一行打印的内容）
    print(id)
    sys.stdout.flush()  # 刷新缓冲区，确保 Java 能立刻读到
    
except Exception as e:
    print("Error:", e)
finally:
    # 释放引脚资源，这步非常重要，否则下次运行会报错
    GPIO.cleanup()