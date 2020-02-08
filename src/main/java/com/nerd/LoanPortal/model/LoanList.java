package com.nerd.LoanPortal.model;

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
}
