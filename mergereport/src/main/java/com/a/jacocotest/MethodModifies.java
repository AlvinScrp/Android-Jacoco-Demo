package com.a.jacocotest;

import android.util.Log;

public class MethodModifies {

    public static String a(){
        Log.d("alvin","run a 1");
        return "a";
    }

    public static String b2(){
        Log.d("alvin","run b 1");
        Log.d("alvin","run b 2");
        Log.d("alvin","run b 3");
        return "b";
    }

    public static String c(boolean onlyOne){

        Log.d("alvin","run c 1");
        try{
            int a= 10;
            a+=20;
            a/=0;
            a+=30;

        }catch (Exception e){
            e.printStackTrace();
        }

        if(!onlyOne){
            Log.d("alvin","run c 2");
        }
        return "c";
    }



}
