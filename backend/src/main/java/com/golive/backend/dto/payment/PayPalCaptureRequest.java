package com.golive.backend.dto.payment;

import java.util.List;

public class PayPalCaptureRequest {

    private String providerOrderId;
    private String providerCaptureId;
    private String providerPayerId;
    private String status;
    private String currency;

    private String eventId;
    private String eventTitle;
    private String eventVenue;
    private String eventLocation;
    private String eventImage;

    private Double subtotal;
    private Double commission;
    private Double insurance;
    private Double fees;
    private Double total;

    private String userId;
    private String userEmail;
    private String payerEmail;

    private List<TicketSelectionRequest> tickets;
    private List<AttendeeRequest> attendees;

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public String getProviderCaptureId() {
        return providerCaptureId;
    }

    public void setProviderCaptureId(String providerCaptureId) {
        this.providerCaptureId = providerCaptureId;
    }

    public String getProviderPayerId() {
        return providerPayerId;
    }

    public void setProviderPayerId(String providerPayerId) {
        this.providerPayerId = providerPayerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public Double getInsurance() {
        return insurance;
    }

    public void setInsurance(Double insurance) {
        this.insurance = insurance;
    }

    public Double getFees() {
        return fees;
    }

    public void setFees(Double fees) {
        this.fees = fees;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public List<TicketSelectionRequest> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketSelectionRequest> tickets) {
        this.tickets = tickets;
    }

    public List<AttendeeRequest> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<AttendeeRequest> attendees) {
        this.attendees = attendees;
    }
}

