package com.nerd.LoanPortal.controller;

import com.nerd.LoanPortal.model.*;
import com.nerd.LoanPortal.repository.LoanDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class LoanPortalController {

    @Autowired
    private RestTemplateConfiguration restTemplateConfiguration;

    @Autowired
    private LoanDao loanDao;

    @GetMapping("/loans/all")
    public String main(Model model) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        LoanList loanList = loanRT.getForObject("/users/1/loans?orderBy={orderBy}", LoanList.class, "default");

        model.addAttribute("loans", loanList.getLoans());
        model.addAttribute("paymentSummaryList", new PaymentSummaryList());
        return "loansTable";
    }

    @GetMapping("/loans/new")
    public String newLoansForm(Model model) {
        Loan newLoan = new Loan();

        model.addAttribute("loanForm", newLoan);
        return "newLoansForm";
    }

    @PostMapping("/loans/new/save")
    public String saveNewLoans(@ModelAttribute Loan loanForm) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        loanRT.postForObject("/users/1/loans", loanForm, Loan.class);
        return "redirect:/loans/all";
    }

    @GetMapping("/loans/edit")
    public String editLoansForm(Model model) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        LoanList loanList = loanRT.getForObject("/users/1/loans?orderBy={orderBy}", LoanList.class, "default");

        model.addAttribute("loansForm", loanList);
        model.addAttribute("loan", new Loan());
        return "editLoansForm";
    }

    @PostMapping("/loans/edit/save")
    public String saveEditedLoans(@ModelAttribute LoanList loanList) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        List<Loan> loans = loanList.getLoans();

        for (int i=0; i<loans.size(); i++) {
            Loan currentLoan = loans.get(i);
            loanRT.put("/users/1/loans/" + currentLoan.getId(), currentLoan);
        }

        return "redirect:/loans/all";
    }

    @GetMapping("/loans/calculate")
    public String calculateLoanPayments(Model model) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        LoanList loanList = loanRT.getForObject("/users/1/loans?orderBy={orderBy}", LoanList.class, "interest_rate");

        // populate loans table
        model.addAttribute("loans", loanList.getLoans());

        // populate payment summary table
        model.addAttribute("paymentSummaryList", loanDao.calculateLoanPayments(loanList));

        return "loansTable";
    }
}
