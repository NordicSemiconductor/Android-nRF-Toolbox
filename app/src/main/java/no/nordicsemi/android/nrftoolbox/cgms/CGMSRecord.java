package no.nordicsemi.android.nrftoolbox.cgms;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by rora on 02.09.2016.
 */
public class CGMSRecord implements Parcelable{

    /** Record sequence number */
    protected int sequenceNumber;
    /** The base time of the measurement */
    protected Calendar time;
    /** Time offset of the record */
    protected String timeStamp;
    protected float reading;

    protected CGMSRecord(float cgmsValue, String timeStamp) {
        sequenceNumber = 0;
        this.timeStamp = timeStamp;
        this.reading = cgmsValue;
    }

    protected CGMSRecord(Parcel in) {
        sequenceNumber = in.readInt();
        timeStamp = in.readString();
        reading = in.readFloat();
    }

    public static final Creator<CGMSRecord> CREATOR = new Creator<CGMSRecord>() {
        @Override
        public CGMSRecord createFromParcel(Parcel in) {
            return new CGMSRecord(in);
        }

        @Override
        public CGMSRecord[] newArray(int size) {
            return new CGMSRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(sequenceNumber);
        parcel.writeString(timeStamp);
        parcel.writeFloat(reading);
    }
}
