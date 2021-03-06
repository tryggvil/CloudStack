package com.cloud.api.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@SuppressWarnings("rawtypes")
@Implementation(responseObject=SuccessResponse.class)
public class RevokeNetworkGroupIngressCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(RevokeNetworkGroupIngressCmd.class.getName());

    private static final String s_name = "revokenetworkgroupingress";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    //FIXME - add description
    @Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING)
    private String accountName;

    //FIXME - add description
    @Parameter(name=ApiConstants.CIDR_LIST, type=CommandType.STRING)
    private String cidrList;

    //FIXME - add description
    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG)
    private Long domainId;

    //FIXME - add description
    @Parameter(name=ApiConstants.END_PORT, type=CommandType.INTEGER)
    private Integer endPort;

    //FIXME - add description
    @Parameter(name=ApiConstants.ICMP_CODE, type=CommandType.INTEGER)
    private Integer icmpCode;

    //FIXME - add description
    @Parameter(name=ApiConstants.ICMP_TYPE, type=CommandType.INTEGER)
    private Integer icmpType;

    //FIXME - add description
    @Parameter(name=ApiConstants.NETWORK_GROUP_NAME, type=CommandType.STRING, required=true)
    private String networkGroupName;

    //FIXME - add description
    @Parameter(name=ApiConstants.PROTOCOL, type=CommandType.STRING)
    private String protocol;

    //FIXME - add description
    @Parameter(name=ApiConstants.START_PORT, type=CommandType.INTEGER)
    private Integer startPort;

    //FIXME - add description
    @Parameter(name=ApiConstants.USER_NETWORK_GROUP_LIST, type=CommandType.MAP)
    private Map userNetworkGroupList;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public String getCidrList() {
        return cidrList;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Integer getEndPort() {
        return endPort;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public String getNetworkGroupName() {
        return networkGroupName;
    }

    public String getProtocol() {
        return protocol;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public Map getUserNetworkGroupList() {
        return userNetworkGroupList;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public static String getResultObjectName() {
    	return "revokenetworkgroupingress";
    }

    @Override
    public long getEntityOwnerId() {
        Account account = UserContext.current().getAccount();
        if ((account == null) || isAdmin(account.getType())) {
            if ((domainId != null) && (accountName != null)) {
                Account userAccount = _responseGenerator.findAccountByNameDomain(accountName, domainId);
                if (userAccount != null) {
                    return userAccount.getId();
                }
            }
        }

        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_GROUP_REVOKE_INGRESS;
    }

    @Override
    public String getEventDescription() {
        StringBuilder sb = new StringBuilder();
        if (getUserNetworkGroupList() != null) {
            sb.append("group list(group/account): ");
            Collection userGroupCollection = getUserNetworkGroupList().values();
            Iterator iter = userGroupCollection.iterator();

            HashMap userGroup = (HashMap)iter.next();
            String group = (String)userGroup.get("group");
            String authorizedAccountName = (String)userGroup.get("account");
            sb.append(group + "/" + authorizedAccountName);

            while (iter.hasNext()) {
                userGroup = (HashMap)iter.next();
                group = (String)userGroup.get("group");
                authorizedAccountName = (String)userGroup.get("account");
                sb.append(", " + group + "/" + authorizedAccountName);
            }
        } else if (getCidrList() != null) {
            sb.append("cidr list: " + getCidrList());
        } else {
            sb.append("<error:  no ingress parameters>");
        }

        return  "revoking ingress from group: " + getNetworkGroupName() + " for " + sb.toString();
    }
    
    @Override
    public void execute(){
        boolean result = _networkGroupMgr.revokeNetworkGroupIngress(this);
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to revoke security group ingress rule");
        }
    }
}
