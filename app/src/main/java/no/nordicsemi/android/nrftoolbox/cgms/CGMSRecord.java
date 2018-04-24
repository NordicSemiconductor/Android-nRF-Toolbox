package no.nordicsemi.android.nrftoolbox.cgms;

import android.os.Parcel;
import android.os.Parcelable;

class CGMSRecord implements Parcelable{
    /** Record sequence number. */
    protected int sequenceNumber;
    /** The base time of the measurement (start time + sequenceNumber of minutes). */
    protected long timestamp;
    /** The glucose concentration in mg/dL. */
    protected float glucoseConcentration;

    CGMSRecord(final int sequenceNumber, final float glucoseConcentration, final long timestamp) {
        this.sequenceNumber = sequenceNumber;
        this.glucoseConcentration = glucoseConcentration;
        this.timestamp = timestamp;
    }

    private CGMSRecord(final Parcel in) {
        this.sequenceNumber = in.readInt();
        this.glucoseConcentration = in.readFloat();
        this.timestamp = in.readLong();
    }

    public static final Creator<CGMSRecord> CREATOR = new Creator<CGMSRecord>() {
        @Override
        public CGMSRecord createFromParcel(final Parcel in) {
            return new CGMSRecord(in);
        }

        @Override
        public CGMSRecord[] newArray(final int size) {
            return new CGMSRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeInt(sequenceNumber);
        parcel.writeFloat(glucoseConcentration);
        parcel.writeLong(timestamp);
    }
}
