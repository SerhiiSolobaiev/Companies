package com.myandroid.companiestest.entity;

public class CompanyAddress {
    private int id;
    private String address;
    private long idCompany;
    private double latitude;
    private double longitude;

    public CompanyAddress(int id, String address, long idCompany, double latitude, double longitude) {
        this.id = id;
        this.address = address;
        this.idCompany = idCompany;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CompanyAddress(String address, long idCompany, double latitude, double longitude) {
        this.address = address;
        this.idCompany = idCompany;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(long idCompany) {
        this.idCompany = idCompany;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "CompanyAddress{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", idCompany=" + idCompany +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
