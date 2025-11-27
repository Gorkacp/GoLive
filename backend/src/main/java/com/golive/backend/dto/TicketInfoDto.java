package com.golive.backend.dto;

public class TicketInfoDto {
    private String ticketId;
    private String eventName;
    private String code;
    private String nombre;
    private String dni;
    private String precio;
    private String incluye;
    private String ubicacion;
    private String fecha;
    private String qrContent;

    // Getters y setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getPrecio() { return precio; }
    public void setPrecio(String precio) { this.precio = precio; }
    public String getIncluye() { return incluye; }
    public void setIncluye(String incluye) { this.incluye = incluye; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getQrContent() { return qrContent; }
    public void setQrContent(String qrContent) { this.qrContent = qrContent; }
}
