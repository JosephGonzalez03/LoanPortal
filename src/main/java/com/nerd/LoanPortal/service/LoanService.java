package com.nerd.LoanPortal.service;

import com.nerd.LoanPortal.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoanService {

    private void makeMonthlyPayment(Loan loan) {
        BigDecimal currentAmount = loan.getOutstandingBalance(),
                interestRate = loan.getInterestRate(),
                contribution = loan.getContribution(),
                yearlyAccruedInterest = currentAmount.multiply(interestRate.movePointLeft(2)),
                monthlyAccruedInterest = yearlyAccruedInterest.divide(BigDecimal.valueOf(12), new MathContext(2)),
                loanAmountAfterPayment = currentAmount.add(monthlyAccruedInterest).subtract(contribution).setScale(2, RoundingMode.HALF_EVEN);

        loan.setOutstandingBalance(loanAmountAfterPayment);
    }

    private PaymentSummary makeMonthlyPayments(List<Loan> loans) {
        BigDecimal oneTimeAdditionalContribution = BigDecimal.ZERO;
        String nameOfNextLoan = new String();
        PaymentSummary paymentSummary = new PaymentSummary();

        for (Loan currentLoan : loans) {
            PaymentReceipt paymentReceipt = new PaymentReceipt();

            // make monthly payment on current loan if it is not paid off
            if (currentLoan.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 1) {
                makeMonthlyPayment(currentLoan);
            }

            // if loan is paid off
            if (currentLoan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
                // save leftover contribution from paid off loan
                oneTimeAdditionalContribution = currentLoan.getOutstandingBalance().abs();

                // move contribution and leftover pay to next loan
                for (Loan nextLoan : loans) {
                    if (!nextLoan.getName().contentEquals(currentLoan.getName()) && nextLoan.getContribution().compareTo(BigDecimal.ZERO) == 1) {
                        nameOfNextLoan = nextLoan.getName();
                        nextLoan.setOutstandingBalance(nextLoan.getOutstandingBalance().subtract(oneTimeAdditionalContribution));
                        nextLoan.increaseContribution(currentLoan.getContribution());
                        break;
                    }
                }

                // create loan payment receipt
                BigDecimal finalPaymentAmount = currentLoan.getContribution().add(currentLoan.getOutstandingBalance());

                paymentReceipt.setLoanName(currentLoan.getName());
                paymentReceipt.setOutStandingBalance(BigDecimal.ZERO);
                paymentReceipt.setContribution(finalPaymentAmount);

                // zero out outstanding balance & contribution for paid off loan
                currentLoan.setOutstandingBalance(BigDecimal.ZERO);
                currentLoan.setContributionToZero();
            } else {
                BigDecimal fullContribution;

                // use leftover contribution from paid off loan for next highest priority loan
                if (currentLoan.getName().contentEquals(nameOfNextLoan)) {
                    fullContribution = currentLoan.getContribution().add(oneTimeAdditionalContribution);
                } else {
                    fullContribution = currentLoan.getContribution();
                }

                // create loan payment receipt
                paymentReceipt.setLoanName(currentLoan.getName());
                paymentReceipt.setOutStandingBalance(currentLoan.getOutstandingBalance());
                paymentReceipt.setContribution(fullContribution);
            }


            // add loan payment receipt to other receipts for the month
            paymentSummary.getPaymentReceipts().add(paymentReceipt);
        }

        return paymentSummary;
    }

    public PaymentSummaryList getPaymentSummaries(LoanList loanList) {
        LoanList loanListCopy = new LoanList(loanList.getLoans());

        boolean areAllLoansPaidOff = false;
        PaymentSummary paymentSummary;
        PaymentSummaryList paymentSummaries = new PaymentSummaryList();

        while (!areAllLoansPaidOff) {
            // make monthly loan payments
            paymentSummary = makeMonthlyPayments(loanListCopy.getLoans());

            // add monthly loan payment receipts to list
            paymentSummaries.getPaymentSummaries().add(paymentSummary);

            // check if all loans are paid off
            areAllLoansPaidOff = true;

            for (Loan loan : loanListCopy.getLoans()) {
                if (loan.getContribution().compareTo(BigDecimal.ZERO) == 1) {
                    areAllLoansPaidOff = false;
                    break;
                }
            }
        }

        return paymentSummaries;
    }

    public PaymentSummary getTotalContributionsSummary(PaymentSummaryList paymentSummaryList) {
        List<PaymentReceipt> receipts = paymentSummaryList.getPaymentSummaries().get(0).getPaymentReceipts();
        PaymentSummary totalContributionSummary = new PaymentSummary(receipts.size());

        // calculate total contribution for each loan
        for (int loanCounter=0; loanCounter < receipts.size(); loanCounter++) {
            PaymentReceipt currentReceipt = receipts.get(loanCounter);
            PaymentReceipt currentTotalContributionReceipt = totalContributionSummary.getPaymentReceipts().get(loanCounter);

            for (int summaryCounter=0; summaryCounter < paymentSummaryList.getPaymentSummaries().size(); summaryCounter++) {
                PaymentSummary currentPaymentSummary = paymentSummaryList.getPaymentSummaries().get(summaryCounter);
                PaymentReceipt currentPaymentSummaryReceipt = currentPaymentSummary.getPaymentReceipts().get(loanCounter);

                currentTotalContributionReceipt.setLoanName(currentReceipt.getLoanName());
                currentTotalContributionReceipt.setContribution(currentTotalContributionReceipt.getContribution().add(currentPaymentSummaryReceipt.getContribution()));
            }
        }

        return totalContributionSummary;
    }
}
