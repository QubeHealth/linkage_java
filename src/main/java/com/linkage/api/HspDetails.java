package com.linkage.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HspDetails {
    @JsonProperty("name")
    private String name;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("country")
    private String country;
    @JsonProperty("types")
    private List<String> types; 

    // Updated constructor including pincode, state, and county
    public HspDetails(String name, String phoneNumber, String address, Double latitude, Double longitude, String pincode, String city, String state, String country,List<String>types) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pincode = pincode;
        this.city = city;
        this.state = state;
        this.country = country;
        this.types=types;
    }

    // Getters and Setters (if needed)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public void setTypes( List<String> types) {
        this.types = types;
    }
    public List<String> getTypes() {
       return types;
    }
    
}