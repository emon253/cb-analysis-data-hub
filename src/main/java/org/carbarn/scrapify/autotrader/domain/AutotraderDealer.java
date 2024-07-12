package org.carbarn.scrapify.autotrader.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "autotrader_dealer")
@Data
public class AutotraderDealer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long autoTraderDealerId;

    @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<AutotraderCarListing> autotraderCarListings;


    @Column(name = "trading_name")
    private String tradingName;

    @Column(name = "phone_lead")
    private String phoneLead;

    private String city;

    private String state;

    private Boolean enableDataScraping;

}
