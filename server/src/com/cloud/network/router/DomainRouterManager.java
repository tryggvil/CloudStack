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
package com.cloud.network.router;

import java.util.List;
import java.util.Map;

import com.cloud.api.commands.UpgradeRouterCmd;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.VlanVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientNetworkCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.RemoteAccessVpnVO;
import com.cloud.network.VpnUserVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.Manager;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachineProfile;

/**
 * NetworkManager manages the network for the different end users.
 *
 */
public interface DomainRouterManager extends Manager {
    public static final int DEFAULT_ROUTER_VM_RAMSIZE = 128;            // 128M
    public static final boolean USE_POD_VLAN = false;
    /**
     * create the router.
     * 
     * @param accountId account Id the router belongs to.
     * @param ipAddress public ip address the router should use to access the internet.
     * @param dcId data center id the router should live in.
     * @param domain domain name of this network.
     * @param offering service offering associated with this request
     * @return DomainRouterVO if created.  null if not.
     */
    DomainRouterVO createRouter(long accountId, String ipAddress, long dcId, String domain, ServiceOfferingVO offering, long startEventId) throws ConcurrentOperationException;

    /**
     * create a DHCP server/user data server for directly connected VMs
     * @param userId the user id of the user creating the router.
     * @param accountId the account id of the user creating the router.
     * @param dcId data center id the router should live in.
     * @param domain domain name of this network.
     * @return DomainRouterVO if created.  null if not.
     */
	DomainRouterVO createDhcpServerForDirectlyAttachedGuests(long userId, long accountId, DataCenterVO dc, HostPodVO pod, Long candidateHost, VlanVO vlan) throws ConcurrentOperationException;
    
    /**
    /*
     * Send ssh public/private key pair to specified host
     * @param hostId
     * @param pubKey
     * @param prvKey
     */
    boolean sendSshKeysToHost(Long hostId, String pubKey, String prvKey);

    /**
     * save a vm password on the router.
     * 
	 * @param routerId the ID of the router to save the password to
	 * @param vmIpAddress the IP address of the User VM that will use the password
	 * @param password the password to save to the router
     */
    boolean savePasswordToRouter(long routerId, String vmIpAddress, String password);
    
    DomainRouterVO startRouter(long routerId, long eventId);
    
    boolean releaseRouter(long routerId);
    
    boolean destroyRouter(long routerId);
    
    boolean stopRouter(long routerId, long eventId);
    
    boolean getRouterStatistics(long vmId, Map<String, long[]> netStats, Map<String, long[]> diskStats);

    boolean rebootRouter(long routerId, long eventId);
    
    /**
     * @param hostId get all of the virtual machine routers on a host.
     * @return collection of VirtualMachineRouter
     */
    List<? extends VirtualRouter> getRouters(long hostId);
    
    /**
     * @param routerId id of the router
     * @return VirtualMachineRouter
     */
    DomainRouterVO getRouter(long routerId);
    
    /**
     * Add a DHCP entry on the domr dhcp server
     * @param routerHostId - the host id of the domr
     * @param routerIp - the private ip address of the domr
     * @param vmName - the name of the VM (e.g., i-10-TEST)
     * @param vmMac  - the mac address of the eth0 interface of the VM
     * @param vmIp   - the ip address to hand out.
     * @return success or failure
     */
    public boolean addDhcpEntry(long routerHostId, String routerIp, String vmName, String vmMac, String vmIp);
    
    /**
     * Adds a virtual machine into the guest network.
     *   1. Starts the domR
     *   2. Sets the dhcp Entry on the domR
     *   3. Sets the domR
     * 
     * @param vm user vm to add to the guest network
     * @param password password for this vm.  Can be null
     * @return DomainRouterVO if everything is successful.  null if not.
     * 
     * @throws ConcurrentOperationException if multiple starts are being attempted.
     */
	public DomainRouterVO addVirtualMachineToGuestNetwork(UserVmVO vm, String password, long startEventId) throws ConcurrentOperationException;	

    String createZoneVlan(DomainRouterVO router);
    
	VirtualRouter upgradeRouter(UpgradeRouterCmd cmd) throws InvalidParameterValueException, PermissionDeniedException;
	
	DomainRouterVO getRouter(long accountId, long zoneId);
	DomainRouterVO getRouter(String publicIpAddress);
	
	DomainRouterVO deployVirtualRouter(Network guestConfig, DeployDestination dest, Account owner) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException;
	
	DomainRouterVO deployDhcp(Network guestConfig, DeployDestination dest, Account owner) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException;
	
	RemoteAccessVpnVO startRemoteAccessVpn(RemoteAccessVpnVO vpnVO) throws ResourceUnavailableException;
	
	boolean addRemoveVpnUsers(RemoteAccessVpnVO vpnVO, List<VpnUserVO> addUsers, List<VpnUserVO> removeUsers);

	boolean deleteRemoteAccessVpn(RemoteAccessVpnVO vpnVO);
	
	DomainRouterVO addVirtualMachineIntoNetwork(Network config, NicProfile nic, VirtualMachineProfile<UserVm> vm, DeployDestination dest, ReservationContext context, Boolean startDhcp) throws ConcurrentOperationException, InsufficientNetworkCapacityException, ResourceUnavailableException;
    
    boolean associateIP (Network network, List<? extends IpAddress> ipAddress);
    
    boolean applyFirewallRules(Network network, List<? extends FirewallRule> rules);
}
