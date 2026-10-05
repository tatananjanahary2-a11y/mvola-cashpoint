package com.hermogenio.cashpoint.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class Ed25519LicenceVerifier {

    private Ed25519LicenceVerifier() {
    }

    /*
     * Public key Ed25519.
     * Tsy tokony hisy private key eto.
     */
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

            byte[] publicKeyBytes =
                    Base64.getDecoder().decode(
                            PUBLIC_KEY_BASE64
                    );

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(
                            publicKeyBytes
                    );

            KeyFactory keyFactory =
                    KeyFactory.getInstance(
                            "Ed25519"
                    );

            PublicKey publicKey =
                    keyFactory.generatePublic(
                            keySpec
                    );

            byte[] signatureBytes =
                    hexToBytes(signatureHex);

            Signature verifier =
                    Signature.getInstance(
                            "Ed25519"
                    );

            verifier.initVerify(
                    publicKey
            );

            verifier.update(
                    message.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return verifier.verify(
                    signatureBytes
            );

        } catch (Exception e) {

            return false;
        }
    }

    private static byte[] hexToBytes(
            String hex
    ) {

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
                            hex.substring(
                                    i,
                                    i + 2
                            ),
                            16
                    );
        }

        return result;
    }
}
