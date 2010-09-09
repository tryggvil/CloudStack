package com.cloud.api.response;

import com.cloud.api.ResponseObject;
import com.cloud.serializer.Param;

public class PortForwardingServiceRuleResponse implements ResponseObject {
    @Param(name="id")
    private long ruleId;

    @Param(name="publicport")
    private String publicPort;

    @Param(name="privateport")
    private String privatePort;

    @Param(name="protocol")
    private String protocol;

    @Param(name="portforwardingserviceid")
    private Long portForwardingServiceId;

    @Param(name="jobid")
    private Long jobId;

    @Param(name="jobstatus")
    private Integer jobStatus;

    public Long getPortForwardingServiceId() {
        return portForwardingServiceId;
    }

    public void setPortForwardingServiceId(Long portForwardingServiceId) {
        this.portForwardingServiceId = portForwardingServiceId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public String getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(String publicPort) {
        this.publicPort = publicPort;
    }

    public String getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(String privatePort) {
        this.privatePort = privatePort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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
}