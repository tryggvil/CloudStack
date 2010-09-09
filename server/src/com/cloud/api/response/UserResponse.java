package com.cloud.api.response;

import java.util.Date;

import com.cloud.api.ResponseObject;
import com.cloud.serializer.Param;

public class UserResponse implements ResponseObject {
    @Param(name="id")
    private Long id;

    @Param(name="username")
    private String username;

    @Param(name="firstname")
    private String firstname;

    @Param(name="lastname")
    private String lastname;

    @Param(name="email")
    private String email;

    @Param(name="created")
    private Date created;

    @Param(name="state")
    private String state;

    @Param(name="account")
    private String accountName;

    @Param(name="accounttype")
    private Short accountType;

    @Param(name="domainid")
    private Long domainId;

    @Param(name="domain")
    private String domainName;

    @Param(name="timezone")
    private String timezone;

    // TODO:  user keys?

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Short getAccountType() {
        return accountType;
    }

    public void setAccountType(Short accountType) {
        this.accountType = accountType;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}