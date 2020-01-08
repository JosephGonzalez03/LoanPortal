package com.nerd.LoanPortal.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class PaymentSummaryList implements Serializable {
    List<PaymentSummary> paymentSummaries;

    public PaymentSummaryList() {
        paymentSummaries = new ArrayList<>();
    }
}
