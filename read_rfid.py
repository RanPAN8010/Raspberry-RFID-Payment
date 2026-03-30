#!/usr/bin/env python3
import RPi.GPIO as GPIO
from mfrc522 import SimpleMFRC522
import sys
import subprocess  #

reader = SimpleMFRC522()

try:
    id, text = reader.read()


    try:

        subprocess.run(["/home/pi/script/buzzer.sh", "1"])
    except Exception as e:

        print("⚠️ ", e)

    # 打印卡号，让 Java 拦截
    print(id)
    sys.stdout.flush()

except Exception as e:
    print("Error:", e)
finally:
    GPIO.cleanup()