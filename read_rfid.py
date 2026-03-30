#!/usr/bin/env python3
import RPi.GPIO as GPIO
from mfrc522 import SimpleMFRC522
import sys
import time  #


reader = SimpleMFRC522()


BUZZER_PIN = 12

try:

    GPIO.setup(BUZZER_PIN, GPIO.OUT)
    GPIO.output(BUZZER_PIN, GPIO.LOW)


    id, text = reader.read()


    GPIO.output(BUZZER_PIN, GPIO.HIGH)
    time.sleep(0.15)
    GPIO.output(BUZZER_PIN, GPIO.LOW)

    print(id)
    sys.stdout.flush()

except Exception as e:
    print("Error:", e)
finally:

    GPIO.cleanup()