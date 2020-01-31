package no.nordicsemi.android.nrftoolbox.cgm;

import android.os.Parcel;
import android.os.Parcelable;

class CGMRecord implements Parcelable{
    /** Record sequence number. */
    int sequenceNumber;
    /** The base time of the measurement (start time + sequenceNumber of minutes). */
    long timestamp;
    /** The glucose concentration in mg/dL. */
    float glucoseConcentration;

    CGMRecord(final int sequenceNumber, final float glucoseConcentration, final long timestamp) {
        this.sequenceNumber = sequenceNumber;
        this.glucoseConcentration = glucoseConcentration;
        this.timestamp = timestamp;
    }

    private CGMRecord(final Parcel in) {
        this.sequenceNumber = in.readInt();
        this.glucoseConcentration = in.readFloat();
        this.timestamp = in.readLong();
    }

    public static final Creator<CGMRecord> CREATOR = new Creator<CGMRecord>() {
        @Override
        public CGMRecord createFromParcel(final Parcel in) {
            return new CGMRecord(in);
        }

        @Override
        public CGMRecord[] newArray(final int size) {
            return new CGMRecord[size];
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
