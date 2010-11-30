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
package com.cloud.server;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cloud.alert.Alert;
import com.cloud.api.ServerApiException;
import com.cloud.api.commands.CreateDomainCmd;
import com.cloud.api.commands.DeleteDomainCmd;
import com.cloud.api.commands.DeletePreallocatedLunCmd;
import com.cloud.api.commands.DeployVMCmd;
import com.cloud.api.commands.ExtractVolumeCmd;
import com.cloud.api.commands.GetCloudIdentifierCmd;
import com.cloud.api.commands.ListAccountsCmd;
import com.cloud.api.commands.ListAlertsCmd;
import com.cloud.api.commands.ListAsyncJobsCmd;
import com.cloud.api.commands.ListCapabilitiesCmd;
import com.cloud.api.commands.ListCapacityCmd;
import com.cloud.api.commands.ListCfgsByCmd;
import com.cloud.api.commands.ListClustersCmd;
import com.cloud.api.commands.ListDiskOfferingsCmd;
import com.cloud.api.commands.ListDomainChildrenCmd;
import com.cloud.api.commands.ListDomainsCmd;
import com.cloud.api.commands.ListEventsCmd;
import com.cloud.api.commands.ListGuestOsCategoriesCmd;
import com.cloud.api.commands.ListGuestOsCmd;
import com.cloud.api.commands.ListHostsCmd;
import com.cloud.api.commands.ListHypervisorsCmd;
import com.cloud.api.commands.ListIpForwardingRulesCmd;
import com.cloud.api.commands.ListIsosCmd;
import com.cloud.api.commands.ListLoadBalancerRuleInstancesCmd;
import com.cloud.api.commands.ListLoadBalancerRulesCmd;
import com.cloud.api.commands.ListPodsByCmd;
import com.cloud.api.commands.ListPreallocatedLunsCmd;
import com.cloud.api.commands.ListPublicIpAddressesCmd;
import com.cloud.api.commands.ListRemoteAccessVpnsCmd;
import com.cloud.api.commands.ListRoutersCmd;
import com.cloud.api.commands.ListServiceOfferingsCmd;
import com.cloud.api.commands.ListSnapshotsCmd;
import com.cloud.api.commands.ListStoragePoolsCmd;
import com.cloud.api.commands.ListSystemVMsCmd;
import com.cloud.api.commands.ListTemplateOrIsoPermissionsCmd;
import com.cloud.api.commands.ListTemplatesCmd;
import com.cloud.api.commands.ListUsersCmd;
import com.cloud.api.commands.ListVMGroupsCmd;
import com.cloud.api.commands.ListVMsCmd;
import com.cloud.api.commands.ListVlanIpRangesCmd;
import com.cloud.api.commands.ListVolumesCmd;
import com.cloud.api.commands.ListVpnUsersCmd;
import com.cloud.api.commands.ListZonesByCmd;
import com.cloud.api.commands.RebootSystemVmCmd;
import com.cloud.api.commands.RegisterCmd;
import com.cloud.api.commands.RegisterPreallocatedLunCmd;
import com.cloud.api.commands.StartSystemVMCmd;
import com.cloud.api.commands.StopSystemVmCmd;
import com.cloud.api.commands.UpdateDomainCmd;
import com.cloud.api.commands.UpdateIsoCmd;
import com.cloud.api.commands.UpdateIsoPermissionsCmd;
import com.cloud.api.commands.UpdatePortForwardingRuleCmd;
import com.cloud.api.commands.UpdateTemplateCmd;
import com.cloud.api.commands.UpdateTemplatePermissionsCmd;
import com.cloud.api.commands.UpdateVMGroupCmd;
import com.cloud.api.commands.UploadCustomCertificateCmd;
import com.cloud.async.AsyncJob;
import com.cloud.capacity.Capacity;
import com.cloud.configuration.Configuration;
import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.dc.Vlan;
import com.cloud.domain.Domain;
import com.cloud.event.Event;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientStorageCapacityException;
import com.cloud.exception.InternalErrorException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.network.IpAddress;
import com.cloud.network.LoadBalancer;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.FirewallRule;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.org.Cluster;
import com.cloud.storage.GuestOS;
import com.cloud.storage.GuestOsCategory;
import com.cloud.storage.Snapshot;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.UserAccount;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.vm.InstanceGroup;
import com.cloud.vm.VirtualMachine;

/**
 * Hopefull this is temporary.
 *
 */
public interface ManagementService {
    static final String Name = "management-server";
    
    /**
     * Creates and starts a new Virtual Machine.
     * 
     * @param cmd the command with the deployment parameters
     *   - userId
     *   - accountId
     *   - zoneId
     *   - serviceOfferingId
     *   - templateId:  the id of the template (or ISO) to use for creating the virtual machine
     *   - diskOfferingId:  ID of the disk offering to use when creating the root disk (if deploying from an ISO) or the data disk (if deploying from a template). If deploying from a template and a disk offering ID is not passed in, the VM will have only a root disk.
     *   - displayName:  user-supplied name to be shown in the UI or returned in the API
     *   - groupName:  user-supplied groupname to be shown in the UI or returned in the API
     *   - userData:  user-supplied base64-encoded data that can be retrieved by the instance from the virtual router
     *   - size:  size to be used for volume creation in case the disk offering is private (i.e. size=0)
     * @return VirtualMachine if successfully deployed, null otherwise
     * @throws InvalidParameterValueException if the parameter values are incorrect.
     * @throws ExecutionException
     * @throws StorageUnavailableException
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException 
     * @throws InsufficientCapacityException 
     */
    UserVm deployVirtualMachine(DeployVMCmd cmd, String password) throws ResourceAllocationException, InsufficientStorageCapacityException, ExecutionException, StorageUnavailableException, ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException;

    /**
     * Retrieves the list of data centers with search criteria.
     * Currently the only search criteria is "available" zones for the account that invokes the API.  By specifying
     * available=true all zones which the account can access.  By specifying available=false the zones where the
     * account has virtual machine instances will be returned.
     * @return a list of DataCenters
     */
    List<? extends DataCenter> listDataCenters(ListZonesByCmd cmd);

    /**
     * returns the a map of the names/values in the configuraton table
     * @return map of configuration name/values
     */
    List<? extends Configuration> searchForConfigurations(ListCfgsByCmd c);
    
    /** revisit
     * Searches for users by the specified search criteria
     * Can search by: "id", "username", "account", "domainId", "type"
     * @param cmd
     * @return List of UserAccounts
     */
    List<? extends UserAccount> searchForUsers(ListUsersCmd cmd);
    
    /**
     * Searches for Service Offerings by the specified search criteria
     * Can search by: "name"
     * @param cmd
     * @return List of ServiceOfferings
     */
    List<? extends ServiceOffering> searchForServiceOfferings(ListServiceOfferingsCmd cmd);
    
    /**
     * Searches for Clusters by the specified search criteria
     * @param c
     * @return
     */
    List<? extends Cluster> searchForClusters(ListClustersCmd c);
    
    /**
     * Searches for Pods by the specified search criteria
     * Can search by: pod name and/or zone name
     * @param cmd
     * @return List of Pods
     */
    List<? extends Pod> searchForPods(ListPodsByCmd cmd);
    
    /**
     * Searches for servers by the specified search criteria
     * Can search by: "name", "type", "state", "dataCenterId", "podId"
     * @param cmd
     * @return List of Hosts
     */
    List<? extends Host> searchForServers(ListHostsCmd cmd);
    
    /**
     * Creates a new template
     * @param cmd
     * @return updated template
     * @throws InvalidParameterValueException, PermissionDeniedException
     */
    VirtualMachineTemplate updateTemplate(UpdateIsoCmd cmd);
    VirtualMachineTemplate updateTemplate(UpdateTemplateCmd cmd);
    
    /**
     * Obtains a list of virtual machines by the specified search criteria.
     * Can search by: "userId", "name", "state", "dataCenterId", "podId", "hostId"
     * @param cmd the API command that wraps the search criteria
     * @return List of UserVMs.
     */
    List<? extends UserVm> searchForUserVMs(ListVMsCmd cmd);

    /**
     * Update an existing port forwarding rule on the given public IP / public port for the given protocol
     * @param cmd - the UpdatePortForwardingRuleCmd command that wraps publicIp, privateIp, publicPort, privatePort, protocol of the rule to update
     * @return the new firewall rule if updated, null if no rule on public IP / public port of that protocol could be found
     */
    FirewallRule updatePortForwardingRule(UpdatePortForwardingRuleCmd cmd);

    /**
     * Obtains a list of events by the specified search criteria.
     * Can search by: "username", "type", "level", "startDate", "endDate"
     * @param c
     * @return List of Events.
     */
    List<? extends Event> searchForEvents(ListEventsCmd c);
    
    /**
     * registerPreallocatedLun registers a preallocated lun in our database.
     * 
     * @param cmd the API command wrapping the register parameters
     *   - targetIqn iqn for the storage server.
     *   - portal portal ip address for the storage server.
     *   - lun lun #
     *   - size size of the lun
     *   - dcId data center to attach to
     *   - tags tags to attach to the lun
     * @return the new PreAllocatedLun 
     */
    Object registerPreallocatedLun(RegisterPreallocatedLunCmd cmd);
    
    /**
     * Obtains a list of routers by the specified search criteria.
     * Can search by: "userId", "name", "state", "dataCenterId", "podId", "hostId"
     * @param cmd
     * @return List of DomainRouters.
     */
    List<? extends VirtualRouter> searchForRouters(ListRoutersCmd cmd);
    
    /** revisit
     * Obtains a list of storage volumes by the specified search criteria.
     * Can search by: "userId", "vType", "instanceId", "dataCenterId", "podId", "hostId"
     * @param cmd
     * @return List of Volumes.
     */
    List<? extends Volume> searchForVolumes(ListVolumesCmd cmd);

    /**
     * Obtains a list of IP Addresses by the specified search criteria.
     * Can search by: "userId", "dataCenterId", "address"
     * @param cmd the command that wraps the search criteria
     * @return List of IPAddresses
     */
    List<? extends IpAddress> searchForIPAddresses(ListPublicIpAddressesCmd cmd);
    
    /**
     * Obtains a list of all guest OS.
     * @return list of GuestOS
     */
    List<? extends GuestOS> listGuestOSByCriteria(ListGuestOsCmd cmd);
    
    /**
     * Obtains a list of all guest OS categories.
     * @return list of GuestOSCategories
     */
    List<? extends GuestOsCategory> listGuestOSCategoriesByCriteria(ListGuestOsCategoriesCmd cmd);
    
    VirtualMachine stopSystemVM(StopSystemVmCmd cmd);
    VirtualMachine startSystemVM(StartSystemVMCmd cmd);
    VirtualMachine rebootSystemVM(RebootSystemVmCmd cmd);
    /**
     * Search for domains owned by the given domainId/domainName (those parameters are wrapped
     * in a command object.
     * @return list of domains owned by the given user
     */
    List<? extends Domain> searchForDomains(ListDomainsCmd c);
    
    List<? extends Domain> searchForDomainChildren(ListDomainChildrenCmd cmd);

    /**
     * create a new domain
     * @param command - the create command defining the name to use and the id of the parent domain under which to create the new domain.
     */
    Domain createDomain(CreateDomainCmd command);

    /**
     * delete a domain with the given domainId
     * @param cmd the command wrapping the delete parameters
     *   - domainId
     *   - ownerId
     *   - cleanup:  whether or not to delete all accounts/VMs/sub-domains when deleting the domain
     */
    boolean deleteDomain(DeleteDomainCmd cmd);

    /**
     * update an existing domain
     * @param cmd - the command containing domainId and new domainName
     * @return Domain object if the command succeeded
     * @throws InvalidParameterValueException, PermissionDeniedException
     */
    Domain updateDomain(UpdateDomainCmd cmd);
    
    /**
     * Searches for accounts by the specified search criteria
     * Can search by: "id", "name", "domainid", "type"
     * @param cmd
     * @return List of Accounts
     */
    List<? extends Account> searchForAccounts(ListAccountsCmd cmd);
    
    /**
     * Searches for alerts
     * @param c
     * @return List of Alerts
     */
    List<? extends Alert> searchForAlerts(ListAlertsCmd cmd);

    /**
     * list all the capacity rows in capacity operations table
     * @param cmd
     * @return List of capacities
     */
    List<? extends Capacity> listCapacities(ListCapacityCmd cmd);

    /**
     * List all snapshots of a disk volume. Optionaly lists snapshots created by specified interval
     * @param cmd the command containing the search criteria (order by, limit, etc.)
     * @return list of snapshots
     * @throws InvalidParameterValueException
     * @throws PermissionDeniedException
     */
    List<? extends Snapshot> listSnapshots(ListSnapshotsCmd cmd);

    /**
     * List the permissions on a template.  This will return a list of account names that have been granted permission to launch instances from the template.
     * @param cmd the command wrapping the search criteria (template id)
     * @return list of account names that have been granted permission to launch instances from the template
     */
    List<String> listTemplatePermissions(ListTemplateOrIsoPermissionsCmd cmd);

    /**
     * List ISOs that match the specified criteria. 
     * @param cmd The command that wraps the (optional) templateId, name, keyword, templateFilter, bootable, account, and zoneId parameters.
     * @return list of ISOs
     */
    List<? extends VirtualMachineTemplate> listIsos(ListIsosCmd cmd);

    /**
     * List templates that match the specified criteria. 
     * @param cmd The command that wraps the (optional) templateId, name, keyword, templateFilter, bootable, account, and zoneId parameters.
     * @return list of ISOs
     */
    List<? extends VirtualMachineTemplate> listTemplates(ListTemplatesCmd cmd);

    /**
     * Search for disk offerings based on search criteria
     * @param cmd the command containing the criteria to use for searching for disk offerings
     * @return a list of disk offerings that match the given criteria
     */
    List<? extends DiskOffering> searchForDiskOfferings(ListDiskOfferingsCmd cmd);

    /**
     * List instances that have either been applied to a load balancer or are eligible to be assigned to a load balancer.
     * @param cmd
     * @return list of vm instances that have been or can be applied to a load balancer
     */
    List<? extends UserVm> listLoadBalancerInstances(ListLoadBalancerRuleInstancesCmd cmd);

    /**
     * List load balancer rules based on the given criteria
     * @param cmd the command that specifies the criteria to use for listing load balancers.  Load balancers can be listed
     *            by id, name, public ip, and vm instance id
     * @return list of load balancers that match the criteria
     */
    List<? extends LoadBalancer> searchForLoadBalancers(ListLoadBalancerRulesCmd cmd);

    /**
     * List storage pools that match the given criteria
     * @param cmd the command that wraps the search criteria (zone, pod, name, IP address, path, and cluster id)
     * @return a list of storage pools that match the given criteria
     */
    List<? extends StoragePool> searchForStoragePools(ListStoragePoolsCmd cmd);

    /**
     * List system VMs by the given search criteria
     * @param cmd the command that wraps the search criteria (host, name, state, type, zone, pod, and/or id)
     * @return the list of system vms that match the given criteria
     */
    List<? extends VirtualMachine> searchForSystemVm(ListSystemVMsCmd cmd);

    /**
     * Returns back a SHA1 signed response
     * @param userId -- id for the user
     * @return -- ArrayList of <CloudId+Signature>
     */
    ArrayList<String> getCloudIdentifierResponse(GetCloudIdentifierCmd cmd);

    public List<? extends Object> getPreAllocatedLuns(ListPreallocatedLunsCmd cmd);

    boolean updateTemplatePermissions(UpdateTemplatePermissionsCmd cmd);
    boolean updateTemplatePermissions(UpdateIsoPermissionsCmd cmd);
    String[] createApiKeyAndSecretKey(RegisterCmd cmd);

    InstanceGroup updateVmGroup(UpdateVMGroupCmd cmd);

    List<? extends InstanceGroup> searchForVmGroups(ListVMGroupsCmd cmd);

    Map<String, String> listCapabilities(ListCapabilitiesCmd cmd);

    /**
     * Extracts the volume to a particular location.
     * @param cmd the command specifying url (where the volume needs to be extracted to), zoneId (zone where the volume exists), id (the id of the volume)
     * @throws URISyntaxException
     * @throws InternalErrorException
     * @throws PermissionDeniedException 
     *
     */
    Long extractVolume(ExtractVolumeCmd cmd) throws URISyntaxException;

    /**
     * return an array of available hypervisors
     * @param cmd
     * @return an array of available hypervisors in the cloud
     */
    String[] getHypervisors(ListHypervisorsCmd cmd);

    /**
     * This method uploads a custom cert to the db, and patches every cpvm with it on the current ms
     * @param cmd -- upload certificate cmd
     * @return -- returns a string on success
     * @throws ServerApiException -- even if one of the console proxy patching fails, we throw back this exception
     */
    String uploadCertificate(UploadCustomCertificateCmd cmd);
    
    public List<? extends RemoteAccessVpn> searchForRemoteAccessVpns(ListRemoteAccessVpnsCmd cmd);
    
    public List<? extends VpnUser> searchForVpnUsers(ListVpnUsersCmd cmd);

    List<? extends FirewallRule> searchForIpForwardingRules(ListIpForwardingRulesCmd cmd);

    String getVersion();
    
    /**
     * Searches for vlan by the specified search criteria
     * Can search by: "id", "vlan", "name", "zoneID"
     * @param cmd
     * @return List of Vlans
     */
    List<? extends Vlan> searchForVlans(ListVlanIpRangesCmd cmd);
    /**
     * Search for async jobs by account and/or startDate
     * @param cmd the command specifying the account and start date parameters
     * @return the list of async jobs that match the criteria
     */
    List<? extends AsyncJob> searchForAsyncJobs(ListAsyncJobsCmd cmd);
    
    /**
     * Generates a random password that will be used (initially) by newly created and started virtual machines
     * @return a random password
     */
    String generateRandomPassword();
    /**
     * Unregisters a preallocated lun in our database
     * @param cmd the api command wrapping the id of the lun
     * @return true if unregistered; false if not.
     * @throws IllegalArgumentException
     */
    boolean unregisterPreallocatedLun(DeletePreallocatedLunCmd cmd) throws IllegalArgumentException;



}