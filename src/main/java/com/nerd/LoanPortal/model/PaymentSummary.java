package com.nerd.LoanPortal.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class PaymentSummary implements Serializable {
    List<PaymentReceipt> paymentReceipts;

    public PaymentSummary() {
        paymentReceipts = new ArrayList<>();
    }
}
