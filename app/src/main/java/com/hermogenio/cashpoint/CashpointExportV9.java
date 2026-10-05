package com.hermogenio.cashpoint;

import android.content.Context;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class CashpointExportV9 {

    public static File exportJson(Context context)
            throws Exception {

        JSONArray tx =
                CashpointDataV9.loadTransactions(context);

        File dir =
                new File(
                        context.getExternalFilesDir(null),
                        "CASHPOINT_BACKUP"
                );

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file =
                new File(
                        dir,
                        "CASHPOINT_BACKUP_" +
                        System.currentTimeMillis() +
                        ".json"
                );

        FileOutputStream out =
                new FileOutputStream(file);

        out.write(
                tx.toString(2)
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );

        out.close();

        return file;
    }
}
