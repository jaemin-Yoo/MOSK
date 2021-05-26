package com.example.mosk;

public class HospitalViewItem {

    private String name;
    private String phnum;
    private String adress;

    public void setName(String strtitle){
        name=strtitle;
    }
    public void setPhnum(String strphnum){ phnum=strphnum; }
    public void setAdress(String stradress){
        adress=stradress;
    }

    public String getName(){
        return this.name;
    }
    public String getPhnum(){
        return this.phnum;
    }
    public String getAdress(){
        return this.adress;
    }

}
