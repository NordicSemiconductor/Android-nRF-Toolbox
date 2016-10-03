package no.nordicsemi.android.nrftoolbox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import no.nordicsemi.android.log.Logger;

public class MainActivity extends Activity {

    protected static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
     */
    public void onConnectClicked(final View view) {

        if (isBLEEnabled()) {
//            if (mService == null) {
//                setDefaultUI();
//                showDeviceScanningDialog(getFilterUUID());
//            } else {
//                Logger.v(mLogSession, "Disconnecting...");
//                mService.disconnect();
//            }
        } else {
            showBLEDialog();
        }
    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

}
