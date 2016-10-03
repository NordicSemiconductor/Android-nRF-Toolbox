package no.nordicsemi.android.nrftoolbox.iot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import no.nordicsemi.android.nrftoolbox.uart.UARTService;
import no.nordicsemi.android.nrftoolbox.utility.ParserUtils;

public class IoTDataReceiver extends BroadcastReceiver {
    public IoTDataReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        byte[] data = intent.getByteArrayExtra(UARTService.EXTRA_DATA);
        String text = ParserUtils.parse(data);
        Log.i("static data receiver","data received "+text+" size "+data.length);
    }
}
