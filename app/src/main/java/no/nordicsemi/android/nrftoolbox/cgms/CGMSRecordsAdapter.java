package no.nordicsemi.android.nrftoolbox.cgms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.nrftoolbox.R;

public class CGMSRecordsAdapter extends BaseAdapter {
	private final static SimpleDateFormat mTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

	private List<CGMSRecord> mRecords;
	private LayoutInflater mInflater;

	public CGMSRecordsAdapter(final Context context) {
		mRecords = new ArrayList<>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mRecords.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_feature_cgms_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.concentration = convertView.findViewById(R.id.cgms_concentration);
			viewHolder.time = convertView.findViewById(R.id.time);
			viewHolder.details = convertView.findViewById(R.id.details);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final CGMSRecord cgmsRecord = mRecords.get(position);
		viewHolder.concentration.setText(String.valueOf(cgmsRecord.glucoseConcentration));
		viewHolder.details.setText(viewHolder.details.getResources().getString(R.string.cgms_details, cgmsRecord.sequenceNumber));
		viewHolder.time.setText(mTimeFormat.format(new Date(cgmsRecord.timestamp)));

		return convertView;
	}

	public void addItem(final CGMSRecord record) {
		mRecords.add(record);
	}

	public void clear() {
		mRecords.clear();
	}

	private static class ViewHolder {
		TextView time;
		TextView details;
		TextView concentration;
	}
}
