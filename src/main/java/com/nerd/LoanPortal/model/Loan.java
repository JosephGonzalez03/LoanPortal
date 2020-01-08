package com.nerd.LoanPortal.model;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
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

    public void increaseContribution(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) == 1) {
            contribution = contribution.add(amount);
        }
    }
    public void setContributionToZero() {
        contribution = BigDecimal.ZERO;
    }
}