package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.exception.ResourceInUseException;

@Implementation(description="Deletes network group", responseObject=SuccessResponse.class)
public class DeleteNetworkGroupCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteNetworkGroupCmd.class.getName());
    private static final String s_name = "deletenetworkgroupresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING, description="the account of the network group. Must be specified with domain ID")
    private String accountName;

    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="the domain ID of account owning the network group")
    private Long domainId;

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="the network group name")
    private String networkGroupName;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getNetworkGroupName() {
        return networkGroupName;
    }


    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
	
    @Override
    public void execute(){
        try{
            boolean result = _networkGroupMgr.deleteNetworkGroup(this);
            if (result) {
                SuccessResponse response = new SuccessResponse(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete network group");
            }
        } catch (ResourceInUseException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(BaseCmd.RESOURCE_IN_USE_ERROR, ex.getMessage());
        }
    }
}
