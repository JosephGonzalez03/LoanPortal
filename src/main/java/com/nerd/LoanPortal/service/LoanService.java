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
                oneTimeAdditionalContribution = loan.getOneTimeAdditionalContribution(),
                yearlyAccruedInterest = currentAmount.multiply(interestRate.movePointLeft(2)),
                monthlyAccruedInterest = yearlyAccruedInterest.divide(BigDecimal.valueOf(12), new MathContext(4)),
                loanAmountAfterPayment = currentAmount.add(monthlyAccruedInterest).subtract(contribution).subtract(oneTimeAdditionalContribution).setScale(2, RoundingMode.HALF_EVEN);

        loan.setOutstandingBalance(loanAmountAfterPayment);
        loan.setOneTimeAdditionalContribution(BigDecimal.ZERO);
    }

    private PaymentSummary makeMonthlyPayments(List<Loan> loans) {
        //LoanList loanListCopy = new LoanList(loans);
        PaymentSummary paymentSummary = new PaymentSummary();
        BigDecimal mPaidOffLoanContribution = BigDecimal.ZERO;
        BigDecimal oneTimeAdditionalContribution = BigDecimal.ZERO;

        for (Loan currentLoan : loans) {
            PaymentReceipt paymentReceipt = new PaymentReceipt();

            // make monthly payment on current loan if it is not paid off
            if (!currentLoan.isPaidOff()) {
                makeMonthlyPayment(currentLoan);
            }

            // if loan is paid off, but not declared as paid off
            if (currentLoan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0 && !currentLoan.isPaidOff()) {
                // declare loan as paid off
                currentLoan.setPaidOff(true);

                oneTimeAdditionalContribution = currentLoan.getOutstandingBalance().abs();

                // create final payment receipt
                paymentReceipt.setLoanName(currentLoan.getName());
                paymentReceipt.setOutstandingBalance(BigDecimal.ZERO);
                paymentReceipt.setContribution(currentLoan.getContribution().subtract(oneTimeAdditionalContribution));

                // find next highest priority loan
                for (Loan nextHighestPriorityLoan : loans) {
                    if (!nextHighestPriorityLoan.isPaidOff()) {
                        // add leftover contribution from paid off loan to one time additional contribution of next highest priority loan
                        nextHighestPriorityLoan.setOneTimeAdditionalContribution(oneTimeAdditionalContribution);

                        /* if the receipt was already created:
                        *   -update outstanding balance on the loan & receipt
                        *   -update contribution on next highest priority loan for next payment
                        * */
                        boolean isAlreadyCreated = false;

                        for (PaymentReceipt receipt: paymentSummary.getPaymentReceipts()) {
                            if (receipt.getLoanName().contentEquals(nextHighestPriorityLoan.getName())) {
                                isAlreadyCreated = true;

                                nextHighestPriorityLoan.setContribution(nextHighestPriorityLoan.getContribution().add(currentLoan.getContribution()));
                                receipt.setOutstandingBalance(nextHighestPriorityLoan.getOutstandingBalance());
                                receipt.setContribution(receipt.getContribution().add(oneTimeAdditionalContribution));
                                break;
                            }
                        }

                        if (!isAlreadyCreated) {
                            mPaidOffLoanContribution = currentLoan.getContribution();
                        }

                        break;
                    }
                }

                // set outstanding balance & contribution for paid off loan to zero
                currentLoan.setOutstandingBalance(BigDecimal.ZERO);
                currentLoan.setContribution(BigDecimal.ZERO);
            } else {
                // create loan payment receipt
                paymentReceipt.setLoanName(currentLoan.getName());
                paymentReceipt.setOutstandingBalance(currentLoan.getOutstandingBalance());
                paymentReceipt.setContribution(currentLoan.getContribution());

                // update contribution on next highest priority (current) loan for next payment & current receipt
                if (mPaidOffLoanContribution.compareTo(BigDecimal.ZERO) > 0) {
                    currentLoan.setContribution(currentLoan.getContribution().add(mPaidOffLoanContribution));
                    paymentReceipt.setContribution(paymentReceipt.getContribution().add(oneTimeAdditionalContribution));
                    mPaidOffLoanContribution = BigDecimal.ZERO;
                }

            }

            // add loan payment receipt to other receipts for the month
            paymentSummary.getPaymentReceipts().add(paymentReceipt);
        }
        return paymentSummary;
    }

    public PaymentSummaryList getPaymentSummaries(LoanList loanList) {
        boolean areAllLoansPaidOff = false;
        PaymentSummary paymentSummary;
        PaymentSummaryList paymentSummaries = new PaymentSummaryList();

        while (!areAllLoansPaidOff) {
            // make monthly loan payments
            paymentSummary = makeMonthlyPayments(loanList.getLoans());

            // add monthly loan payment receipts to list
            paymentSummaries.getPaymentSummaries().add(paymentSummary);

            // check if all loans are paid off
            areAllLoansPaidOff = true;

            for (Loan loan : loanList.getLoans()) {
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
