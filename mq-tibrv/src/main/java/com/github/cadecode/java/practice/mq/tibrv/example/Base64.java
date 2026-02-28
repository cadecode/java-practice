package com.github.cadecode.java.practice.mq.tibrv.example;/*
 * Copyright (c) 1998-$Date: 2013-12-20 07:48:17 -0800 (Fri, 20 Dec 2013) $ TIBCO Software Inc.
 * All rights reserved.
 * TIB/Rendezvous is protected under US Patent No. 5,187,787.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 */

public class Base64 {

    private static final String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    private static final char[] alphabet = base64.toCharArray();

    static private final byte[] codes = new byte[256];

    static {
        for (int i = 0; i < 256; i++)
            codes[i] = -1;
        for (int i = 'A'; i <= 'Z'; i++)
            codes[i] = (byte) (i - 'A');
        for (int i = 'a'; i <= 'z'; i++)
            codes[i] = (byte) (26 + i - 'a');
        for (int i = '0'; i <= '9'; i++)
            codes[i] = (byte) (52 + i - '0');
        codes['+'] = 62;
        codes['/'] = 63;
    }


    public static String base64Encode(byte[] data) {

        char[] out = new char[((data.length + 2) / 3) * 4];

        for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & (int) data[i]);
            val <<= 8;
            if ((i + 1) < data.length) {
                val |= (0xFF & (int) data[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < data.length) {
                val |= (0xFF & (int) data[i + 2]);
                quad = true;
            }
            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index] = alphabet[val & 0x3F];
        }

        return new String(out);
    }

    public static byte[] base64Decode(String input) {

        char[] data = input.toCharArray();
        int tempLen = data.length;

        for (int ix = 0; ix < data.length; ix++) {
            if ((data[ix] > 255)
                    || codes[data[ix]] < 0)
                --tempLen;
        }

        int len = (tempLen / 4) * 3;
        if ((tempLen % 4) == 3)
            len += 2;
        if ((tempLen % 4) == 2)
            len += 1;

        byte[] out = new byte[len];

        int shift = 0;
        int accum = 0;
        int index = 0;

        for (int ix = 0; ix < data.length; ix++) {
            int value = (data[ix] > 255) ? -1 : codes[data[ix]];

            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;

                if (shift >= 8) {
                    shift -= 8;
                    out[index++] = (byte) ((accum >> shift) & 0xff);
                }
            }
        }

        if (index != out.length) {
            throw new Error("Miscalculated data length (wrote "
                    + index
                    + " instead of "
                    + out.length
                    + ")");
        }

        return out;
    }

    public static String base64Encode(String str) {

        if (str == null)
            return null;

        int i = 0, insize = str.length();
        StringBuffer buffer = new StringBuffer();

        for (i = 0; i < insize - 2; i += 3) {
            buffer.append(base64.charAt((str.charAt(i) >> 2) & 0x3F));
            buffer.append(base64.charAt(((str.charAt(i) & 0x3) << 4)
                    | ((str.charAt(i + 1) & 0xF0) >> 4)));
            buffer.append(base64.charAt(((str.charAt(i + 1) & 0x0F) << 2)
                    | ((str.charAt(i + 2) & 0xC0) >> 6)));
            buffer.append(base64.charAt(str.charAt(i + 2) & 0x3F));
        }

        if (i < insize) {
            buffer.append(base64.charAt((str.charAt(i) >> 2) & 0x3F));

            if (i == (insize - 1)) {
                buffer.append(base64.charAt(((str.charAt(i) & 0x3) << 4)));
                buffer.append('=');
            } else {
                buffer.append(base64.charAt(((str.charAt(i) & 0x3) << 4)
                        | ((str.charAt(i + 1) & 0xF0) >> 4)));
                buffer.append(base64.charAt(((str.charAt(i + 1) & 0xF) << 2)));
            }

            buffer.append('=');
        }

        return new String(buffer);
    }

}
