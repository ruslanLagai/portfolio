package com.home.project.portfolio.model.entity;

import com.home.project.portfolio.model.InstrumentType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "ENTITY_METADATA")
@Getter
@Setter
public class StockMetadata {

    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;

    private String figi;
    private String ticker;
    private String isin;
    private InstrumentType instrumentType;
    private String name;

}
