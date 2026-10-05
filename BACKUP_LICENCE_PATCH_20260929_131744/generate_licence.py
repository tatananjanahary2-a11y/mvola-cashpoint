#!/usr/bin/env python3

import secrets
import string
from datetime import datetime, timedelta

ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

def part(length=4):
    return "".join(secrets.choice(ALPHABET) for _ in range(length))

def generate_license():
    return "MVL-" + "-".join(part(4) for _ in range(3))

def main():
    licence = generate_license()

    today = datetime.now()
    expiration = today + timedelta(days=30)

    print()
    print("==========================================")
    print("       M'VOLA - CASHPOINT")
    print("       LICENCE GENERATOR")
    print("==========================================")
    print()
    print("Licence  :", licence)
    print("Créée le :", today.strftime("%Y-%m-%d"))
    print("Expire le:", expiration.strftime("%Y-%m-%d"))
    print("Durée    : 30 jours")
    print()
    print("==========================================")

if __name__ == "__main__":
    main()
