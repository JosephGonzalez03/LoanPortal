package com.nerd.LoanPortal.controller;

import com.nerd.LoanPortal.configuration.RestTemplateConfiguration;
import com.nerd.LoanPortal.configuration.HttpProperties;
import com.nerd.LoanPortal.model.*;
import com.nerd.LoanPortal.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class LoanPortalController {

    @Autowired
    private RestTemplateConfiguration restTemplateConfiguration;

    @Autowired
    private LoanService loanService;

    @GetMapping("/loans")
    public String orderedLoans(RedirectAttributes redirectAttributes, @RequestParam(defaultValue = "NAME") String orderBy) {
        HttpProperties properties = restTemplateConfiguration.loanSystemApiProperties();
        RestTemplate loanSystemApi = restTemplateConfiguration.restTemplate(properties);
        ResponseEntity<Loan[]> orderedLoans = loanSystemApi.getForEntity("/users/1/loans?orderBy={orderBy}", Loan[].class, orderBy);

        redirectAttributes.addFlashAttribute("raPaymentSummaries", new ArrayList<PaymentSummary>());
        redirectAttributes.addFlashAttribute("raLoans", Arrays.asList(orderedLoans.getBody()));
        return "redirect:/loans/all";
    }

    @GetMapping("/loans/all")
    public String main(Model model, @ModelAttribute("raLoans") List<Loan> loans, @ModelAttribute("raPaymentSummaries") List<PaymentSummary> paymentSummaries) {
        model.addAttribute("loans", loans);
        model.addAttribute("paymentSummaryList", paymentSummaries);
        return "loansTable";
    }


    @GetMapping("/loans/new")
    public String newLoansForm(Model model) {
        Loan newLoan = new Loan();

        model.addAttribute("newLoan", newLoan);
        return "newLoansForm";
    }

    @PostMapping("/loans/new/save")
    public String saveNewLoans(@ModelAttribute Loan newLoan) {
        HttpProperties properties = restTemplateConfiguration.loanSystemApiProperties();
        RestTemplate loanSystemApi = restTemplateConfiguration.restTemplate(properties);
        loanSystemApi.postForObject("/users/1/loans", newLoan, Loan.class);
        return "redirect:/loans";
    }

    @GetMapping("/loans/edit")
    public String editLoansForm(Model model) {
        HttpProperties properties = restTemplateConfiguration.loanSystemApiProperties();
        RestTemplate loanSystemApi = restTemplateConfiguration.restTemplate(properties);
        ResponseEntity<Loan[]> orderedLoans = loanSystemApi.getForEntity("/users/1/loans?orderBy={orderBy}", Loan[].class, "NAME");

        model.addAttribute("editedLoans", Arrays.asList(orderedLoans.getBody()));
        model.addAttribute("loan", new Loan());
        return "editLoansForm";
    }

    @PostMapping("/loans/edit/save")
    public String saveEditedLoans(@ModelAttribute LoanForm editedLoans) {
        HttpProperties properties = restTemplateConfiguration.loanSystemApiProperties();
        RestTemplate loanSystemApi = restTemplateConfiguration.restTemplate(properties);

        for (int i=0; i<editedLoans.getLoans().size(); i++) {
            Loan currentLoan = editedLoans.getLoans().get(i);
            loanSystemApi.put("/users/1/loans/" + currentLoan.getId(), currentLoan);
        }

        return "redirect:/loans";
    }

    @GetMapping("/loans/calculate")
    public String calculateLoanPayments(Model model, @RequestParam(defaultValue = "NAME") String orderBy) {
        // create loan system api rest template
        HttpProperties properties = restTemplateConfiguration.loanSystemApiProperties();
        RestTemplate loanSystemApi = restTemplateConfiguration.restTemplate(properties);

        // create payment process api rest template
        properties = restTemplateConfiguration.paymentProcessApiProperties();
        RestTemplate paymentProcessApi = restTemplateConfiguration.restTemplate(properties);

        // request ordered loans
        ResponseEntity<Loan[]> orderedLoans = loanSystemApi.getForEntity("/users/1/loans?orderBy={orderBy}", Loan[].class, orderBy);
        ResponseEntity<Loan[]> orderedAlphabeticallyLoans = loanSystemApi.getForEntity("/users/1/loans?orderBy={orderBy}", Loan[].class, "NAME");

        // request payment summaries
        ResponseEntity<PaymentSummary[]> paymentSummaries = paymentProcessApi.getForEntity("/users/1/paymentSummaries?operation=FORECAST&orderBy={orderBy}", PaymentSummary[].class, orderBy);

        // populate loans table
        model.addAttribute("loans", Arrays.asList(orderedAlphabeticallyLoans.getBody()));

        // populate payment summary table
        model.addAttribute("paymentSummaries", Arrays.asList(paymentSummaries.getBody()));

        // populate total contributions summary
        model.addAttribute("totalContributionsSummary", loanService.getTotalContributionsSummary(Arrays.asList(paymentSummaries.getBody())));

        return "loansTable";
    }
}
