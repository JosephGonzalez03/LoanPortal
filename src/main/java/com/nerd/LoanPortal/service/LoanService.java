package com.nerd.LoanPortal.service;

import com.nerd.LoanPortal.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoanService {
    public PaymentSummary getTotalContributionsSummary(List<PaymentSummary> paymentSummaries) {
        List<PaymentReceipt> receipts = paymentSummaries.get(0).getPaymentReceipts();
        PaymentSummary totalContributionSummary = new PaymentSummary(receipts.size());

        // calculate total contribution for each loan
        for (int loanCounter=0; loanCounter < receipts.size(); loanCounter++) {
            PaymentReceipt currentReceipt = receipts.get(loanCounter);
            PaymentReceipt currentTotalContributionReceipt = totalContributionSummary.getPaymentReceipts().get(loanCounter);

            for (int summaryCounter=0; summaryCounter < paymentSummaries.size(); summaryCounter++) {
                PaymentSummary currentPaymentSummary = paymentSummaries.get(summaryCounter);
                PaymentReceipt currentPaymentSummaryReceipt = currentPaymentSummary.getPaymentReceipts().get(loanCounter);

                currentTotalContributionReceipt.setLoanName(currentReceipt.getLoanName());
                currentTotalContributionReceipt.setContribution(currentTotalContributionReceipt.getContribution().add(currentPaymentSummaryReceipt.getContribution()));
            }
        }

        return totalContributionSummary;
    }
}
