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

import com.cloud.api.ApiConstants;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class IngressRuleResponse extends BaseResponse {
    @SerializedName("ruleid") @Param(description="the id of the ingress rule")
    private Long ruleId;

    @SerializedName("protocol") @Param(description="the protocol of the ingress rule")
    private String protocol;

    //FIXME - add description
    @SerializedName(ApiConstants.ICMP_TYPE)
    private Integer icmpType;

    //FIXME - add description
    @SerializedName(ApiConstants.ICMP_CODE)
    private Integer icmpCode;

    @SerializedName(ApiConstants.START_PORT) @Param(description="the starting IP of the ingress rule")
    private Integer startPort;

    @SerializedName(ApiConstants.END_PORT) @Param(description="the ending IP of the ingress rule ")
    private Integer endPort;

    @SerializedName(ApiConstants.NETWORK_GROUP_NAME) @Param(description="network group name")
    private String networkGroupName;

    @SerializedName(ApiConstants.ACCOUNT) @Param(description="account owning the ingress rule")
    private String accountName;

    @SerializedName(ApiConstants.CIDR) @Param(description="the CIDR notation for the base IP address of the ingress rule")
    private String cidr;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public void setStartPort(Integer startPort) {
        this.startPort = startPort;
    }

    public Integer getEndPort() {
        return endPort;
    }

    public void setEndPort(Integer endPort) {
        this.endPort = endPort;
    }

    public String getNetworkGroupName() {
        return networkGroupName;
    }

    public void setNetworkGroupName(String networkGroupName) {
        this.networkGroupName = networkGroupName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }
}
