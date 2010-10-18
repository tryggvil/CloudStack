/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 * 
 * You should have received a copy of the GNU General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ExtractResponse;
import com.cloud.event.EventTypes;
import com.cloud.storage.VolumeVO;
import com.cloud.user.Account;

@Implementation(method="extractVolume")
public class ExtractVolumeCmd extends BaseAsyncCmd {
	public static final Logger s_logger = Logger.getLogger(ExtractVolumeCmd.class.getName());

    private static final String s_name = "extractvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    //FIXME - add description
    @Parameter(name="id", type=CommandType.LONG, required=true)
    private Long id;

    //FIXME - add description
    @Parameter(name="url", type=CommandType.STRING, required=true)
    private String url;

    //FIXME - add description
    @Parameter(name="zoneid", type=CommandType.LONG, required=true)
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

	@Override
	public String getName() {
		return s_name;
	}

    @Override
    public long getAccountId() {
        VolumeVO volume = ApiDBUtils.findVolumeById(getId());
        if (volume != null) {
            return volume.getId();
        }

        // invalid id, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_UPLOAD;
    }

    @Override
    public String getEventDescription() {
        return  "Extraction job";
    }

	@Override @SuppressWarnings("unchecked")
	public ExtractResponse getResponse() {
	    ExtractResponse response = (ExtractResponse)getResponseObject();
	    response.setResponseName(getName());
	    return response;
	}

	public static String getStaticName() {
		return "ExtractVolume";
	}
}