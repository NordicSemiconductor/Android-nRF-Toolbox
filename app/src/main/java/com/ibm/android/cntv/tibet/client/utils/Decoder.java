package com.ibm.android.cntv.tibet.client.utils;

/**
 * Created by yingkitw on 2016/9/30.
 */
public class Decoder {

    static long defSEC1989 =   631065600L;

    static int byteArrayToInt(byte[] b, int offset, int length) {
        int value= 0;
        for (int i = 0; i < length; i++) {
            if((i+offset)>=b.length) break;//TODOf
            int shift= (length - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public static void main(String[] args)
    {
        byte[] raw = new byte[]{(byte)0xAA, (byte)0xF9, (byte)0x03, (byte)0x80,
                (byte)0x86, (byte)0x50, (byte)0x32, (byte)0xFF,
                (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF,
                (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x9D};

        System.out.println(Decoder.decode(raw));
    }

    public static String decode(byte[] raw)
    {
        if (raw[2] == 0x01)
        {
            return decode1(raw);
        }
        else if (raw[2] == 0x02)
        {
            return decode2(raw);
        }
        else if (raw[2] == 0x03)
        {
            return decode3(raw);
        }

        return "not work " +raw.toString();
    }

    static String decode1(byte[] raw)
    {
        int cur_time = byteArrayToInt(raw,3,4);
        cur_time += defSEC1989;

        int moving_time = byteArrayToInt(raw,7,4);
        moving_time *= 100;

        int latitude = byteArrayToInt(raw,11,4);
        latitude *= 10000000;

        int longitude = byteArrayToInt(raw,15,4);
        longitude *= 10000000;

        return "{ \"d\": {"
                +"\"currenttime\":"+ Integer.toString(cur_time)+",\n"
                +"\"movingtime\":"+ Integer.toString(moving_time)+",\n"
                +"\"latitude\":"+ Integer.toString(latitude)+",\n"
                +"\"longtitude\":"+ Integer.toString(longitude)+"\n"
                +"} }";


    }

    static String decode2(byte[] raw)
    {
        int cur_time = byteArrayToInt(raw,3,4);
        cur_time += defSEC1989;

        int ride_dist = byteArrayToInt(raw,7,4);
        ride_dist *= 100;

        int altitude = byteArrayToInt(raw,11,2);

        int temperature = byteArrayToInt(raw,13,1);

        int pressure = byteArrayToInt(raw,14,4);

        return "{ \"d\": {"
                +"\"currenttime\":"+ Integer.toString(cur_time)+",\n"
                +"\"riderdistance\":"+ Integer.toString(ride_dist)+",\n"
                +"\"altitude\":"+ Integer.toString(altitude)+",\n"
                +"\"temperature\":"+ Integer.toString(temperature)+",\n"
                +"\"pressure\":"+ Integer.toString(pressure)+"\n"
                +"} }";

    }

    static String decode3(byte[] raw)
    {
        int cur_time = byteArrayToInt(raw,3,4);
        cur_time += defSEC1989;

        int cur_speed = byteArrayToInt(raw,7,2);
        cur_speed *= 1000;

        int avg_speed = byteArrayToInt(raw,9,2);
        avg_speed *= 1000;

        int max_speed = byteArrayToInt(raw,11,2);
        max_speed *= 1000;

        int cur_cad = byteArrayToInt(raw,13,1);

        int avg_cad = byteArrayToInt(raw,14,1);

        int max_cad = byteArrayToInt(raw,15,1);

        int cur_hrm = byteArrayToInt(raw,16,1);

        int avg_hrm = byteArrayToInt(raw,17,1);

        int max_hrm = byteArrayToInt(raw,18,1);

        return "{ \"d\": {"
                +"\"currenttime\":"+ Integer.toString(cur_time)+",\n"
                +"\"currentspeed\":"+ Integer.toString(cur_speed)+",\n"
                +"\"averagespeed\":"+ Integer.toString(avg_speed)+",\n"
                +"\"currentcad\":"+ Integer.toString(cur_cad)+",\n"
                +"\"averagecad\":"+ Integer.toString(avg_cad)+",\n"
                +"\"maxcad\":"+ Integer.toString(max_cad)+",\n"
                +"\"currenthrm\":"+ Integer.toString(cur_hrm)+",\n"
                +"\"averagehrm\":"+ Integer.toString(avg_hrm)+",\n"
                +"\"maxhrm\":"+ Integer.toString(max_hrm)+"\n"
                +"} }";

    }
}
