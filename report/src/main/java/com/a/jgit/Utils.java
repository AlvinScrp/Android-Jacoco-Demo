package com.a.jgit;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Utils {

    public static String MD5Encode(String s) {
        String MD5String = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            MD5String = Base64.getEncoder().encodeToString(md5.digest(s.getBytes("utf-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return MD5String;
    }
}
