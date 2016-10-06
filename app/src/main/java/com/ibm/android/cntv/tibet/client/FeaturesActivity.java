///*
// * Copyright (c) 2015, Nordic Semiconductor
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
// * documentation and/or other materials provided with the distribution.
// *
// * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
// * software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
// * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package com.ibm.android.cntv.tibet.client;
//
//import android.content.ActivityNotFoundException;
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.GridView;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.List;
//
//import com.ibm.android.cntv.tibet.client.adapter.AppAdapter;
//import com.ibm.android.cntv.tibet.client.hrs.HRSActivity;
//
//public class FeaturesActivity extends AppCompatActivity {
//	private static final String NRF_CONNECT_CATEGORY = "com.ibm.android.cntv.tibet.client.LAUNCHER";
//	private static final String UTILS_CATEGORY = "com.ibm.android.cntv.tibet.client.UTILS";
//	private static final String NRF_CONNECT_PACKAGE = "no.nordicsemi.android.mcp";
//	private static final String NRF_CONNECT_CLASS = NRF_CONNECT_PACKAGE + ".DeviceListActivity";
//	private static final String NRF_CONNECT_MARKET_URI = "market://details?id=no.nordicsemi.android.mcp";
//
//	// Extras that can be passed from NFC (see SplashscreenActivity)
//	public static final String EXTRA_APP = "application/vnd.no.nordicsemi.type.app";
//	public static final String EXTRA_ADDRESS = "application/vnd.no.nordicsemi.type.address";
//
//	private DrawerLayout mDrawerLayout;
//	private ActionBarDrawerToggle mDrawerToggle;
//
//	@Override
//	protected void onCreate(final Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_features);
//
//        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//        setSupportActionBar(toolbar);
//
//		// ensure that Bluetooth exists
//		if (!ensureBLEExists())
//			finish();
//
//		final DrawerLayout drawer = mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//
//		// Set the drawer toggle as the DrawerListener
//		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
//            @Override
//            public void onDrawerSlide(final View drawerView, final float slideOffset) {
//                // Disable the Hamburger icon animation
//                super.onDrawerSlide(drawerView, 0);
//            }
//        };
//		drawer.addDrawerListener(mDrawerToggle);
//
//		// setup plug-ins in the drawer
//		setupPluginsInDrawer((ViewGroup) drawer.findViewById(R.id.plugin_container));
//
//		// configure the app grid
//		final GridView grid = (GridView) findViewById(R.id.grid);
//		grid.setAdapter(new AppAdapter(this));
//		grid.setEmptyView(findViewById(android.R.id.empty));
//
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//		final Intent intent = getIntent();
//		if (intent.hasExtra(EXTRA_APP) && intent.hasExtra(EXTRA_ADDRESS)) {
//			final String app = intent.getStringExtra(EXTRA_APP);
//			switch (app) {
//				case "HRM":
//					final Intent newIntent = new Intent(this, HRSActivity.class);
//					newIntent.putExtra(EXTRA_ADDRESS, intent.getByteArrayExtra(EXTRA_ADDRESS));
//					newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//					startActivity(newIntent);
//					break;
//				default:
//					// other are not supported yet
//					break;
//			}
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(final Menu menu) {
//		getMenuInflater().inflate(R.menu.help, menu);
//		return true;
//	}
//
//	@Override
//	protected void onPostCreate(final Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		// Sync the toggle state after onRestoreInstanceState has occurred.
//		mDrawerToggle.syncState();
//	}
//
//	@Override
//	public void onConfigurationChanged(final Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		mDrawerToggle.onConfigurationChanged(newConfig);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(final MenuItem item) {
//		// Pass the event to ActionBarDrawerToggle, if it returns
//		// true, then it has handled the app icon touch event
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//
//		switch (item.getItemId()) {
//		case R.id.action_about:
//			final AppHelpFragment fragment = AppHelpFragment.getInstance(R.string.about_text, true);
//			fragment.show(getSupportFragmentManager(), null);
//			break;
//		}
//		return true;
//	}
//
//	private void setupPluginsInDrawer(final ViewGroup container) {
//		final LayoutInflater inflater = LayoutInflater.from(this);
//		final PackageManager pm = getPackageManager();
//
//		// look for nRF Connect
//		final Intent nrfConnectIntent = new Intent(Intent.ACTION_MAIN);
//		nrfConnectIntent.addCategory(NRF_CONNECT_CATEGORY);
//		nrfConnectIntent.setClassName(NRF_CONNECT_PACKAGE, NRF_CONNECT_CLASS);
//		final ResolveInfo nrfConnectInfo = pm.resolveActivity(nrfConnectIntent, 0);
//
//		// configure link to nRF Connect
//		final TextView nrfConnectItem = (TextView) container.findViewById(R.id.link_mcp);
//		if (nrfConnectInfo == null) {
//			nrfConnectItem.setTextColor(Color.GRAY);
//			ColorMatrix grayscale = new ColorMatrix();
//			grayscale.setSaturation(0.0f);
//			nrfConnectItem.getCompoundDrawables()[0].mutate().setColorFilter(new ColorMatrixColorFilter(grayscale));
//		}
//		nrfConnectItem.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				Intent action = nrfConnectIntent;
//				if (nrfConnectInfo == null)
//					action = new Intent(Intent.ACTION_VIEW, Uri.parse(NRF_CONNECT_MARKET_URI));
//				action.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//				action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				try {
//					startActivity(action);
//				} catch (final ActivityNotFoundException e) {
//					Toast.makeText(FeaturesActivity.this, R.string.no_application_play, Toast.LENGTH_SHORT).show();
//				}
//				mDrawerLayout.closeDrawers();
//			}
//		});
//
//		// look for other plug-ins
//		final Intent utilsIntent = new Intent(Intent.ACTION_MAIN);
//		utilsIntent.addCategory(UTILS_CATEGORY);
//
//		final List<ResolveInfo> appList = pm.queryIntentActivities(utilsIntent, 0);
//		for (final ResolveInfo info : appList) {
//			final View item = inflater.inflate(R.layout.drawer_plugin, container, false);
//			final ImageView icon = (ImageView) item.findViewById(android.R.id.icon);
//			final TextView label = (TextView) item.findViewById(android.R.id.text1);
//
//			label.setText(info.loadLabel(pm));
//			icon.setImageDrawable(info.loadIcon(pm));
//			item.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(final View v) {
//					final Intent intent = new Intent();
//					intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
//					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(intent);
//					mDrawerLayout.closeDrawers();
//				}
//			});
//			container.addView(item);
//		}
//	}
//
//	private boolean ensureBLEExists() {
//		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
//			return false;
//		}
//		return true;
//	}
//}
