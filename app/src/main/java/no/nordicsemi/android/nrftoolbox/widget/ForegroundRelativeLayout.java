/*************************************************************************************************************************************************
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
 ************************************************************************************************************************************************/

package no.nordicsemi.android.nrftoolbox.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import no.nordicsemi.android.nrftoolbox.R;

public class ForegroundRelativeLayout extends RelativeLayout {

	private Drawable mForegroundSelector;
	private Rect mRectPadding;
	private boolean mUseBackgroundPadding = false;

	public ForegroundRelativeLayout(Context context) {
		super(context);
	}

	public ForegroundRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForegroundRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundRelativeLayout,
				defStyle, 0);

		final Drawable d = a.getDrawable(R.styleable.ForegroundRelativeLayout_foreground);
		if (d != null) {
			setForeground(d);
		}

		a.recycle();

		if (this.getBackground() instanceof NinePatchDrawable) {
			final NinePatchDrawable npd = (NinePatchDrawable) this.getBackground();
			mRectPadding = new Rect();
			if (npd.getPadding(mRectPadding)) {
			 mUseBackgroundPadding = true;
			}
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		if (mForegroundSelector != null && mForegroundSelector.isStateful()) {
			mForegroundSelector.setState(getDrawableState());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mForegroundSelector != null) {
			if (mUseBackgroundPadding) {
				mForegroundSelector.setBounds(mRectPadding.left, mRectPadding.top, w - mRectPadding.right, h - mRectPadding.bottom);
			} else {
				mForegroundSelector.setBounds(0, 0, w, h);
			}
		}
	}

	@Override
	protected void dispatchDraw(@NonNull Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mForegroundSelector != null) {
			mForegroundSelector.draw(canvas);
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || (who == mForegroundSelector);
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mForegroundSelector != null) mForegroundSelector.jumpToCurrentState();
	}

	public void setForeground(Drawable drawable) {
		if (mForegroundSelector != drawable) {
			if (mForegroundSelector != null) {
				mForegroundSelector.setCallback(null);
				unscheduleDrawable(mForegroundSelector);
			}

			mForegroundSelector = drawable;

			if (drawable != null) {
				setWillNotDraw(false);
				drawable.setCallback(this);
				if (drawable.isStateful()) {
					drawable.setState(getDrawableState());
				}
			} else {
				setWillNotDraw(true);
			}
			requestLayout();
			invalidate();
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void drawableHotspotChanged(float x, float y) {
		super.drawableHotspotChanged(x, y);
		if (mForegroundSelector != null) {
			mForegroundSelector.setHotspot(x, y);
		}
	}
} 