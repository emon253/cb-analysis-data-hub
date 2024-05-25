package org.carbarn.scrapify.autotrader.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class ProductFilterCriteria {
    private String makes;
    private String models;
    private Integer minYear;
    private Integer maxYear;
    private Integer year;
    private String colors;
    private String seats;
    private String fuelTypes;
    private String doors;
    private String bodyTypes;
    private String driveTrains;
    private Integer minOdometer;
    private Integer maxOdometer;
    private Double minPrice;
    private Double maxPrice;
    private String status;
    private String maxSoldDate;
    private String minSoldDate;
    private String maxListingDate;
    private String minListingDate;
    private String vinPrefix;
    private Long dealerId;
    private Boolean showListedDealersVehicles = Boolean.TRUE;


    // Method to convert a comma-separated string to a List
    private List<String> convertToList(String s) {
        return s != null ? Arrays.asList(s.split("\\s*,\\s*")) : new ArrayList<>();
    }

    // Method to convert a comma-separated string of integers to a List<Integer>
    private List<Integer> convertToIntList(String s) {
        if (s == null) return new ArrayList<>();
        return Arrays.stream(s.split("\\s*,\\s*"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    // Getters that parse the comma-separated strings into lists
    public List<String> getMakes() { return convertToList(makes); }
    public List<String> getModels() { return convertToList(models); }
    public List<String> getColors() { return convertToList(colors); }
    public List<Integer> getSeats() { return convertToIntList(seats); }
    public List<String> getFuelTypes() { return convertToList(fuelTypes); }
    public List<Integer> getDoors() { return convertToIntList(doors); }
    public List<String> getBodyTypes() { return convertToList(bodyTypes); }
    public List<String> getDriveTrains() { return convertToList(driveTrains); }
}
