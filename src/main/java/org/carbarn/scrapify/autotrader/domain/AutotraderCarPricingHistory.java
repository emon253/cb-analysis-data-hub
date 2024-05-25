package org.carbarn.scrapify.autotrader.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "autotrader_car_pricing_history")
@Data
public class AutotraderCarPricingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_listing_id")
    private AutotraderCarListing autotraderCarListing;

    @Column(name = "advertised_price")
    private int advertisedPrice;

    @Column(name = "egc_price")
    private int egcPrice;

    @Column(name = "driveaway_price")
    private int driveawayPrice;

    @Column(name = "is_fixed_price")
    private int isFixedPrice;

    @Column(name = "deleted_at")
    private String deletedAt;
}
