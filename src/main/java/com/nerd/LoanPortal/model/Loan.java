package com.nerd.LoanPortal.model;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Component
public class Loan implements Serializable {

    @EqualsAndHashCode.Include
    private Integer id;

    private String name;

    @Digits(integer=3, fraction=3)
    private BigDecimal interestRate;

    @Digits(integer=10, fraction=2)
    private BigDecimal outstandingBalance;

    @Digits(integer=10, fraction=2)
    private BigDecimal contribution;

    @Digits(integer=10, fraction=2)
    private BigDecimal oneTimeAdditionalContribution;

    private boolean isPaidOff;

    public Loan() {
        this.oneTimeAdditionalContribution = BigDecimal.ZERO;
    }

    public void increaseContribution(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) == 1) {
            contribution = contribution.add(amount);
        }
    }
}