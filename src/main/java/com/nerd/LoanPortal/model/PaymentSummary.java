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

    public PaymentSummary(List<PaymentReceipt> paymentReceipts) {
        paymentReceipts.forEach(paymentReceipt -> this.paymentReceipts.add(new PaymentReceipt(paymentReceipt.getLoanName(), paymentReceipt.getOutStandingBalance(), paymentReceipt.getContribution())));
    }

    public PaymentSummary(Integer numberOfReceipts) {
        paymentReceipts = new ArrayList<>();

        for (int i=0; i<numberOfReceipts; i++) {
            paymentReceipts.add(new PaymentReceipt());
        }
    }
}
