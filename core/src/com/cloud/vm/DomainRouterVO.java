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
package com.cloud.vm;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.cloud.network.router.VirtualRouter;
import com.cloud.utils.net.NetUtils;

/**
 * VirtualMachineRouterVO implements all the fields stored for a domain router.
 */
@Entity
@Table(name="domain_router")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue(value="DomainRouter")
public class DomainRouterVO extends VMInstanceVO implements VirtualRouter {
    @Column(name="ram_size", nullable=false)
    private int ramSize;
    
    @Column(name="gateway")
    private String gateway;
    
    @Column(name="public_ip_address")
    private String publicIpAddress;
    
    @Column(name="public_mac_address")
    private String publicMacAddress;
    
    @Column(name="public_netmask")
    private String publicNetmask;
    
    @Column(name="vlan_db_id")
    private Long vlanDbId;
    
    @Column(name="vlan_id")
    private String vlanId;
    
    @Column(name="guest_mac_address")
    private String guestMacAddress;
    
    @Column(name="guest_ip_address")
    private String guestIpAddress;
    
    @Column(name="guest_netmask")
    private String guestNetmask;

    @Column(name="dns1")
    private String dns1;
    
    @Column(name="dns2")
    private String dns2;
    
    @Column(name="domain", nullable=false)
    private String domain;

    @Column(name="vnet")
    private String vnet;
    
    @Column(name="dc_vlan")
    private String zoneVlan;

    @Column(name="guest_dc_mac_address")
    private String guestZoneMacAddress;
    
    @Column(name="network_id")
    long networkId;
    
    @Column(name="role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.DHCP_FIREWALL_LB_PASSWD_USERDATA;
    
    public DomainRouterVO(DomainRouterVO that) {
        this(that.id, that.serviceOfferingId, that.instanceName, that.privateMacAddress, that.privateIpAddress, that.privateNetmask, that.templateId, that.guestOSId, that.guestMacAddress, that.guestIpAddress, that.guestNetmask, that.accountId, that.domainId, that.publicMacAddress, that.publicIpAddress, that.publicNetmask, that.vlanDbId, that.vlanId, that.podId, that.dataCenterId, that.ramSize, that.gateway, that.domain, that.dns1, that.dns2);
        this.vnet = that.vnet;
        this.role = that.role;
    }
    
    public DomainRouterVO(long id,
                          long serviceOfferingId,
                          String name,
                          String privateMacAddress,
                          String privateIpAddress,
                          String privateNetmask,
                          long templateId,
                          long guestOSId,
                          String guestMacAddress,
                          String guestIpAddress,
                          String guestNetmask,
                          long accountId,
                          long domainId,
                          String publicMacAddress,
                          String publicIpAddress,
                          String publicNetMask,
                          Long vlanDbId, String vlanId,
                          long podId,
                          long dataCenterId,
                          int ramSize,
                          String gateway,
                          String domain,
                          String dns1,
                          String dns2) {
        super(id, serviceOfferingId, name, name, Type.DomainRouter, templateId, guestOSId, privateMacAddress, privateIpAddress, privateNetmask, dataCenterId, podId, domainId, accountId, true, null);
        this.privateMacAddress = privateMacAddress;
        this.guestMacAddress = guestMacAddress;
        this.guestIpAddress = guestIpAddress;
        this.publicIpAddress = publicIpAddress;
        this.publicMacAddress = publicMacAddress;
        this.publicNetmask = publicNetMask;
        this.vlanDbId = vlanDbId;
        this.vlanId = vlanId;
        this.ramSize = ramSize;
        this.gateway = gateway;
        this.domain = domain;
        this.dns1 = dns1;
        this.dns2 = dns2;
        this.dataCenterId = dataCenterId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.guestNetmask = guestNetmask;
    }
    
    public DomainRouterVO(long id,
            long serviceOfferingId,
            String name,
            long templateId,
            long guestOSId,
            long domainId,
            long accountId,
            long networkConfigurationId,
            boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.DomainRouter, templateId, guestOSId, domainId, accountId, haEnabled);
        this.networkId = networkConfigurationId;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public void setPublicNetmask(String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public void setGuestMacAddress(String routerMacAddress) {
        this.guestMacAddress = routerMacAddress;
    }
    
    public long getNetworkId() {
        return networkId;
    }
    
    @Override
    public String getGuestNetmask() {
        return guestNetmask;
    }

    public void setGuestIpAddress(String routerIpAddress) {
        this.guestIpAddress = routerIpAddress;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setVnet(String vnet) {
        this.vnet = vnet;
    }

    @Override
    public String getVnet() {
        return vnet;
    }
    
    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }
    
    @Override
    public String getPublicNetmask() {
        return publicNetmask;
    }
    
    @Override
    public String getPublicMacAddress() {
        return publicMacAddress;
    }
    
    @Override
    public String getGuestIpAddress() {
        return guestIpAddress;
    }
    
    protected DomainRouterVO() {
        super();
    }
    
    @Override
    public String getDns1() {
        return dns1;
    }

    @Override
    public String getDns2() {
        return dns2;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public int getRamSize() {
        return ramSize;
    }
    
    @Override
    public void setRamSize(int ramSize) {
        this.ramSize = ramSize;
    }
    
    @Override
    public String getGateway() {
        return gateway;
    }
    
    @Override
    public String getPublicIpAddress() {
        return publicIpAddress;
    }
    
    @Override
    public String getVlanId() {
    	return vlanId;
    }
    
    @Override
    public String getGuestMacAddress() {
        return guestMacAddress;
    }

	public void setVlanDbId(Long vlanDbId) {
		this.vlanDbId = vlanDbId;
	}

	public Long getVlanDbId() {
		return vlanDbId;
	}

	@Override
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String[] getDhcpRange() {
	   if (guestIpAddress != null && guestNetmask != null) {
		   long cidrSize = NetUtils.getCidrSize(guestNetmask);
		   return NetUtils.getIpRangeFromCidr(guestIpAddress, cidrSize);
	   }
	   return new String[2];
	}

    public void setZoneVlan(String zoneVlan) {
        this.zoneVlan = zoneVlan;
    }

    @Override
    public String getZoneVlan() {
        return zoneVlan;
    }

    public void setGuestZoneMacAddress(String guestZoneMacAddress) {
        this.guestZoneMacAddress = guestZoneMacAddress;
    }

    @Override
    public String getGuestZoneMacAddress() {
        return guestZoneMacAddress;
    }
    
    public void setServiceOfferingId(long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }
}
