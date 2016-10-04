package com.ibm.android.cntv.tibet.client.utils;

/**
 * Created by yingkitw on 2016/9/30.
 */
public class Decoder{
    static long defSEC1989 =   631065600L;

    static boolean is0xFF(byte[] b, int offset, int length) {
        if ((b[offset] & 0x000000FF) == 0xFF)
        {
            return true;
        }
        return false;
    }

    static int byteArrayToInt(byte[] b, int offset, int length) {
        int value= 0;
        for (int i = 0; i < length; i++) {
            int shift= (i) * 8;
            //  int shift= (length - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
            System.out.print((b[i + offset] & 0x000000FF)+"|");
        }
        System.out.println();
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
        StringBuilder ret = new StringBuilder();
        ret.append("{ \"d\": {\n");

        if(!is0xFF(raw,3,4)){
            int cur_time = byteArrayToInt(raw,3,4);
            cur_time += defSEC1989;
            ret.append("\"currenttime\":"+ Long.toString(cur_time & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,7,4)){
            int moving_time = byteArrayToInt(raw,7,4);
            moving_time *= 100;
            ret.append("\"movingtime\":"+ Long.toString(moving_time & 0xFFFFFFFFL)+", \n");
        }

        if(!is0xFF(raw,11,4)){
            int latitude = byteArrayToInt(raw,11,4);
            latitude *= 10000000;
            ret.append("\"latitude\":"+ Long.toString(latitude & 0xFFFFFFFFL)+", \n");
        }

        if(!is0xFF(raw,15,4)){
            int longitude = byteArrayToInt(raw,15,4);
            longitude *= 10000000;
            ret.append("\"longtitude\":"+ Long.toString(longitude & 0xFFFFFFFFL)+", \n");
        }

        ret.append("} }");
        return ret.toString().replace(", \n} }"," \n} }");
    }

    static String decode2(byte[] raw)
    {
        StringBuilder ret = new StringBuilder();
        ret.append("{ \"d\": {");

        if(!is0xFF(raw,3,4)){
            int cur_time = byteArrayToInt(raw,3,4);
            cur_time += defSEC1989;
            ret.append("\"currenttime\":"+ Long.toString(cur_time & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,7,4)){
            int ride_dist = byteArrayToInt(raw,7,4);
            ride_dist *= 100;
            ret.append("\"riderdistance\":"+ Long.toString(ride_dist & 0xFFFFFFFFL)+", \n");
        }

        if(!is0xFF(raw,11,2)){
            int altitude = byteArrayToInt(raw,11,2);
            ret.append("\"altitude\":"+ Long.toString(altitude & 0xFFFFFFFFL)+", \n");
        }

        if(!is0xFF(raw,13,1)){
            int temperature = byteArrayToInt(raw,13,1);
            ret.append("\"temperature\":"+ Long.toString(temperature & 0xFFFFFFFFL)+", \n");
        }

        if(!is0xFF(raw,14,4)){
            int pressure = byteArrayToInt(raw,14,4);
            ret.append("\"pressure\":"+ Long.toString(pressure & 0xFFFFFFFFL)+", \n");
        }

        ret.append("} }");
        return ret.toString().replace(", \n} }"," \n} }");

    }

    static String decode3(byte[] raw)
    {
        StringBuilder ret = new StringBuilder();
        ret.append("{ \"d\": {");

        if(!is0xFF(raw,3,4)){
            int cur_time = byteArrayToInt(raw,3,4);
            cur_time += defSEC1989;

            ret.append("\"currenttime\":"+ Long.toString(cur_time & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,7,2)){
            int cur_speed = byteArrayToInt(raw,7,2);
            cur_speed *= 1000;

            ret.append("\"currentspeed\":"+ Long.toString(cur_speed & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,9,2)){
            int avg_speed = byteArrayToInt(raw,9,2);
            avg_speed *= 1000;

            ret.append("\"averagespeed\":"+ Long.toString(avg_speed & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,11,2)){
            int max_speed = byteArrayToInt(raw,11,2);
            max_speed *= 1000;

            ret.append("\"maxspeed\":"+ Long.toString(max_speed & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,13,2)){
            int cur_cad = byteArrayToInt(raw,13,1);

            ret.append("\"currentcad\":"+ Long.toString(cur_cad & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,14,1)){
            int avg_cad = byteArrayToInt(raw,14,1);

            ret.append("\"averagecad\":"+ Long.toString(avg_cad & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,15,1)){
            int max_cad = byteArrayToInt(raw,15,1);

            ret.append("\"maxcad\":"+ Long.toString(max_cad & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,16,1)){
            int cur_hrm = byteArrayToInt(raw,16,1);

            ret.append("\"currenthrm\":"+ Long.toString(cur_hrm & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,17,1)){
            int avg_hrm = byteArrayToInt(raw,17,1);

            ret.append("\"averagehrm\":"+ Long.toString(avg_hrm & 0x000000FF)+", \n");
        }

        if(!is0xFF(raw,18,1)){
            int max_hrm = byteArrayToInt(raw,18,1);

            ret.append("\"maxhrm\":"+ Long.toString(max_hrm & 0x000000FF)+", \n");
        }


        ret.append("} }");

        return ret.toString().replace(", \n} }"," \n} }");
    }
}