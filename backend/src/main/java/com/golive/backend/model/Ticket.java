package com.golive.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    private String ticketNumber = "GL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private String ticketId;
    private String transactionId;
    private String userId;
    private String userEmail;

    private String eventId;
    private String eventTitle;
    private String eventImage;
    private String venue;
    private String location;
    private Date eventDate;

    private String zoneName;
    private double price;
    private double serviceFee;
    private boolean insurance;
    private String status = "sold";

    private Attendee attendee;
    private String qrCode;
    private byte[] qrImageData;
    private byte[] ticketPdf;
    private Date issuedAt = new Date();
    private Date date = new Date();
    private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public boolean isInsurance() {
        return insurance;
    }

    public void setInsurance(boolean insurance) {
        this.insurance = insurance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Attendee getAttendee() {
        return attendee;
    }

    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public byte[] getQrImageData() {
        return qrImageData;
    }

    public void setQrImageData(byte[] qrImageData) {
        this.qrImageData = qrImageData;
    }

    public byte[] getTicketPdf() {
        return ticketPdf;
    }

    public void setTicketPdf(byte[] ticketPdf) {
        this.ticketPdf = ticketPdf;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static class Attendee {
        private String fullName;
        private String email;
        private String phone;
        private Integer birthDay;
        private Integer birthMonth;
        private Integer birthYear;
        private String idType;
        private String idNumber;
        private String postalCode;
        private String country;
        private String ticketType;
        private boolean insurance;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getBirthDay() {
            return birthDay;
        }

        public void setBirthDay(Integer birthDay) {
            this.birthDay = birthDay;
        }

        public Integer getBirthMonth() {
            return birthMonth;
        }

        public void setBirthMonth(Integer birthMonth) {
            this.birthMonth = birthMonth;
        }

        public Integer getBirthYear() {
            return birthYear;
        }

        public void setBirthYear(Integer birthYear) {
            this.birthYear = birthYear;
        }

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }

        public String getIdNumber() {
            return idNumber;
        }

        public void setIdNumber(String idNumber) {
            this.idNumber = idNumber;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTicketType() {
            return ticketType;
        }

        public void setTicketType(String ticketType) {
            this.ticketType = ticketType;
        }

        public boolean isInsurance() {
            return insurance;
        }

        public void setInsurance(boolean insurance) {
            this.insurance = insurance;
        }
    }
}
