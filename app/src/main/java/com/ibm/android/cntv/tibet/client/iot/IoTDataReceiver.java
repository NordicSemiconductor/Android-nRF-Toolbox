//package com.ibm.android.cntv.tibet.client.iot;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import com.ibm.android.cntv.tibet.client.uart.UARTService;
//import com.ibm.android.cntv.tibet.client.utility.ParserUtils;
//
//public class IoTDataReceiver extends BroadcastReceiver {
//    public IoTDataReceiver() {
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        byte[] data = intent.getByteArrayExtra(UARTService.EXTRA_DATA);
//        String text = ParserUtils.parse(data);
//        Log.i("static data receiver","data received "+text+" size "+data.length);
//    }
//}
