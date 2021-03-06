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

package com.cloud.network;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Entity
@Table(name=("remote_access_vpn"))
@SecondaryTable(name="account",
        pkJoinColumns={@PrimaryKeyJoinColumn(name="account_id", referencedColumnName="id")})
public class RemoteAccessVpnVO implements RemoteAccessVpn {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name="account_id")
    private long accountId;
    
    @Column(name="zone_id")
    private long zoneId;
    
    @Column(name="account_name", table="account", insertable=false, updatable=false)
    private String accountName = null;
    
    @Column(name="domain_id", table="account", insertable=false, updatable=false)
    private long domainId;

    @Column(name="vpn_server_addr")
    private String vpnServerAddress;
    
    @Column(name="local_ip")
    private String localIp;

    @Column(name="ip_range")
    private String ipRange;

    @Column(name="ipsec_psk")
    private String ipsecPresharedKey;

    public RemoteAccessVpnVO() { }

    public RemoteAccessVpnVO(long accountId, long zoneId, String publicIp, String localIp, String ipRange,  String presharedKey) {
        this.accountId = accountId;
        this.vpnServerAddress = publicIp;
        this.ipRange = ipRange;
        this.ipsecPresharedKey = presharedKey;
        this.zoneId = zoneId;
        this.localIp = localIp;

    }

    @Override
    public long getId() {
        return id;
    }

    

    @Override
    public long getAccountId() {
        return accountId;
    }
    
    @Override
    public String getAccountName() {
        return accountName;
    }

	@Override
    public String getVpnServerAddress() {
		return vpnServerAddress;
	}

	@Override
    public void setVpnServerAddress(String vpnServerAddress) {
		this.vpnServerAddress = vpnServerAddress;
	}

	@Override
    public String getIpRange() {
		return ipRange;
	}

	@Override
    public void setIpRange(String ipRange) {
		this.ipRange = ipRange;
	}

	@Override
    public String getIpsecPresharedKey() {
		return ipsecPresharedKey;
	}

	@Override
    public void setIpsecPresharedKey(String ipsecPresharedKey) {
		this.ipsecPresharedKey = ipsecPresharedKey;
	}

	@Override
    public void setId(Long id) {
		this.id = id;
	}

	@Override
    public void setZoneId(long zoneId) {
		this.zoneId = zoneId;
	}

	@Override
    public long getZoneId() {
		return zoneId;
	}

	@Override
    public String getLocalIp() {
		return localIp;
	}

	@Override
    public long getDomainId() {
		return domainId;
	}
    
    
}
