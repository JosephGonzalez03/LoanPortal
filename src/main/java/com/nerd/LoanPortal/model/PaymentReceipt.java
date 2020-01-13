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
@Component
public class PaymentReceipt implements Serializable {
    String loanName;

    @Digits(integer=10, fraction=2)
    BigDecimal outStandingBalance;

    @Digits(integer=10, fraction=2)
    BigDecimal contribution;
}
