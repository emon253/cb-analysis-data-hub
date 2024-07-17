package org.carbarn.scrapify.autotrader.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.carbarn.scrapify.util.BaseEntity;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "autotrader_vehicle")
@Data
public class AutotraderCarListing extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long autoTraderId;

    private Long autoTraderDealerId;

    private String source;
    private Integer sourceId;
    private String vin;
    private String stockNo;

    @Column(name = "manu_year")
    private int manufactureYear;

    @Column(name = "colour_body")
    private String colourBody;
    private Integer odometer;
    private String rego;

    @Column(name = "rego_expiry")
    private String regoExpiry;
    private String make;
    private String model;
    private String variant;
    private String series;
    private String modelCode;
    private String chassis;
    private String status;
    private String soldDate;
    private String url;
    private String lat;
    private String lon;
    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;
    @Column(name = "deleted_at")
    private String deletedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "autotraderCarListing", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AutotraderCarPricingHistory> pricingHistory;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dealer_id")
    @JsonIgnoreProperties("autotraderCarListings")
    private AutotraderDealer dealer;

}
