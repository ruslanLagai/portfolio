package com.home.project.portfolio.model.entity;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.operations.Status;
import lombok.*;
import ru.tinkoff.piapi.contract.v1.OperationType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Set;

@Entity(name = "OPERATION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationEntity {

    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "operation")
    private Set<Trade> trades;

    @Column(nullable = false, unique = true)
    private String operationId;
    private String accountId;
    private Status status;
    private Currency currency;
    private double payment;
    private double price;
    private double commission;
    private int quantity;
    private int quantityExecuted;
    private String figi;
    private String ticker;
    private InstrumentType instrumentType;
    private boolean isMarginCall;
    private ZonedDateTime date;
    @Enumerated(value = EnumType.STRING)
    private OperationType operationType;
}
