package com.hermogenio.cashpoint.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

public final class Ed25519LicenceVerifier {

    private Ed25519LicenceVerifier() {
    }

    private static final String PUBLIC_KEY_BASE64 =
            "MCowBQYDK2VwAyEA+QdYpmV0BxFzOeOHL1SPTsvooUKuvNQsQKp5tUEnFXk=";

    public static boolean verify(
            String deviceId,
            String expiration,
            String nonce,
            String signatureHex
    ) {
        try {
            String message =
                    "MVL2|" +
                    deviceId +
                    "|" +
                    expiration +
                    "|" +
                    nonce;

            byte[] encodedPublicKey =
                    Base64.getDecoder().decode(
                            PUBLIC_KEY_BASE64
                    );

            /*
             * X.509 Ed25519 public key:
             * 12-byte prefix + 32-byte raw public key.
             */
            if (encodedPublicKey.length != 44) {
                return false;
            }

            byte[] rawPublicKey = new byte[32];

            System.arraycopy(
                    encodedPublicKey,
                    12,
                    rawPublicKey,
                    0,
                    32
            );

            byte[] signature =
                    hexToBytes(signatureHex);

            if (signature.length != 64) {
                return false;
            }

            Ed25519PublicKeyParameters publicKey =
                    new Ed25519PublicKeyParameters(
                            rawPublicKey,
                            0
                    );

            Ed25519Signer verifier =
                    new Ed25519Signer();

            verifier.init(false, publicKey);

            byte[] messageBytes =
                    message.getBytes(
                            StandardCharsets.UTF_8
                    );

            verifier.update(
                    messageBytes,
                    0,
                    messageBytes.length
            );

            return verifier.verifySignature(signature);

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null ||
                hex.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal"
            );
        }

        byte[] result =
                new byte[hex.length() / 2];

        for (int i = 0;
             i < hex.length();
             i += 2) {

            result[i / 2] =
                    (byte) Integer.parseInt(
                            hex.substring(i, i + 2),
                            16
                    );
        }

        return result;
    }
}
