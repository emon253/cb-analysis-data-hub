package org.carbarn.scrapify.carvx.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class CarvxModelCodeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String searchValue;
    private String chassis;
    private String body;
    private String model;
    private String engine;
    private String grade;
    private String drive;
    private String year;
    private String transmission;
    private String fuel;
}
