package com.myandroid.companiestest.entity;

import java.util.List;

public class Company {
    private int id;
    private String name;
    private String description;
    private List<CompanyAddress> addresses;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CompanyAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<CompanyAddress> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", addresses=" + addresses +
                '}';
    }
}
