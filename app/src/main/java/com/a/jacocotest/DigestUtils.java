package com.a.jacocotest;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class DigestUtils {

    public static String md5(File file) {
        FileInputStream fis = null;
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] b = md.digest();
            char[] chars = encodeHex(b);
            return new String(chars);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return "";
    }

    static char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    private static char[] encodeHex(final byte[] data) {
        final int dataLength = data.length;
        final char[] out = new char[dataLength << 1];
        for (int i = 0, j = 0; i < dataLength; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return out;
    }

}
