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

	private float mExpandCircleRadius;
	private float mShrinkCircleRadius;

	private ObjectAnimator mExpandCircleAnimator;
	private ObjectAnimator mFadeInLabelAnimator;
	private AnimatorSet mExpandAnimator;

	private ObjectAnimator mShrinkCircleAnimator;
	private ObjectAnimator mFadeOutLabelAnimator;
	private AnimatorSet mShrinkAnimator;

	private TextView mName;
	private CircledImageView mIcon;

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

		mName = (TextView) findViewById(R.id.name);
		mIcon = (CircledImageView) findViewById(R.id.icon);
		mExpandCircleRadius = mIcon.getCircleRadius();
		mShrinkCircleRadius = mExpandCircleRadius * SHRINK_CIRCLE_RATIO;

		mShrinkCircleAnimator = ObjectAnimator.ofFloat(mIcon, "circleRadius", mExpandCircleRadius, mShrinkCircleRadius);
		mFadeOutLabelAnimator = ObjectAnimator.ofFloat(mName, "alpha",	EXPAND_LABEL_ALPHA, SHRINK_LABEL_ALPHA);
		mShrinkAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
		mShrinkAnimator.playTogether(mShrinkCircleAnimator, mFadeOutLabelAnimator);

		mExpandCircleAnimator = ObjectAnimator.ofFloat(mIcon, "circleRadius", mShrinkCircleRadius, mExpandCircleRadius);
		mFadeInLabelAnimator = ObjectAnimator.ofFloat(mName, "alpha", SHRINK_LABEL_ALPHA, EXPAND_LABEL_ALPHA);
		mExpandAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
		mExpandAnimator.playTogether(mExpandCircleAnimator, mFadeInLabelAnimator);
	}

	@Override
	public void onCenterPosition(final boolean animate) {
		if (animate) {
			mShrinkAnimator.cancel();
			if (!mExpandAnimator.isRunning()) {
				mExpandCircleAnimator.setFloatValues(mIcon.getCircleRadius(), mExpandCircleRadius);
				mFadeInLabelAnimator.setFloatValues(mName.getAlpha(), EXPAND_LABEL_ALPHA);
				mExpandAnimator.start();
			}
		} else {
			mExpandAnimator.cancel();
			mIcon.setCircleRadius(mExpandCircleRadius);
			mName.setAlpha(EXPAND_LABEL_ALPHA);
		}
	}

	@Override
	public void onNonCenterPosition(final boolean animate) {
		if (animate) {
			mExpandAnimator.cancel();
			if (!mShrinkAnimator.isRunning()) {
				mShrinkCircleAnimator.setFloatValues(mIcon.getCircleRadius(), mShrinkCircleRadius);
				mFadeOutLabelAnimator.setFloatValues(mName.getAlpha(), SHRINK_LABEL_ALPHA);
				mShrinkAnimator.start();
			}
		} else {
			mShrinkAnimator.cancel();
			mIcon.setCircleRadius(mShrinkCircleRadius);
			mName.setAlpha(SHRINK_LABEL_ALPHA);
		}
	}
}
