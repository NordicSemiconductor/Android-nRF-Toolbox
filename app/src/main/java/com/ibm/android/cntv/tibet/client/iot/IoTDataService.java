package com.ibm.android.cntv.tibet.client.iot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ibm.android.cntv.tibet.client.uart.UARTService;
import com.ibm.android.cntv.tibet.client.utility.ParserUtils;
import com.ibm.android.cntv.tibet.client.utils.Constants;
import com.ibm.android.cntv.tibet.client.utils.Decoder;
import com.ibm.android.cntv.tibet.client.utils.MyIoTActionListener;
import com.ibm.android.cntv.tibet.client.utils.MyIoTCallbacks;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;

public class IoTDataService extends Service {

    private IoTClient iotClient;
    private MyIoTCallbacks myIoTCallbacks;
    private String mUser;
    private String mToken;

    public IoTDataService() {
    }

    public class IoTDataServiceBinder extends Binder {
        public void updateUserToken(String user, String token){
            if(mUser!=null && mUser.equals(user) && mToken!=null && mToken.equals(token))
                return;
            if(iotClient!=null){
                disconnect();
            }
            mUser = user;
            mToken = token;
            connect();
        }

        public boolean isConnected(){
            if(iotClient!=null) return iotClient.isMqttConnected();
            else return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IoTDataServiceBinder();
    }

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra(UARTService.EXTRA_DATA);
            String text = ParserUtils.parse(data);
            Log.i("data receiver","data received "+text+" size "+data.length);
            if(data.length!=20){
                Log.w("data receiver","incorrect message length! "+text+" size "+data.length);
                return;
            }
            if(iotClient==null || !iotClient.isMqttConnected()) return;
            MyIoTActionListener listener = new MyIoTActionListener(context, Constants.ActionStateStatus.PUBLISH);
            String msg = Decoder.decode(data);
            try {
                String eventType="";
                if (data[2] == 0x01)
                {
                    eventType = Constants.PACK1_EVENT;
                }
                else if (data[2] == 0x02)
                {
                    eventType = Constants.PACK2_EVENT;
                }
                else if (data[2] == 0x03)
                {
                    eventType = Constants.PACK3_EVENT;
                }
                if(eventType.length()>0) {
                    IMqttDeliveryToken deliveryToken = iotClient.publishEvent(eventType, "json", msg, 2, false, listener);
                    if(deliveryToken != null)
                        Log.i("data receiver", "data published" + text);
                }
                else {
                    Log.w("data receiver", "unknown message type! data not published" + text);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        connect();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UARTService.BROADCAST_UART_RX);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mDataReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mDataReceiver);

        disconnect();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void connect(){
        if(mUser==null || mToken==null) return;
        iotClient = IoTClient.getInstance(getApplicationContext(), "9iybos", mUser, "Android", mToken);
        myIoTCallbacks = MyIoTCallbacks.getInstance(getApplicationContext());
        MyIoTActionListener listener = new MyIoTActionListener(getApplicationContext(), Constants.ActionStateStatus.CONNECTING);
        try {
            iotClient.connectDevice(myIoTCallbacks,listener,null);
            Log.i("data receiver", "iotclient connected");
        } catch (MqttException e) {
            Log.e("iotclient connect fail", e.getMessage());
            e.printStackTrace();
            iotClient = null;
        }
    }

    private void disconnect(){
        if(iotClient==null) return;
        MyIoTActionListener listener = new MyIoTActionListener(getApplicationContext(), Constants.ActionStateStatus.DISCONNECTING);
        try {
            iotClient.disconnectDevice(listener);
            Log.i("data receiver", "iotclient disconnected");
        } catch (MqttException e) {
            e.printStackTrace();
        }
        iotClient = null;
    }
}
