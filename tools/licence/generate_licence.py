#!/usr/bin/env python3

import sys
import secrets
import hmac
import hashlib
from datetime import datetime, timedelta, timezone

SECRET = "MVOLA-CASHPOINT-LICENCE-V1-CHANGE-ME"

ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

def make_nonce(length=8):
    return "".join(secrets.choice(ALPHABET) for _ in range(length))

def signature(device_id, expiration, nonce):
    message = f"{device_id}|{expiration}|{nonce}".encode()
    key = SECRET.encode()

    digest = hmac.new(
        key,
        message,
        hashlib.sha256
    ).hexdigest().upper()

    return digest[:16]

def generate_license(device_id, days=30):
    today = datetime.now(timezone.utc).date()
    expiration = today + timedelta(days=days)

    expiration_text = expiration.strftime("%Y%m%d")
    nonce = make_nonce()

    sig = signature(
        device_id,
        expiration_text,
        nonce
    )

    return (
        f"MVL-{expiration_text}-{nonce}-{sig}"
    )

def main():

    if len(sys.argv) < 2:
        print()
        print("UTILISATION :")
        print()
        print("python tools/licence/generate_licence.py ID_APPAREIL [DUREE]")
        print()
        print("Exemple :")
        print("python tools/licence/generate_licence.py abc123 30")
        print()
        return

    device_id = sys.argv[1].strip()

    if not device_id:
        print("ERREUR : ID appareil vide.")
        return

    days = 30

    if len(sys.argv) >= 3:
        try:
            days = int(sys.argv[2])
        except ValueError:
            print("ERREUR : durée invalide.")
            return

    if days <= 0:
        print("ERREUR : durée doit être supérieure à 0.")
        return

    licence = generate_license(
        device_id,
        days
    )

    today = datetime.now(timezone.utc).date()
    expiration = today + timedelta(days=days)

    print()
    print("==========================================")
    print("       M'VOLA - CASHPOINT")
    print("       LICENCE UNIQUE")
    print("==========================================")
    print()
    print("ID appareil :", device_id)
    print("Licence     :", licence)
    print("Créée le    :", today.strftime("%Y-%m-%d"))
    print("Expire le   :", expiration.strftime("%Y-%m-%d"))
    print("Durée       :", days, "jours")
    print()
    print("==========================================")

if __name__ == "__main__":
    main()
