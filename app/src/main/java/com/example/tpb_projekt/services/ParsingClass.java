package com.example.tpb_projekt.services;

public class ParsingClass {
    public String SingleStringClear(String str){
        String data =str;
        data = data.replace(" ", "");
        data = data.replace("\n", "");
        data = data.replace("]", "");
        data = data.replace("[", "");
        return data;
    }
    public String SingleStringClearWithArray(String str){
        String data =str;
        data = data.replace(" ", "");
        data = data.replace("\n", "");
        data = data.substring(3);
        data = data.substring(0, data.length()-3);
        return data;
    }
}
