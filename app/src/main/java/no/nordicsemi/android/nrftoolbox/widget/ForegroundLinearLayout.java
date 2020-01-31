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
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import no.nordicsemi.android.nrftoolbox.R;

public class ForegroundLinearLayout extends LinearLayout {

	private Drawable foregroundSelector;
	private Rect rectPadding;
	private boolean useBackgroundPadding = false;

	public ForegroundLinearLayout(Context context) {
		super(context);
	}

	public ForegroundLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForegroundLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundLinearLayout,
				defStyle, 0);

		final Drawable d = a.getDrawable(R.styleable.ForegroundRelativeLayout_foreground);
		if (d != null) {
			setForeground(d);
		}

		a.recycle();

		if (this.getBackground() instanceof NinePatchDrawable) {
			final NinePatchDrawable npd = (NinePatchDrawable) this.getBackground();
			rectPadding = new Rect();
			if (npd.getPadding(rectPadding)) {
			 useBackgroundPadding = true;
			}
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		if (foregroundSelector != null && foregroundSelector.isStateful()) {
			foregroundSelector.setState(getDrawableState());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (foregroundSelector != null) {
			if (useBackgroundPadding) {
				foregroundSelector.setBounds(rectPadding.left, rectPadding.top, w - rectPadding.right, h - rectPadding.bottom);
			} else {
				foregroundSelector.setBounds(0, 0, w, h);
			}
		}
	}

	@Override
	protected void dispatchDraw(@NonNull Canvas canvas) {
		super.dispatchDraw(canvas);

		if (foregroundSelector != null) {
			foregroundSelector.draw(canvas);
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || (who == foregroundSelector);
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (foregroundSelector != null) foregroundSelector.jumpToCurrentState();
	}

	public void setForeground(Drawable drawable) {
		if (foregroundSelector != drawable) {
			if (foregroundSelector != null) {
				foregroundSelector.setCallback(null);
				unscheduleDrawable(foregroundSelector);
			}

			foregroundSelector = drawable;

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
		if (foregroundSelector != null) {
			foregroundSelector.setHotspot(x, y);
		}
	}
} 