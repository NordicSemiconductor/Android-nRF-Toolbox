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

package no.nordicsemi.android.nrftoolbox;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceItemLayout extends RelativeLayout implements WearableListView.OnCenterProximityListener {
	private static final int ANIMATION_DURATION_MS = 150;
	/**
	 * The ratio for the size of a circle in shrink state.
	 */
	private static final float SHRINK_CIRCLE_RATIO = .75f;

	private static final float SHRINK_LABEL_ALPHA = .5f;
	private static final float EXPAND_LABEL_ALPHA = 1f;

	private float expandCircleRadius;
	private float shrinkCircleRadius;

	private ObjectAnimator expandCircleAnimator;
	private ObjectAnimator fadeInLabelAnimator;
	private AnimatorSet expandAnimator;

	private ObjectAnimator shrinkCircleAnimator;
	private ObjectAnimator fadeOutLabelAnimator;
	private AnimatorSet shrinkAnimator;

	private TextView name;
	private CircledImageView icon;

	public DeviceItemLayout(final Context context) {
		this(context, null, 0);
	}

	public DeviceItemLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeviceItemLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		name = findViewById(R.id.name);
		icon = findViewById(R.id.icon);
		expandCircleRadius = icon.getCircleRadius();
		shrinkCircleRadius = expandCircleRadius * SHRINK_CIRCLE_RATIO;

		shrinkCircleAnimator = ObjectAnimator.ofFloat(icon, "circleRadius", expandCircleRadius, shrinkCircleRadius);
		fadeOutLabelAnimator = ObjectAnimator.ofFloat(name, "alpha",	EXPAND_LABEL_ALPHA, SHRINK_LABEL_ALPHA);
		shrinkAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
		shrinkAnimator.playTogether(shrinkCircleAnimator, fadeOutLabelAnimator);

		expandCircleAnimator = ObjectAnimator.ofFloat(icon, "circleRadius", shrinkCircleRadius, expandCircleRadius);
		fadeInLabelAnimator = ObjectAnimator.ofFloat(name, "alpha", SHRINK_LABEL_ALPHA, EXPAND_LABEL_ALPHA);
		expandAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
		expandAnimator.playTogether(expandCircleAnimator, fadeInLabelAnimator);
	}

	@Override
	public void onCenterPosition(final boolean animate) {
		if (animate) {
			shrinkAnimator.cancel();
			if (!expandAnimator.isRunning()) {
				expandCircleAnimator.setFloatValues(icon.getCircleRadius(), expandCircleRadius);
				fadeInLabelAnimator.setFloatValues(name.getAlpha(), EXPAND_LABEL_ALPHA);
				expandAnimator.start();
			}
		} else {
			expandAnimator.cancel();
			icon.setCircleRadius(expandCircleRadius);
			name.setAlpha(EXPAND_LABEL_ALPHA);
		}
	}

	@Override
	public void onNonCenterPosition(final boolean animate) {
		if (animate) {
			expandAnimator.cancel();
			if (!shrinkAnimator.isRunning()) {
				shrinkCircleAnimator.setFloatValues(icon.getCircleRadius(), shrinkCircleRadius);
				fadeOutLabelAnimator.setFloatValues(name.getAlpha(), SHRINK_LABEL_ALPHA);
				shrinkAnimator.start();
			}
		} else {
			shrinkAnimator.cancel();
			icon.setCircleRadius(shrinkCircleRadius);
			name.setAlpha(SHRINK_LABEL_ALPHA);
		}
	}
}
