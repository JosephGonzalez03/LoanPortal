package com.nerd.LoanPortal.repository;

import com.nerd.LoanPortal.model.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Repository
public class LoanDao {

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
        PaymentSummary paymentSummary = new PaymentSummary();

        for (Loan currentLoan : loans) {
            // make monthly payment on current loan if it is not paid off
            if (currentLoan.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 1) {
                makeMonthlyPayment(currentLoan);
            }

            // handle when loan is paid off
            if (currentLoan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
                // move contribution and leftover pay to next loan
                for (Loan nextLoan : loans) {
                    if (!nextLoan.getName().contentEquals(currentLoan.getName()) && nextLoan.getContribution().compareTo(BigDecimal.ZERO) == 1) {
                        nextLoan.setOutstandingBalance(nextLoan.getOutstandingBalance().add(currentLoan.getOutstandingBalance()));
                        nextLoan.increaseContribution(currentLoan.getContribution());
                        break;
                    }
                }

                // zero out outstanding balance & contribution for paid off loan
                currentLoan.setOutstandingBalance(BigDecimal.ZERO);
                currentLoan.setContributionToZero();
            }

            // create loan payment receipt
            PaymentReceipt paymentReceipt = new PaymentReceipt();
            paymentReceipt.setLoanName(currentLoan.getName());
            paymentReceipt.setOutStandingBalance(currentLoan.getOutstandingBalance());
            paymentReceipt.increaseTotalContribution(currentLoan.getContribution());

            // add loan payment receipt to other receipts for the month
            paymentSummary.getPaymentReceipts().add(paymentReceipt);
        }

        return paymentSummary;
    }

    public PaymentSummaryList calculateLoanPayments(LoanList loanList) {
        LoanList loanListCopy = new LoanList(loanList.getLoans());

        boolean areAllLoansPaidOff = false;
        PaymentSummary paymentSummary;
        PaymentSummaryList paymentSummaries = new PaymentSummaryList();

        while (!areAllLoansPaidOff) {
            // make monthly loan payments
            paymentSummary = makeMonthlyPayments(loanListCopy.getLoans());

            // check if all loans are paid off
            areAllLoansPaidOff = true;

            for (Loan loan : loanListCopy.getLoans()) {
                if (loan.getContribution().compareTo(BigDecimal.ZERO) == 1) {
                    areAllLoansPaidOff = false;
                    break;
                }
            }

            // add monthly loan payment receipts to list
            paymentSummaries.getPaymentSummaries().add(paymentSummary);
        }

        return paymentSummaries;
    }
}
