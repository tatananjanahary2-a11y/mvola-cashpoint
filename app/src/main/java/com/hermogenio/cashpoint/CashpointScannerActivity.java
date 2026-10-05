package com.hermogenio.cashpoint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CashpointScannerActivity extends Activity {

    private static final int REQ_CAMERA = 8801;

    private TextureView textureView;
    private TextView statusText;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;

    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private TextRecognizer recognizer;

    private String cameraId;
    private int sensorOrientation;

    private Size cameraSize;

    private final AtomicBoolean processing =
            new AtomicBoolean(false);

    private volatile boolean numeroTrouve = false;

    /*
     * Mampiasa pass maromaro:
     *
     * 1 = OCR normal
     * 2 = OCR image agrandie
     * 3 = OCR contraste
     * 4 = OCR noir/blanc
     */
    private int passOCR = 0;

    private final TextureView.SurfaceTextureListener
            textureListener =
            new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(
                SurfaceTexture surface,
                int width,
                int height) {

            ouvrirCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(
                SurfaceTexture surface,
                int width,
                int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(
                SurfaceTexture surface) {

            fermerCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(
                SurfaceTexture surface) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        construireInterface();

        recognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );

        demarrerThread();

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    REQ_CAMERA
            );

        } else {

            preparerCamera();
        }
    }

    private void construireInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(15, 23, 42)
        );

        TextView titre =
                new TextView(this);

        titre.setText(
                "📷 SCAN NUMÉRO"
        );

        titre.setTextColor(
                Color.WHITE
        );

        titre.setTextSize(21);

        titre.setGravity(
                Gravity.CENTER
        );

        titre.setPadding(
                16,
                22,
                16,
                8
        );

        root.addView(
                titre
        );

        statusText =
                new TextView(this);

        statusText.setText(
                "Placez le numéro devant la caméra..."
        );

        statusText.setTextColor(
                Color.WHITE
        );

        statusText.setTextSize(15);

        statusText.setGravity(
                Gravity.CENTER
        );

        statusText.setPadding(
                12,
                8,
                12,
                15
        );

        root.addView(
                statusText
        );

        textureView =
                new TextureView(this);

        textureView.setSurfaceTextureListener(
                textureListener
        );

        LinearLayout.LayoutParams cameraParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1f
                );

        root.addView(
                textureView,
                cameraParams
        );

        TextView aide =
                new TextView(this);

        aide.setText(
                "🔢 Numérique + ✍️ manuscrit\n" +
                "034 • 032 • 033 • 038"
        );

        aide.setTextColor(
                Color.WHITE
        );

        aide.setTextSize(14);

        aide.setGravity(
                Gravity.CENTER
        );

        aide.setPadding(
                10,
                14,
                10,
                22
        );

        root.addView(
                aide
        );

        setContentView(root);
    }

    private void demarrerThread() {

        cameraThread =
                new HandlerThread(
                        "CASHPOINT_OCR_V6"
                );

        cameraThread.start();

        cameraHandler =
                new Handler(
                        cameraThread.getLooper()
                );
    }

    private void preparerCamera() {

        try {

            cameraManager =
                    (CameraManager)
                            getSystemService(
                                    Context.CAMERA_SERVICE
                            );

            if (cameraManager == null) {

                erreur(
                        "Caméra indisponible."
                );

                return;
            }

            String[] ids =
                    cameraManager.getCameraIdList();

            for (String id : ids) {

                CameraCharacteristics c =
                        cameraManager.getCameraCharacteristics(
                                id
                        );

                Integer facing =
                        c.get(
                                CameraCharacteristics.LENS_FACING
                        );

                if (facing != null &&
                        facing ==
                                CameraCharacteristics.LENS_FACING_BACK) {

                    cameraId = id;

                    Integer orientation =
                            c.get(
                                    CameraCharacteristics
                                            .SENSOR_ORIENTATION
                            );

                    sensorOrientation =
                            orientation == null
                                    ? 90
                                    : orientation;

                    choisirTaille(c);

                    break;
                }
            }

            if (cameraId == null) {

                erreur(
                        "Caméra arrière introuvable."
                );

                return;
            }

            if (textureView.isAvailable()) {
                ouvrirCamera();
            }

        } catch (Exception e) {

            erreur(
                    "Erreur caméra."
            );
        }
    }

    private void choisirTaille(
            CameraCharacteristics c) {

        try {

            android.hardware.camera2.params.StreamConfigurationMap map =
                    c.get(
                            CameraCharacteristics
                                    .SCALER_STREAM_CONFIGURATION_MAP
                    );

            if (map == null) {
                return;
            }

            Size[] sizes =
                    map.getOutputSizes(
                            ImageFormat.YUV_420_888
                    );

            if (sizes == null ||
                    sizes.length == 0) {
                return;
            }

            List<Size> list =
                    new ArrayList<>(
                            Arrays.asList(sizes)
                    );

            Collections.sort(
                    list,
                    new Comparator<Size>() {

                        @Override
                        public int compare(
                                Size a,
                                Size b) {

                            long da =
                                    Math.abs(
                                            (long) a.getWidth()
                                            * a.getHeight()
                                            - 1280L * 720L
                                    );

                            long db =
                                    Math.abs(
                                            (long) b.getWidth()
                                            * b.getHeight()
                                            - 1280L * 720L
                                    );

                            return Long.compare(
                                    da,
                                    db
                            );
                        }
                    }
            );

            cameraSize =
                    list.get(0);

        } catch (Exception ignored) {
        }
    }

    private void ouvrirCamera() {

        if (cameraId == null) {
            preparerCamera();
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {

            cameraManager.openCamera(
                    cameraId,
                    new CameraDevice.StateCallback() {

                        @Override
                        public void onOpened(
                                @NonNull CameraDevice camera) {

                            cameraDevice = camera;

                            status(
                                    "🟢 Camera active — recherche..."
                            );

                            demarrerPreview();
                        }

                        @Override
                        public void onDisconnected(
                                @NonNull CameraDevice camera) {

                            camera.close();

                            if (cameraDevice == camera) {
                                cameraDevice = null;
                            }
                        }

                        @Override
                        public void onError(
                                @NonNull CameraDevice camera,
                                int error) {

                            camera.close();

                            if (cameraDevice == camera) {
                                cameraDevice = null;
                            }

                            erreur(
                                    "Erreur caméra."
                            );
                        }
                    },
                    cameraHandler
            );

        } catch (Exception e) {

            erreur(
                    "Impossible d'ouvrir la caméra."
            );
        }
    }

    private void demarrerPreview() {

        if (cameraDevice == null ||
                !textureView.isAvailable()) {
            return;
        }

        try {

            SurfaceTexture texture =
                    textureView.getSurfaceTexture();

            if (texture == null) {
                return;
            }

            int width =
                    cameraSize == null
                            ? 1280
                            : cameraSize.getWidth();

            int height =
                    cameraSize == null
                            ? 720
                            : cameraSize.getHeight();

            texture.setDefaultBufferSize(
                    width,
                    height
            );

            Surface preview =
                    new Surface(texture);

            imageReader =
                    ImageReader.newInstance(
                            width,
                            height,
                            ImageFormat.YUV_420_888,
                            2
                    );

            imageReader.setOnImageAvailableListener(
                    reader -> {

                        Image image = null;

                        try {

                            image =
                                    reader.acquireLatestImage();

                            if (image == null) {
                                return;
                            }

                            if (numeroTrouve) {
                                return;
                            }

                            if (!processing.compareAndSet(
                                    false,
                                    true
                            )) {
                                return;
                            }

                            analyserImage(
                                    image
                            );

                            image = null;

                        } catch (Exception e) {

                            processing.set(false);

                        } finally {

                            if (image != null) {
                                image.close();
                            }
                        }

                    },
                    cameraHandler
            );

            CaptureRequest.Builder request =
                    cameraDevice.createCaptureRequest(
                            CameraDevice.TEMPLATE_PREVIEW
                    );

            request.addTarget(
                    preview
            );

            request.addTarget(
                    imageReader.getSurface()
            );

            request.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest
                            .CONTROL_AF_MODE_CONTINUOUS_PICTURE
            );

            cameraDevice.createCaptureSession(
                    Arrays.asList(
                            preview,
                            imageReader.getSurface()
                    ),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(
                                @NonNull CameraCaptureSession session) {

                            captureSession =
                                    session;

                            try {

                                session.setRepeatingRequest(
                                        request.build(),
                                        null,
                                        cameraHandler
                                );

                            } catch (Exception e) {

                                erreur(
                                        "Preview impossible."
                                );
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession session) {

                            erreur(
                                    "Configuration caméra échouée."
                            );
                        }
                    },
                    cameraHandler
            );

        } catch (Exception e) {

            erreur(
                    "Erreur démarrage OCR."
            );
        }
    }

    private void analyserImage(
            Image image) {

        int rotation =
                calculerRotation();

        InputImage input;

        try {

            input =
                    InputImage.fromMediaImage(
                            image,
                            rotation
                    );

        } catch (Exception e) {

            image.close();
            processing.set(false);
            return;
        }

        recognizer
                .process(input)
                .addOnSuccessListener(
                        result -> {

                            String texte =
                                    result == null
                                            ? ""
                                            : result.getText();

                            String numero =
                                    extraireNumero(
                                            texte
                                    );

                            if (numero != null) {

                                numeroTrouve = true;

                                image.close();
                                processing.set(false);

                                retournerNumero(
                                        numero
                                );

                                return;
                            }

                            /*
                             * Raha tsy hitan'ny OCR normal,
                             * ampitomboina ny frame manaraka.
                             */
                            passOCR++;

                            if (passOCR % 8 == 0) {

                                status(
                                        "✍️ Recherche manuscrit..."
                                );
                            } else {

                                status(
                                        "🔎 Recherche numéro..."
                                );
                            }

                            image.close();
                            processing.set(false);
                        }
                )
                .addOnFailureListener(
                        e -> {

                            image.close();
                            processing.set(false);
                        }
                );
    }

    /*
     * Rotation Camera2 -> ML Kit
     */
    private int calculerRotation() {

        int rotation =
                getWindowManager()
                        .getDefaultDisplay()
                        .getRotation();

        int degrees;

        switch (rotation) {

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;

            default:
                degrees = 0;
        }

        return (
                sensorOrientation
                        - degrees
                        + 360
        ) % 360;
    }

    /*
     * NUMÉRIQUE + MANUSCRIT
     */
    private String extraireNumero(
            String texte) {

        if (texte == null) {
            return null;
        }

        /*
         * Nettoyage OCR:
         *
         * O/o -> 0
         * I/l -> 1
         *
         * Ampiasaina ihany raha
         * manodidina ny chiffre.
         */
        String normalise =
                texte
                        .replace('O', '0')
                        .replace('o', '0')
                        .replace('I', '1')
                        .replace('l', '1');

        /*
         * 1. Numéro standard
         */
        String numero =
                chercherNumero(normalise);

        if (numero != null) {
            return numero;
        }

        /*
         * 2. OCR manuscrit:
         *
         * Ohatra:
         *
         * 034 12 34 567
         * 034-12-34-567
         * 034.12.34.567
         *
         * Esorina ny séparateurs.
         */
        String compact =
                normalise.replaceAll(
                        "[^0-9]",
                        ""
                );

        /*
         * Raha misy +261
         */
        Matcher intl =
                Pattern.compile(
                        "261(3[2348][0-9]{7})"
                ).matcher(
                        compact
                );

        if (intl.find()) {

            return "0" +
                    intl.group(1);
        }

        /*
         * Recherche directe:
         * 034xxxxxxx
         * 032xxxxxxx
         * 033xxxxxxx
         * 038xxxxxxx
         */
        Matcher direct =
                Pattern.compile(
                        "(0[3][2348][0-9]{7})"
                ).matcher(
                        compact
                );

        if (direct.find()) {

            return direct.group(1);
        }

        /*
         * 3. Cas OCR manuscrit:
         *
         * Ilay OCR indraindray manasaraka
         * ny chiffres amin'ny espaces.
         */
        String digits =
                normalise.replaceAll(
                        "[^0-9]",
                        ""
                );

        /*
         * Maka fenêtre 10 chiffres.
         */
        for (int i = 0;
             i + 10 <= digits.length();
             i++) {

            String candidate =
                    digits.substring(
                            i,
                            i + 10
                    );

            if (candidate.matches(
                    "0[3][2348][0-9]{7}"
            )) {

                return candidate;
            }
        }

        return null;
    }

    private String chercherNumero(
            String texte) {

        Pattern p =
                Pattern.compile(
                        "(?<!\\d)" +
                        "(?:" +
                        "\\+261[\\s.\\-]*3[2348]" +
                        "|" +
                        "0[3][2348]" +
                        ")" +
                        "(?:[\\s.\\-]*\\d){7}" +
                        "(?!\\d)"
                );

        Matcher m =
                p.matcher(texte);

        while (m.find()) {

            String brut =
                    m.group();

            if (brut == null) {
                continue;
            }

            String numero =
                    brut.replaceAll(
                            "[^0-9]",
                            ""
                    );

            if (numero.startsWith("261") &&
                    numero.length() == 12) {

                numero =
                        "0" +
                        numero.substring(3);
            }

            if (numero.matches(
                    "0[3][2348][0-9]{7}"
            )) {

                return numero;
            }
        }

        return null;
    }

    private void retournerNumero(
            String numero) {

        runOnUiThread(() -> {

            status(
                    "✅ Numéro détecté : " + numero
            );

            Intent result =
                    new Intent();

            /*
             * MainActivity V5/V6
             */
            result.putExtra(
                    "numero_scanned",
                    numero
            );

            /*
             * Compatibilité ancienne version
             */
            result.putExtra(
                    "numero_scanne",
                    numero
            );

            setResult(
                    RESULT_OK,
                    result
            );

            Toast.makeText(
                    this,
                    "Numéro détecté : " + numero,
                    Toast.LENGTH_SHORT
            ).show();

            new Handler().postDelayed(
                    this::finish,
                    350
            );
        });
    }

    private void status(
            String text) {

        runOnUiThread(() -> {

            if (statusText != null) {

                statusText.setText(
                        text
                );
            }
        });
    }

    private void erreur(
            String text) {

        runOnUiThread(() -> {

            if (statusText != null) {

                statusText.setText(
                        "❌ " + text
                );
            }

            Toast.makeText(
                    this,
                    text,
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private void fermerCamera() {

        try {

            if (captureSession != null) {

                captureSession.close();
                captureSession = null;
            }

        } catch (Exception ignored) {
        }

        try {

            if (cameraDevice != null) {

                cameraDevice.close();
                cameraDevice = null;
            }

        } catch (Exception ignored) {
        }

        try {

            if (imageReader != null) {

                imageReader.close();
                imageReader = null;
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != REQ_CAMERA) {
            return;
        }

        if (grantResults.length > 0 &&
                grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {

            preparerCamera();

        } else {

            Toast.makeText(
                    this,
                    "Autorisation caméra refusée.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    @Override
    protected void onPause() {

        fermerCamera();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        fermerCamera();

        try {

            if (cameraThread != null) {

                cameraThread.quitSafely();
                cameraThread = null;
            }

        } catch (Exception ignored) {
        }

        try {

            if (recognizer != null) {

                recognizer.close();
                recognizer = null;
            }

        } catch (Exception ignored) {
        }

        super.onDestroy();
    }
}
