package com.nobelti.cl.model;

import java.util.Date;

public class ValidaProyectoResponse {

    private boolean validate;
    private String nombreProyecto;
    private Date createDate;
    private String companyOwner;
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    public boolean isValidate() {
        return validate;
    }
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    public String getNombreProyecto() {
        return nombreProyecto;
    }
    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public String getCompanyOwner() {
        return companyOwner;
    }
    public void setCompanyOwner(String companyOwner) {
        this.companyOwner = companyOwner;
    }

    
    
}
