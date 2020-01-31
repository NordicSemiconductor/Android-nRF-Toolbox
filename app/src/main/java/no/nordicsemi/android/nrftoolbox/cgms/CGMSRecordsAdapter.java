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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.nrftoolbox.R;

class CGMSRecordsAdapter extends BaseAdapter {
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

	private List<CGMSRecord> records;
	private LayoutInflater inflater;

	CGMSRecordsAdapter(@NonNull final Context context) {
		records = new ArrayList<>();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return records.size();
	}

	@Override
	public Object getItem(final int i) {
		return null;
	}

	@Override
	public long getItemId(final int i) {
		return i;
	}

	@Override
	public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
		ViewHolder viewHolder;
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.activity_feature_cgms_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.concentration = view.findViewById(R.id.cgms_concentration);
			viewHolder.time = view.findViewById(R.id.time);
			viewHolder.details = view.findViewById(R.id.details);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final CGMSRecord cgmsRecord = records.get(position);
		viewHolder.concentration.setText(String.valueOf(cgmsRecord.glucoseConcentration));
		viewHolder.details.setText(viewHolder.details.getResources().getString(R.string.cgms_details, cgmsRecord.sequenceNumber));
		viewHolder.time.setText(timeFormat.format(new Date(cgmsRecord.timestamp)));

		return view;
	}

	void addItem(final CGMSRecord record) {
		records.add(record);
	}

	void clear() {
		records.clear();
	}

	private static class ViewHolder {
		TextView time;
		TextView details;
		TextView concentration;
	}
}
