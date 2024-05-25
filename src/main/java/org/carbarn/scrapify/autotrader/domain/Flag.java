package org.carbarn.scrapify.autotrader.domain;

import jakarta.persistence.*;
import lombok.*;
import org.carbarn.scrapify.util.BaseEntity;

@Entity
@Table(name = "flag_data")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Flag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String value;

    public Flag(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
