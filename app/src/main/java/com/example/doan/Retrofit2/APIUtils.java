package com.example.doan.Retrofit2;

public class APIUtils {
    public static  final  String baseUrl = "http://192.168.1.39:8080/Mobile_App/";
    public  static  DataCilent getData(){
        return  RetrofitCilent.getCilent(baseUrl).create(DataCilent.class);
    }
}
