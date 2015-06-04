/*
 * Copyright (c) 2015, Nordic Semiconductor
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

package no.nordicsemi.android.nrftoolbox.uart;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class UARTActivity extends BleProfileServiceReadyActivity<UARTService.UARTBinder> implements UARTControlFragment.ControlFragmentListener, UARTInterface {
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private SlidingPaneLayout mSlider;
	private UARTService.UARTBinder mServiceBinder;
	private boolean mEditMode;

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return UARTService.class;
	}

	@Override
	protected void onServiceBinded(final UARTService.UARTBinder binder) {
		mServiceBinder = binder;
	}

	@Override
	protected void onServiceUnbinded() {
		mServiceBinder = null;
	}

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_uart);

		// Setup the sliding pane if it exists
		final SlidingPaneLayout slidingPane = mSlider = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
		if (slidingPane != null) {
			slidingPane.setSliderFadeColor(Color.TRANSPARENT);
			slidingPane.setShadowResourceLeft(R.drawable.shadow_r);
			slidingPane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
				@Override
				public void onPanelClosed(final View panel) {
					// Close the keyboard
					final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
					logFragment.onFragmentHidden();
				}
			});
		}
	}

	@Override
	protected void onRestoreInstanceState(final @NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mEditMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
		setEditMode(mEditMode, false);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SIS_EDIT_MODE, mEditMode);
	}

	@Override
	protected boolean onOptionsItemSelected(int itemId) {
		switch (itemId) {
		case R.id.action_show_log:
			mSlider.openPane();
			return true;
		}
		return false;
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.uart_feature_title;
	}

	@Override
	protected Uri getLocalAuthorityLogger() {
		return UARTLocalLogContentProvider.AUTHORITY_URI;
	}

	@Override
	protected void setDefaultUI() {
		// empty
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		// do nothing
	}

	@Override
	public void onDeviceSelected(final BluetoothDevice device, final String name) {
		// The super method starts the service
		super.onDeviceSelected(device, name);

		// Notify the log fragment about it
		final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
		logFragment.onServiceStarted();
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.uart_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.uart_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return null; // not used
	}

	@Override
	protected boolean isDiscoverableRequired() {
		return false;
	}

	@Override
	public void send(final String text) {
		if (mServiceBinder != null)
			mServiceBinder.send(text);
	}

	@Override
	public void setEditMode(final boolean editMode) {
		setEditMode(editMode, true);
	}

	@Override
	public void onBackPressed() {
		if (mSlider != null && mSlider.isOpen()) {
			mSlider.closePane();
			return;
		}
		if (mEditMode) {
			final UARTControlFragment fragment = (UARTControlFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_control);
			fragment.setEditMode(false);
			return;
		}
		super.onBackPressed();
	}

	/**
	 * Updates the ActionBar background color depending on whether we are in edit mode or not.
	 * 
	 * @param editMode
	 *            <code>true</code> to show edit mode, <code>false</code> otherwise
	 * @param change
	 *            if <code>true</code> the background will change with animation, otherwise immediately
	 */
	@SuppressLint("NewApi")
	private void setEditMode(final boolean editMode, final boolean change) {
		mEditMode = editMode;
		if (!change) {
			final ColorDrawable color = new ColorDrawable();
			int darkColor = 0;
			if (editMode) {
				color.setColor(getResources().getColor(R.color.orange));
				darkColor = getResources().getColor(R.color.dark_orange);
			} else {
				color.setColor(getResources().getColor(R.color.actionBarColor));
				darkColor = getResources().getColor(R.color.actionBarColorDark);
			}
			getSupportActionBar().setBackgroundDrawable(color);

			// Since Lollipop the status bar color may also be changed
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				getWindow().setStatusBarColor(darkColor);
		} else {
			final TransitionDrawable transition = (TransitionDrawable) getResources().getDrawable(
					editMode ? R.drawable.start_edit_mode : R.drawable.stop_edit_mode);
			transition.setCrossFadeEnabled(true);
			getSupportActionBar().setBackgroundDrawable(transition);
			transition.startTransition(200);

			// Since Lollipop the status bar color may also be changed
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				final int colorFrom = getResources().getColor(editMode ? R.color.actionBarColorDark : R.color.dark_orange);
				final int colorTo = getResources().getColor(!editMode ? R.color.actionBarColorDark : R.color.dark_orange);

				final ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
				anim.setDuration(200);
				anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(final ValueAnimator animation) {
						getWindow().setStatusBarColor((Integer) animation.getAnimatedValue());
					}
				});
				anim.start();
			}

			if (mSlider != null && editMode) {
				mSlider.closePane();
			}
		}
	}
}
