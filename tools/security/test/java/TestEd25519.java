import com.hermogenio.cashpoint.security.Ed25519LicenceVerifier;

public class TestEd25519 {

    public static void main(String[] args) {

        String deviceId = "DEVICE123";
        String expiration = "20261029";
        String nonce = "LL7QWDNVFER8";

        String signature =
                "60D5D001B746B80DD3AA53C07E38F06BC549859CA03DBD4E123DB1F4A9CCF2603E80B3183681A963A668C41689E1AADD9AC9D00B957CB393C65DBD4DED71CE0F";

        boolean valid =
                Ed25519LicenceVerifier.verify(
                        deviceId,
                        expiration,
                        nonce,
                        signature
                );

        System.out.println(
                "ORIGINAL_LICENCE = " + valid
        );

        boolean fake =
                Ed25519LicenceVerifier.verify(
                        deviceId,
                        expiration,
                        "FAKE12345",
                        signature
                );

        System.out.println(
                "FAKE_LICENCE = " + fake
        );

        if (!valid) {
            throw new RuntimeException(
                    "TEST FAILED: licence originale nolavina"
            );
        }

        if (fake) {
            throw new RuntimeException(
                    "TEST FAILED: licence fake nekena"
            );
        }

        System.out.println(
                "ED25519_JAVA_TEST_OK"
        );
    }
}
