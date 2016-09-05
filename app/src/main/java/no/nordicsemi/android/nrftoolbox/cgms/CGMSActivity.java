/*
 * Copyright (c) 2016, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.cgms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class CGMSActivity extends BleProfileServiceReadyActivity<CGMService.CGMSBinder>  implements PopupMenu.OnMenuItemClickListener {

    private View mControlPanelStd;
    private View mControlPanelAbort;
    private TextView mUnitView;
    private ListView mRecordsListView;
    private CGMSRecordsAdapter mCgmsRecordsAdapter;

    private CGMService.CGMSBinder mBinder;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_feature_cgms);
        setGUI();
    }

    @Override
    protected void onInitialize(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    private void setGUI() {
        mRecordsListView = (ListView) findViewById(R.id.list);
        mUnitView = (TextView) findViewById(R.id.unit);
        mControlPanelStd = findViewById(R.id.cgms_control_std);
        mControlPanelAbort = findViewById(R.id.cgms_control_abort);

        findViewById(R.id.action_last).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecords();
                if(mBinder != null) {
                    mBinder.clear();
                    mBinder.getLastRecord();
                }
            }
        });
        findViewById(R.id.action_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecords();
                if(mBinder != null){
                    clearRecords();
                    mBinder.getAllRecords();
                }
            }
        });
        findViewById(R.id.action_abort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBinder != null){
                    mBinder.abort();
                }
            }
        });

        // create popup menu attached to the button More
        findViewById(R.id.action_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(CGMSActivity.this, v);
                menu.setOnMenuItemClickListener(CGMSActivity.this);
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.gls_more, menu.getMenu());
                menu.show();
            }
        });
    }

    private void loadAdapter(SparseArray<CGMSRecord> records){
        for(int i = 0; i < records.size(); i++){
            mCgmsRecordsAdapter.addItem(records.get(i));
        }
        mCgmsRecordsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onServiceBinded(CGMService.CGMSBinder binder) {
        mBinder = binder;
        SparseArray<CGMSRecord> cgmsRecords = binder.getCgmsRecords();
        if(cgmsRecords != null && cgmsRecords.size() > 0){
            if(mCgmsRecordsAdapter == null) {
                mCgmsRecordsAdapter = new CGMSRecordsAdapter(CGMSActivity.this);
                mRecordsListView.setAdapter(mCgmsRecordsAdapter);
            }
            loadAdapter(cgmsRecords);
        }
    }

    @Override
    protected void onServiceUnbinded() {
        mBinder = null;
    }

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return CGMService.class;
    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.cgms_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.cgms_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.cgms_default_name;
    }

    @Override
    protected UUID getFilterUUID() {
        return CGMSManager.CGMS_UUID;
    }

    @Override
    public void onServicesDiscovered(final boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady() {

    }

    private void setOperationInProgress(final boolean progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // setSupportProgressBarIndeterminateVisibility(progress);
                mControlPanelStd.setVisibility(!progress ? View.VISIBLE : View.GONE);
                mControlPanelAbort.setVisibility(progress ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        setOperationInProgress(false);
        clearRecords();
    }

    @Override
    protected void setDefaultUI() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_refresh:
                /*if(mBinder != null)
                mBinder.refreshRecords();*/
                break;
            case R.id.action_first:
                clearRecords();
                if(mBinder != null)
                    mBinder.getFirstRecord();
                break;
            case R.id.action_clear:
                clearRecords();
                if(mBinder != null)
                    mBinder.clear();
                break;
            case R.id.action_delete_all:
                clearRecords();
                if(mBinder != null)
                    mBinder.deleteAllRecords();
                break;
        }
        return true;
    }

    private void clearRecords(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mCgmsRecordsAdapter != null){
                    mCgmsRecordsAdapter.clear();
                    mCgmsRecordsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (CGMService.BROADCAST_CGMS_VALUES.equals(action)) {
                CGMSRecord cgmsRecord = intent.getExtras().getParcelable(CGMService.EXTRA_CGMS_RECORD);
                if(mCgmsRecordsAdapter == null){
                    mCgmsRecordsAdapter = new CGMSRecordsAdapter(CGMSActivity.this);
                    mRecordsListView.setAdapter(mCgmsRecordsAdapter);
                }
                mCgmsRecordsAdapter.addItem(cgmsRecord);
                mCgmsRecordsAdapter.notifyDataSetChanged();

            } else if (CGMService.BROADCAST_DATA_SET_CHANGED.equals(action)) {
                // Update GUI
                clearRecords();
            } else if (CGMService.OPERATION_STARTED.equals(action)) {
                // Update GUI
                setOperationInProgress(true);
            } else if (CGMService.OPERATION_COMPLETED.equals(action)) {
                // Update GUI
                setOperationInProgress(false);
            } else if (CGMService.OPERATION_SUPPORTED.equals(action)) {
                // Update GUI
                setOperationInProgress(false);
            } else if (CGMService.OPERATION_NOT_SUPPORTED.equals(action)) {
                // Update GUI
                setOperationInProgress(false);
            } else if (CGMService.OPERATION_ABORTED.equals(action)) {
                // Update GUI
                setOperationInProgress(false);
            } else if (CGMService.OPERATION_FAILED.equals(action)) {
                // Update GUI
                setOperationInProgress(false);
                showToast(R.string.gls_operation_failed);
            }
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CGMService.BROADCAST_CGMS_VALUES);
        intentFilter.addAction(CGMService.BROADCAST_DATA_SET_CHANGED);
        intentFilter.addAction(CGMService.OPERATION_STARTED);
        intentFilter.addAction(CGMService.OPERATION_COMPLETED);
        intentFilter.addAction(CGMService.OPERATION_SUPPORTED);
        intentFilter.addAction(CGMService.OPERATION_NOT_SUPPORTED);
        intentFilter.addAction(CGMService.OPERATION_ABORTED);
        intentFilter.addAction(CGMService.OPERATION_FAILED);
        return intentFilter;
    }
}
