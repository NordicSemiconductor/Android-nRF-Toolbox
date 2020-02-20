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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

public class ExpandableRecordAdapter extends BaseExpandableListAdapter {;
	private final GlucoseManager glucoseManager;
	private final LayoutInflater inflater;
	private final Context context;
	private SparseArray<GlucoseRecord> records;

	ExpandableRecordAdapter(final Context context, final GlucoseManager manager) {
		this.glucoseManager = manager;
		this.context = context;
		inflater = LayoutInflater.from(context);
		records = manager.getRecords().clone();
	}

	@Override
	public void notifyDataSetChanged() {
		records = glucoseManager.getRecords().clone();
		super.notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return records.size();
	}

	@Override
	public Object getGroup(final int groupPosition) {
		return records.valueAt(groupPosition);
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return records.keyAt(groupPosition);
	}

	@Override
	public View getGroupView(final int position, boolean isExpanded, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.activity_feature_gls_item, parent, false);

			final GroupViewHolder holder = new GroupViewHolder();
			holder.time = view.findViewById(R.id.time);
			holder.details = view.findViewById(R.id.details);
			holder.concentration = view.findViewById(R.id.gls_concentration);
			view.setTag(holder);
		}
		final GlucoseRecord record = (GlucoseRecord) getGroup(position);
		if (record == null)
			return view; // this may happen during closing the activity
		final GroupViewHolder holder = (GroupViewHolder) view.getTag();
		holder.time.setText(context.getString(R.string.gls_timestamp, record.time));
		try {
			holder.details.setText(context.getResources().getStringArray(R.array.gls_type)[record.type]);
		} catch (final ArrayIndexOutOfBoundsException e) {
			holder.details.setText(context.getResources().getStringArray(R.array.gls_type)[0]);
		}
		if (record.unit == GlucoseRecord.UNIT_kgpl) {
			holder.concentration.setText(context.getString(R.string.gls_value, record.glucoseConcentration * 100000.0f));
		} else {
			holder.concentration.setText(context.getString(R.string.gls_value, record.glucoseConcentration * 1000.0f));
		}
		return view;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		final GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
		int count = 1 + (record.status != 0 ? 1 : 0); // Sample Location and optional Sensor Status Annunciation
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
		final Resources resources = context.getResources();
		final GlucoseRecord record = (GlucoseRecord) getGroup(groupPosition);
		String tmp;
		switch (childIdToItemId(childPosition, record)) {
			case 0:
				try {
					tmp = resources.getStringArray(R.array.gls_location)[record.sampleLocation];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_location)[0];
				}
				return new Pair<>(resources.getString(R.string.gls_location_title), tmp);
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
				try {
					tmp = resources.getStringArray(R.array.gls_context_carbohydrare)[record.context.carbohydrateId];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_context_carbohydrare)[0];
				}
				return new Pair<>(resources.getString(R.string.gls_context_carbohydrare_title), tmp + " (" + record.context.carbohydrateUnits + " g)");
			}
			case 3: { // meal
				try {
					tmp = resources.getStringArray(R.array.gls_context_meal)[record.context.meal];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_context_meal)[0];
				}
				return new Pair<>(resources.getString(R.string.gls_context_meal_title), tmp);
			}
			case 4: { // tester
				try {
					tmp = resources.getStringArray(R.array.gls_context_tester)[record.context.tester];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_context_tester)[0];
				}
				return new Pair<>(resources.getString(R.string.gls_context_tester_title), tmp);
			}
			case 5: { // health
				try {
					tmp = resources.getStringArray(R.array.gls_context_health)[record.context.health];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_context_health)[0];
				}
				return new Pair<>(resources.getString(R.string.gls_context_health_title), tmp);
			}
			case 6: { // exercise duration and intensity
				return new Pair<>(resources.getString(R.string.gls_context_exercise_title), resources.getString(R.string.gls_context_exercise, record.context.exerciseDuration, record.context.exerciseIntensity));
			}
			case 7: { // medication ID and quantity
				try {
					tmp = resources.getStringArray(R.array.gls_context_medication_id)[record.context.medicationId];
				} catch (final ArrayIndexOutOfBoundsException e) {
					tmp = resources.getStringArray(R.array.gls_context_medication_id)[0];
				}
				final int resId = record.context.medicationUnit == GlucoseRecord.UNIT_kgpl ? R.string.gls_context_medication_kg : R.string.gls_context_medication_l;
				return new Pair<>(resources.getString(R.string.gls_context_medication_title), resources.getString(resId, tmp, record.context.medicationQuantity));
			}
			case 8: { // HbA1c value
				return new Pair<>(resources.getString(R.string.gls_context_hba1c_title), resources.getString(R.string.gls_context_hba1c, record.context.HbA1c));
			}
			default:
				return new Pair<>("Not implemented", "The value exists but is not shown");
			}
	}

	private int childIdToItemId(final int childPosition, final GlucoseRecord record) {
		int itemId = 0;
		int child = childPosition;

		// Location is required
		if (itemId == childPosition)
			return itemId;

		if (++itemId > 0 && record.status != 0 && --child == 0) return itemId;
		if (record.context != null) {
			if (++itemId > 0 && record.context.carbohydrateId != 0 	 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.meal != 0 			 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.tester != 0 			 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.health != 0 			 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.exerciseDuration != 0 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.medicationId != 0 	 && --child == 0) return itemId;
			if (++itemId > 0 && record.context.HbA1c != 0 			 && --child == 0) return itemId;
		}
		throw new IllegalArgumentException("No item ID for position " + childPosition);
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
			view = inflater.inflate(R.layout.activity_feature_gls_subitem, parent, false);
			final ChildViewHolder holder = new ChildViewHolder();
			holder.title = view.findViewById(android.R.id.text1);
			holder.details = view.findViewById(android.R.id.text2);
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
