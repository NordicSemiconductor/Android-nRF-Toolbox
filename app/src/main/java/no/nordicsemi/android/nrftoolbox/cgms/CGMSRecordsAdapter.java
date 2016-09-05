package no.nordicsemi.android.nrftoolbox.cgms;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by rora on 02.09.2016.
 */
public class CGMSRecordsAdapter extends BaseAdapter {

    private SparseArray<CGMSRecord> mRecords;
    private LayoutInflater mInflator;
    private Context context;

    public CGMSRecordsAdapter(Context context) {
        super();
        mRecords = new SparseArray<>();
        this.context = context;
        mInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = mInflator.inflate(R.layout.activity_feature_cgms_item, null);
            viewHolder = new ViewHolder();
            viewHolder.concentration = (TextView) convertView.findViewById(R.id.cgms_concentration);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.details = (TextView) convertView.findViewById(R.id.details);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CGMSRecord cgmsRecord = mRecords.get(position);
        final String concentration = String.valueOf(cgmsRecord.reading);

        if (concentration != null && concentration.length() > 0) {
            viewHolder.concentration.setText(concentration /*+ " " + context.getString(R.string.cgms_value_unit*/);
            viewHolder.time.setText(cgmsRecord.timeStamp);
        }

        return convertView;
    }

    public void addItem(CGMSRecord record) {
        mRecords.put(mRecords.size(), record);
    }

    public SparseArray<CGMSRecord> getValues() {
        return mRecords;
    }

    public void clear() {
        mRecords.clear();
    }

    static class ViewHolder {
        TextView time;
        TextView details;
        TextView concentration;
    }
}
