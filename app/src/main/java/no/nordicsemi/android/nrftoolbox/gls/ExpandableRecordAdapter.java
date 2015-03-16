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
package no.nordicsemi.android.nrftoolbox.gls;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

public class ExpandableRecordAdapter extends BaseExpandableListAdapter {
	private final GlucoseManager mGlucoseManager;
	private final LayoutInflater mInflater;
	private final Context mContext;

	public ExpandableRecordAdapter(final Context context, final GlucoseManager manager) {
		mGlucoseManager = manager;
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getGroupCount() {
		return mGlucoseManager.getRecords().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGlucoseManager.getRecords().valueAt(groupPosition);
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return mGlucoseManager.getRecords().keyAt(groupPosition);
	}

	@Override
	public View getGroupView(final int position, boolean isExpanded, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.activity_feature_gls_item, parent, false);

			final GroupViewHolder holder = new GroupViewHolder();
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.details = (TextView) view.findViewById(R.id.details);
			holder.concentration = (TextView) view.findViewById(R.id.gls_concentration);
			view.setTag(holder);
		}
		final GlucoseRecord record = (GlucoseRecord) getGroup(position);
		if (record == null)
			return view; // this may happen during closing the activity
		final GroupViewHolder holder = (GroupViewHolder) view.getTag();
		holder.time.setText(mContext.getString(R.string.gls_timestamp, record.time));
		try {
			holder.details.setText(mContext.getResources().getStringArray(R.array.gls_type)[record.type]);
		} catch (final ArrayIndexOutOfBoundsException e) {
			holder.details.setText(mContext.getResources().getStringArray(R.array.gls_type)[0]);
		}
		if (record.unit == GlucoseRecord.UNIT_kgpl) {
			holder.concentration.setText(mContext.getString(R.string.gls_value, record.glucoseConcentration * 100000.0f));
		} else {
			holder.concentration.setText(mContext.getString(R.string.gls_value, record.glucoseConcentration * 1000.0f));
		}
		return view;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		final GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
		int count = 1 + (record.status != 0 ? 1 : 0); // Sample Location and Sensor Status Annunciation
		if (record.context != null) {
			final GlucoseRecord.MeasurementContext context = record.context;
			if (context.carbohydrateId != 0)
				count += 1; // Carbohydrate ID and units
			if (context.meal != 0)
				count += 1; // Meal
			if (context.tester != 0)
				count += 1; // Tester
			if (context.health != 0)
				count += 1; // Health
			if (context.exerciseDuration != 0)
				count += 1; // Duration and intensity
			if (context.medicationId != 0)
				count += 1; // Medication ID and quantity (with unit)
			if (context.HbA1c != 0)
				count += 1; // HbA1c
		}
		return count;
	}

	@Override
	public Object getChild(final int groupPosition, final int childPosition) {
		final Resources resources = mContext.getResources();
		final GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
		switch (childPosition) {
		case 0:
			String location = null;
			try {
				location = resources.getStringArray(R.array.gls_location)[record.sampleLocation];
			} catch (final ArrayIndexOutOfBoundsException e) {
				location = resources.getStringArray(R.array.gls_location)[0];
			}
			return new Pair<>(resources.getString(R.string.gls_location_title), location);
		case 1: { // sensor status annunciation
			final StringBuilder builder = new StringBuilder();
			final int status = record.status;
			for (int i = 0; i < 12; ++i)
				if ((status & (1 << i)) > 0)
					builder.append(resources.getStringArray(R.array.gls_status_annunciation)[i]).append("\n");
			builder.setLength(builder.length() - 1);
			return new Pair<>(resources.getString(R.string.gls_status_annunciation_title), builder.toString());
		}
		case 2: { // carbohydrate id and unit
			final StringBuilder builder = new StringBuilder();
			builder.append(resources.getStringArray(R.array.gls_context_carbohydrare)[record.context.carbohydrateId]).append(" (").append(record.context.carbohydrateUnits).append(" kg)");
			return new Pair<>(resources.getString(R.string.gls_context_carbohydrare_title), builder.toString());
		}
		case 3: { // meal
			final StringBuilder builder = new StringBuilder();
			builder.append(resources.getStringArray(R.array.gls_context_meal)[record.context.meal]);
			return new Pair<>(resources.getString(R.string.gls_context_meal_title), builder.toString());
		}
		case 4: { // tester
			final StringBuilder builder = new StringBuilder();
			builder.append(resources.getStringArray(R.array.gls_context_tester)[record.context.tester]);
			return new Pair<>(resources.getString(R.string.gls_context_tester_title), builder.toString());
		}
		case 5: { // health
			final StringBuilder builder = new StringBuilder();
			builder.append(resources.getStringArray(R.array.gls_context_health)[record.context.health]);
			return new Pair<>(resources.getString(R.string.gls_context_health_title), builder.toString());
		}
		default:
			// TODO write parsers for other properties
			return new Pair<>("Not implemented", "The value exists but is not shown");
		}
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return groupPosition + childPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.activity_feature_gls_subitem, parent, false);
			final ChildViewHolder holder = new ChildViewHolder();
			holder.title = (TextView) view.findViewById(android.R.id.text1);
			holder.details = (TextView) view.findViewById(android.R.id.text2);
			view.setTag(holder);
		}
		final Pair<String, String> value = (Pair<String, String>) getChild(groupPosition, childPosition);
		final ChildViewHolder holder = (ChildViewHolder) view.getTag();
		holder.title.setText(value.first);
		holder.details.setText(value.second);
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return false;
	}

	private class GroupViewHolder {
		private TextView time;
		private TextView details;
		private TextView concentration;
	}

	private class ChildViewHolder {
		private TextView title;
		private TextView details;
	}

}
