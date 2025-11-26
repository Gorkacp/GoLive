package com.golive.backend.dto.payment;

import com.golive.backend.model.Ticket;
import com.golive.backend.model.Transaction;

import java.util.List;

public class PaymentResultResponse {

    private String transactionId;
    private String transactionNumber;
    private double total;
    private String currency;
    private List<Ticket> tickets;

    public PaymentResultResponse() {
    }

    public PaymentResultResponse(Transaction transaction, List<Ticket> tickets) {
        this.transactionId = transaction.getId();
        this.transactionNumber = transaction.getTransactionNumber();
        this.total = transaction.getTotal();
        this.currency = transaction.getCurrency();
        this.tickets = tickets;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}

