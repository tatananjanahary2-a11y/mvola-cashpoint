package com.hermogenio.cashpoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CashpointArchiveReceiverV9
        extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        CashpointArchiveV9.archive(context);

        CashpointArchiveV9.schedule(context);
    }
}
