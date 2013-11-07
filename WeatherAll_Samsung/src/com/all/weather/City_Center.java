package com.all.weather;

import android.provider.BaseColumns;

public final class City_Center {
    
    private String cityname;
    private String shengname;
    private String citycode;

    public static final class CityCenterColumns implements BaseColumns {
        private CityCenterColumns() {}
        public static final String CITY_NAME = "cityname";
        public static final String SHEN_GNAME = "shengname";
        public static final String CITY_CODE = "citycode";
    }

    public City_Center(String cityname, String shengname, String citycode) {
        super();
        this.cityname = cityname;
        this.shengname = shengname;
        this.citycode = citycode;
    }


    @Override
    public String toString() {
        return cityname;
//        if(getCity().equals("Xiangtan")){
//            return String.format("%s --- %s", getCity(), getState().replace("Sichuan", "Hunan"));
//        }
//        else{
//            return String.format("%s --- %s", getCity(), getState());
//        }
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getShengname() {
        return shengname;
    }

    public void setShengname(String shengname) {
        this.shengname = shengname;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }
    
}
