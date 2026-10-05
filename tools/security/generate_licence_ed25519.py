#!/usr/bin/env python3

import sys
import subprocess
import secrets
from datetime import datetime, timedelta, timezone
from pathlib import Path

BASE = Path(__file__).resolve().parent
PRIVATE_KEY = BASE / "keys" / "licence_private.pem"

ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"


def make_nonce(length=12):
    return "".join(secrets.choice(ALPHABET) for _ in range(length))


def sign_message(message):
    import tempfile

    with tempfile.NamedTemporaryFile(delete=False) as f:
        f.write(message.encode())
        input_file = f.name

    try:
        result = subprocess.run(
            [
                "openssl",
                "pkeyutl",
                "-sign",
                "-inkey",
                str(PRIVATE_KEY),
                "-rawin",
                "-in",
                input_file,
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=True,
        )

        return result.stdout.hex().upper()

    finally:
        Path(input_file).unlink(missing_ok=True)


def generate_license(device_id, days=30):
    today = datetime.now(timezone.utc).date()
    expiration = today + timedelta(days=days)

    expiration_text = expiration.strftime("%Y%m%d")
    nonce = make_nonce()

    message = (
        f"MVL2|{device_id}|{expiration_text}|{nonce}"
    )

    signature = sign_message(message)

    return (
        f"MVL2-{expiration_text}-{nonce}-{signature}"
    )


def main():
    if len(sys.argv) < 2:
        print()
        print("UTILISATION :")
        print()
        print(
            "python tools/security/generate_licence_ed25519.py "
            "ID_APPAREIL [DUREE]"
        )
        print()
        print("Exemple :")
        print(
            "python tools/security/generate_licence_ed25519.py "
            "DEVICE123 30"
        )
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

    if not PRIVATE_KEY.exists():
        print("ERREUR : clé privée introuvable.")
        print(PRIVATE_KEY)
        return

    today = datetime.now(timezone.utc).date()
    expiration = today + timedelta(days=days)

    licence = generate_license(device_id, days)

    print()
    print("==========================================")
    print("       M'VOLA - CASHPOINT")
    print("       LICENCE ED25519")
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
