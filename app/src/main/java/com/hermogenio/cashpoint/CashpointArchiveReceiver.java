package com.hermogenio.cashpoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CashpointArchiveReceiver
        extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        CashpointDailyArchive
                .verifierEtArchiver(
                        context
                );

        CashpointArchiveScheduler
                .programmer(
                        context
                );
    }
}
