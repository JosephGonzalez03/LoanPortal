package com.nerd.LoanPortal.controller;

import com.nerd.LoanPortal.model.*;
import com.nerd.LoanPortal.repository.LoanDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class LoanPortalController {

    @Autowired
    private RestTemplateConfiguration restTemplateConfiguration;

    @Autowired
    private LoanDao loanDao;

    @GetMapping("/loans")
    public String orderedLoans(RedirectAttributes redirectAttributes, @RequestParam(defaultValue = "default") String orderBy) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        LoanList loanList = loanRT.getForObject("/users/1/loans?orderBy={orderBy}", LoanList.class, orderBy);

        redirectAttributes.addFlashAttribute("raPaymentSummaryList", new PaymentSummaryList());
        redirectAttributes.addFlashAttribute("raLoans", loanList.getLoans());
        return "redirect:/loans/all";
    }

    @GetMapping("/loans/all")
    public String main(Model model, @ModelAttribute("raLoans") List<Loan> loans, @ModelAttribute("raPaymentSummaryList") PaymentSummaryList paymentSummaryList) {
        model.addAttribute("loans", loans);
        model.addAttribute("paymentSummaryList", paymentSummaryList);
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
        return "redirect:/loans";
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

        return "redirect:/loans";
    }

    @GetMapping("/loans/calculate")
    public String calculateLoanPayments(Model model) {
        RestTemplateProperties properties = restTemplateConfiguration.loanApiProperties();
        RestTemplate loanRT = restTemplateConfiguration.restTemplate(properties);
        LoanList loanList = loanRT.getForObject("/users/1/loans?orderBy={orderBy}", LoanList.class, "interest_rate");

        PaymentSummaryList paymentSummaryList = loanDao.getPaymentSummaries(loanList);

        // populate loans table
        model.addAttribute("loans", loanList.getLoans());

        // populate payment summary table
        model.addAttribute("paymentSummaryList", paymentSummaryList);

        // populate total contributions summary
        model.addAttribute("totalContributionsSummary", loanDao.getTotalContributionsSummary(paymentSummaryList));

        return "loansTable";
    }
}
