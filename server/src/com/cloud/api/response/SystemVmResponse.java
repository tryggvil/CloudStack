/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class SystemVmResponse extends BaseResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("systemvmtype")
    private String systemVmType;

    @SerializedName("jobid")
    private Long jobId;

    @SerializedName("jobstatus")
    private Integer jobStatus;

    @SerializedName("zoneid")
    private Long zoneId;

    @SerializedName("zonename")
    private String zoneName;

    @SerializedName("dns1")
    private String dns1;

    @SerializedName("dns2")
    private String dns2;

    @SerializedName("networkdomain")
    private String networkDomain;

    @SerializedName("gateway")
    private String gateway;

    @SerializedName("name")
    private String name;

    @SerializedName("podid")
    private Long podId;

    @SerializedName("hostid")
    private Long hostId;

    @SerializedName("hostname")
    private String hostName;

    @SerializedName("privateip")
    private String privateIp;

    @SerializedName("privatemacaddress")
    private String privateMacAddress;

    @SerializedName("privatenetmask")
    private String privateNetmask;

    @SerializedName("publicip")
    private String publicIp;

    @SerializedName("publicmacaddress")
    private String publicMacAddress;

    @SerializedName("publicnetmask")
    private String publicNetmask;

    @SerializedName("templateid")
    private Long templateId;

    @SerializedName("created")
    private Date created;

    @SerializedName("state")
    private String state;

    @SerializedName("activeviewersessions")
    private Integer activeViewerSessions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemVmType() {
        return systemVmType;
    }

    public void setSystemVmType(String systemVmType) {
        this.systemVmType = systemVmType;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPodId() {
        return podId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    public void setPrivateMacAddress(String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }

    public String getPrivateNetmask() {
        return privateNetmask;
    }

    public void setPrivateNetmask(String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    public Integer getActiveViewerSessions() {
        return activeViewerSessions;
    }

    public void setActiveViewerSessions(Integer activeViewerSessions) {
        this.activeViewerSessions = activeViewerSessions;
    }
}