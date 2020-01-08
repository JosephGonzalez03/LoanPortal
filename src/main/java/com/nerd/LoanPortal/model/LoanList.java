package com.nerd.LoanPortal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter @Setter
public class LoanList implements Serializable {
    List<Loan> loans = new ArrayList<>();

    public LoanList(List<Loan> loans) {
        loans.forEach(loan -> this.loans.add(new Loan(loan.getId(), loan.getName(), loan.getInterestRate(), loan.getOutstandingBalance(), loan.getContribution())));
    }
}
