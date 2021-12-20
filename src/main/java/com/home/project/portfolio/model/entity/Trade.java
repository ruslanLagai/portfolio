package com.home.project.portfolio.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity(name = "TRADE")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_Id")
    private OperationEntity operation;

    private long operationId;
    private String tradeId;
    private ZonedDateTime date;
    private int quantity;
    private double price;
}
