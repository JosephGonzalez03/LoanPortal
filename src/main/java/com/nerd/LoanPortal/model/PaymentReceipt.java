package com.nerd.LoanPortal.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
@Component
public class PaymentReceipt implements Serializable {
    String loanName;

    @Digits(integer=10, fraction=2)
    BigDecimal outStandingBalance;

    @Digits(integer=10, fraction=2)
    @Setter(AccessLevel.NONE)
    BigDecimal totalContribution = BigDecimal.ZERO;

    public void increaseTotalContribution(BigDecimal amount) {
        totalContribution = totalContribution.add(amount);
    }
}
