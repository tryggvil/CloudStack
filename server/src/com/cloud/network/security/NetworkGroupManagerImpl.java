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
package com.cloud.network.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.NetworkIngressRulesCmd;
import com.cloud.agent.api.NetworkIngressRulesCmd.IpPortAndProto;
import com.cloud.agent.manager.Commands;
import com.cloud.api.BaseCmd;
import com.cloud.api.ServerApiException;
import com.cloud.api.commands.AuthorizeNetworkGroupIngressCmd;
import com.cloud.api.commands.CreateNetworkGroupCmd;
import com.cloud.api.commands.DeleteNetworkGroupCmd;
import com.cloud.api.commands.ListNetworkGroupsCmd;
import com.cloud.api.commands.RevokeNetworkGroupIngressCmd;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.network.security.NetworkGroupWorkVO.Step;
import com.cloud.network.security.dao.IngressRuleDao;
import com.cloud.network.security.dao.NetworkGroupDao;
import com.cloud.network.security.dao.NetworkGroupRulesDao;
import com.cloud.network.security.dao.NetworkGroupVMMapDao;
import com.cloud.network.security.dao.NetworkGroupWorkDao;
import com.cloud.network.security.dao.VmRulesetLogDao;
import com.cloud.server.ManagementServer;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.UserContext;
import com.cloud.user.dao.AccountDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.State;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;

@Local(value={NetworkGroupManager.class, NetworkGroupService.class})
public class NetworkGroupManagerImpl implements NetworkGroupManager, NetworkGroupService, Manager {
    public static final Logger s_logger = Logger.getLogger(NetworkGroupManagerImpl.class);

	@Inject NetworkGroupDao _networkGroupDao;
	@Inject IngressRuleDao  _ingressRuleDao;
	@Inject NetworkGroupVMMapDao _networkGroupVMMapDao;
	@Inject NetworkGroupRulesDao _networkGroupRulesDao;
	@Inject UserVmDao _userVMDao;
	@Inject AccountDao _accountDao;
	@Inject ConfigurationDao _configDao;
	@Inject NetworkGroupWorkDao _workDao;
	@Inject VmRulesetLogDao _rulesetLogDao;
	@Inject DomainDao _domainDao;
	@Inject AgentManager _agentMgr;
	ScheduledExecutorService _executorPool;
    ScheduledExecutorService _cleanupExecutor;

	private long _serverId;

	private final long _timeBetweenCleanups = 30; //seconds

	
	boolean _enabled = false;
	NetworkGroupListener _answerListener;
    
	
	private final class NetworkGroupVOComparator implements
			Comparator<NetworkGroupVO> {
		@Override
		public int compare(NetworkGroupVO o1, NetworkGroupVO o2) {
			return o1.getId() == o2.getId() ? 0 : o1.getId() < o2.getId() ? -1 : 1;
		}
	}

	public  class WorkerThread implements Runnable {
		@Override
		public void run() {
			work();
		}
		
		WorkerThread() {
			
		}
	}
	
	public  class CleanupThread implements Runnable {
		@Override
		public void run() {
			cleanupFinishedWork();
			cleanupUnfinishedWork();
		}
	


		CleanupThread() {
			
		}
	}
	
	


	
	public static class PortAndProto implements Comparable<PortAndProto>{
		String proto;
		int startPort;
		int endPort;
		public PortAndProto(String proto, int startPort, int endPort) {
			this.proto = proto;
			this.startPort = startPort;
			this.endPort = endPort;
		}
		public String getProto() {
			return proto;
		}
		public int getStartPort() {
			return startPort;
		}
		public int getEndPort() {
			return endPort;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + endPort;
			result = prime * result + ((proto == null) ? 0 : proto.hashCode());
			result = prime * result + startPort;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PortAndProto other = (PortAndProto) obj;
			if (endPort != other.endPort)
				return false;
			if (proto == null) {
				if (other.proto != null)
					return false;
			} else if (!proto.equals(other.proto))
				return false;
			if (startPort != other.startPort)
				return false;
			return true;
		}
		
		@Override
		public int compareTo(PortAndProto obj) {
			if (this == obj)
				return 0;
			if (obj == null)
				return 1;
			if (proto == null) {
				if (obj.proto != null)
					return -1;
				else
					return 0;
			}
			if (!obj.proto.equalsIgnoreCase(proto)) {
				return proto.compareTo(obj.proto);
			}
			if (startPort < obj.startPort)
				return -1;
			else if (startPort > obj.startPort)
				return 1;
			
			if (endPort < obj.endPort)
				return -1;
			else if (endPort > obj.endPort)
				return 1;
			
			return 0;
		}
		
	}

	@Override
	public void handleVmStateTransition(UserVm userVm, State vmState) {
		if (!_enabled) {
			return;
		}
		switch (vmState) {
		case Creating:
		case Destroyed:
		case Error:
		case Migrating:
		case Expunging:
		case Starting:
		case Unknown:
			return;
		case Running:
			handleVmStarted(userVm);
			break;
		case Stopping:
		case Stopped:
			handleVmStopped(userVm);
			break;
		}

	}
	
	public static class CidrComparator implements Comparator<String> {

		@Override
		public int compare(String cidr1, String cidr2) {
			return cidr1.compareTo(cidr2); //FIXME
		}
		
	}

	protected Map<PortAndProto, Set<String>> generateRulesForVM(Long userVmId){

		Map<PortAndProto, Set<String>> allowed = new TreeMap<PortAndProto, Set<String>>();

		List<NetworkGroupVMMapVO> groupsForVm = _networkGroupVMMapDao.listByInstanceId(userVmId);
		for (NetworkGroupVMMapVO mapVO: groupsForVm) {
			List<IngressRuleVO> rules = _ingressRuleDao.listByNetworkGroupId(mapVO.getNetworkGroupId());
			for (IngressRuleVO rule: rules){
				PortAndProto portAndProto = new PortAndProto(rule.getProtocol(), rule.getStartPort(), rule.getEndPort());
				Set<String> cidrs = allowed.get(portAndProto );
				if (cidrs == null) {
					cidrs = new TreeSet<String>(new CidrComparator());
				}
				if (rule.getAllowedNetworkId() != null){
					List<NetworkGroupVMMapVO> allowedInstances = _networkGroupVMMapDao.listByNetworkGroup(rule.getAllowedNetworkId(), State.Running);
					for (NetworkGroupVMMapVO ngmapVO: allowedInstances){
						String cidr = ngmapVO.getGuestIpAddress();
						if (cidr != null) {
							cidr = cidr + "/32";
							cidrs.add(cidr);
						}
					}
				}else if (rule.getAllowedSourceIpCidr() != null) {
					cidrs.add(rule.getAllowedSourceIpCidr());
				}
				if (cidrs.size() > 0)
					allowed.put(portAndProto, cidrs);
			}
		}


		return  allowed;
	}
	
	private String generateRulesetSignature(Map<PortAndProto, Set<String>> allowed) {
		String ruleset = allowed.toString();
		return DigestUtils.md5Hex(ruleset);
	}

	protected void handleVmStarted(UserVm userVm) {
		Set<Long> affectedVms = getAffectedVmsForVmStart(userVm);
		scheduleRulesetUpdateToHosts(affectedVms, true, null);
	}
	
	@DB
	public void scheduleRulesetUpdateToHosts(Set<Long> affectedVms, boolean updateSeqno, Long delayMs) {
	    if (!_enabled) {
	        return;
	    }
		if (delayMs == null)
			delayMs = new Long(100l);
		
		for (Long vmId: affectedVms) {
			Transaction txn = Transaction.currentTxn();
			txn.start();
			VmRulesetLogVO log = null;
			NetworkGroupWorkVO work = null;
			UserVm vm = null;
			try {
				vm = _userVMDao.acquireInLockTable(vmId);
				if (vm == null) {
					s_logger.warn("Failed to acquire lock on vm id " + vmId);
					continue;
				}
				log = _rulesetLogDao.findByVmId(vmId);
				if (log == null) {
					log = new VmRulesetLogVO(vmId);
					log = _rulesetLogDao.persist(log);
				}
		
				if (log != null && updateSeqno){
					log.incrLogsequence();
					_rulesetLogDao.update(log.getId(), log);
				}
				work = _workDao.findByVmIdStep(vmId, Step.Scheduled);
				if (work == null) {
					work = new NetworkGroupWorkVO(vmId,  null, null, NetworkGroupWorkVO.Step.Scheduled, null);
					work = _workDao.persist(work);
				}
				
				work.setLogsequenceNumber(log.getLogsequence());
				 _workDao.update(work.getId(), work);
				
			} finally {
				if (vm != null) {
					_userVMDao.releaseFromLockTable(vmId);
				}
			}
			txn.commit();

			_executorPool.schedule(new WorkerThread(), delayMs, TimeUnit.MILLISECONDS);

		}
	}
	
	protected Set<Long> getAffectedVmsForVmStart(UserVm userVm) {
		Set<Long> affectedVms = new HashSet<Long>();
		affectedVms.add(userVm.getId());
		List<NetworkGroupVMMapVO> groupsForVm = _networkGroupVMMapDao.listByInstanceId(userVm.getId());
		//For each group, find the ingress rules that allow the group
		for (NetworkGroupVMMapVO mapVO: groupsForVm) {//FIXME: use custom sql in the dao
			List<IngressRuleVO> allowingRules = _ingressRuleDao.listByAllowedNetworkGroupId(mapVO.getNetworkGroupId());
			//For each ingress rule that allows a group that the vm belongs to, find the group it belongs to
			affectedVms.addAll(getAffectedVmsForIngressRules(allowingRules));
		}
		return affectedVms;
	}
	
	protected Set<Long> getAffectedVmsForVmStop(UserVm userVm) {
		Set<Long> affectedVms = new HashSet<Long>();
		List<NetworkGroupVMMapVO> groupsForVm = _networkGroupVMMapDao.listByInstanceId(userVm.getId());
		//For each group, find the ingress rules that allow the group
		for (NetworkGroupVMMapVO mapVO: groupsForVm) {//FIXME: use custom sql in the dao
			List<IngressRuleVO> allowingRules = _ingressRuleDao.listByAllowedNetworkGroupId(mapVO.getNetworkGroupId());
			//For each ingress rule that allows a group that the vm belongs to, find the group it belongs to
			affectedVms.addAll(getAffectedVmsForIngressRules(allowingRules));
		}
		return affectedVms;
	}
	
	
	protected Set<Long> getAffectedVmsForIngressRules(List<IngressRuleVO> allowingRules) {
		Set<Long> distinctGroups = new HashSet<Long> ();
		Set<Long> affectedVms = new HashSet<Long>();

		for (IngressRuleVO allowingRule: allowingRules){
			distinctGroups.add(allowingRule.getNetworkGroupId());
		}
		for (Long groupId: distinctGroups){
			//allVmUpdates.putAll(generateRulesetForGroupMembers(groupId));
			affectedVms.addAll(_networkGroupVMMapDao.listVmIdsByNetworkGroup(groupId));
		}
		return affectedVms;
	}

	
	
	protected NetworkIngressRulesCmd generateRulesetCmd(String vmName, String guestIp, String guestMac, Long vmId, String signature,  long seqnum, Map<PortAndProto, Set<String>> rules) {
		List<IpPortAndProto> result = new ArrayList<IpPortAndProto>();
		for (PortAndProto pAp : rules.keySet()) {
			Set<String> cidrs = rules.get(pAp);
			if (cidrs.size() > 0) {
				IpPortAndProto ipPortAndProto = new NetworkIngressRulesCmd.IpPortAndProto(pAp.getProto(), pAp.getStartPort(), pAp.getEndPort(), cidrs.toArray(new String[cidrs.size()]));
				result.add(ipPortAndProto);
			}
		}
		return new NetworkIngressRulesCmd(guestIp, guestMac, vmName, vmId, signature, seqnum, result.toArray(new IpPortAndProto[result.size()]));
	}
	
	protected void handleVmStopped(UserVm userVm) {
		Set<Long> affectedVms = getAffectedVmsForVmStop(userVm);
		scheduleRulesetUpdateToHosts(affectedVms, true, null);
	}
	
	
	@Override @DB @SuppressWarnings("rawtypes")
	public List<IngressRuleVO> authorizeNetworkGroupIngress(AuthorizeNetworkGroupIngressCmd cmd) throws InvalidParameterValueException, PermissionDeniedException{
		String groupName = cmd.getNetworkGroupName();
		String protocol = cmd.getProtocol();
		Integer startPort = cmd.getStartPort();
		Integer endPort = cmd.getEndPort();
		Integer icmpType = cmd.getIcmpType();
		Integer icmpCode = cmd.getIcmpCode();
		List<String> cidrList = cmd.getCidrList();
		Map groupList = cmd.getUserNetworkGroupList();
        Account account = UserContext.current().getAccount();
        String accountName = cmd.getAccountName();
        Long domainId = cmd.getDomainId();
		Integer startPortOrType = null;
        Integer endPortOrCode = null;
        Long accountId = null;
		
		if (!_enabled) {
			return null;
		}
		
		//Verify input parameters
        if (protocol == null) {
        	protocol = "all";
        }

        if (!NetUtils.isValidNetworkGroupProto(protocol)) {
        	s_logger.debug("Invalid protocol specified " + protocol);
        	 throw new InvalidParameterValueException("Invalid protocol " + protocol);
        }
        if ("icmp".equalsIgnoreCase(protocol) ) {
            if ((icmpType == null) || (icmpCode == null)) {
                throw new InvalidParameterValueException("Invalid ICMP type/code specified, icmpType = " + icmpType + ", icmpCode = " + icmpCode);
            }
        	if (icmpType == -1 && icmpCode != -1) {
        		throw new InvalidParameterValueException("Invalid icmp type range" );
        	} 
        	if (icmpCode > 255) {
        		throw new InvalidParameterValueException("Invalid icmp code " );
        	}
        	startPortOrType = icmpType;
        	endPortOrCode= icmpCode;
        } else if (protocol.equals("all")) {
        	if ((startPort != null) || (endPort != null)) {
                throw new InvalidParameterValueException("Cannot specify startPort or endPort without specifying protocol");
            }
        	startPortOrType = 0;
        	endPortOrCode = 0;
        } else {
            if ((startPort == null) || (endPort == null)) {
                throw new InvalidParameterValueException("Invalid port range specified, startPort = " + startPort + ", endPort = " + endPort);
            }
            if (startPort == 0 && endPort == 0) {
                endPort = 65535;
            }
            if (startPort > endPort) {
                s_logger.debug("Invalid port range specified: " + startPort + ":" + endPort);
                throw new InvalidParameterValueException("Invalid port range " );
            }
            if (startPort > 65535 || endPort > 65535 || startPort < -1 || endPort < -1) {
                s_logger.debug("Invalid port numbers specified: " + startPort + ":" + endPort);
                throw new InvalidParameterValueException("Invalid port numbers " );
            }
            
        	if (startPort < 0 || endPort < 0) {
        		throw new InvalidParameterValueException("Invalid port range " );
        	}
            startPortOrType = startPort;
            endPortOrCode= endPort;
        }
        
        protocol = protocol.toLowerCase();

        if ((account == null) || isAdmin(account.getType())) {
            if ((accountName != null) && (domainId != null)) {
                // if it's an admin account, do a quick permission check
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Unable to find rules for network security group id = " + groupName + ", permission denied.");
                    }
                    throw new PermissionDeniedException("Unable to find rules for network security group id = " + groupName + ", permission denied.");
                }

                Account groupOwner = _accountDao.findActiveAccount(accountName, domainId);
                if (groupOwner == null) {
                    throw new PermissionDeniedException("Unable to find account " + accountName + " in domain " + domainId);
                }
                accountId = groupOwner.getId();
            } else {
                if (account != null) {
                    accountId = account.getId();
                    domainId = account.getDomainId();
                }
            }
        } else {
            if (account != null) {
                accountId = account.getId();
                domainId = account.getDomainId();
            }
        }

        if (accountId == null) {
            throw new InvalidParameterValueException("Unable to find account for network security group " + groupName + "; failed to authorize ingress.");
        }
      

        if (cidrList == null && groupList == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("At least one cidr or at least one security group needs to be specified");
            }
        	throw new InvalidParameterValueException("At least one cidr or at least one security group needs to be specified");
        }
        
        List<NetworkGroupVO> authorizedGroups = new ArrayList<NetworkGroupVO> ();
        if (groupList != null) {
            Collection userGroupCollection = groupList.values();
            Iterator iter = userGroupCollection.iterator();
            while (iter.hasNext()) {
                HashMap userGroup = (HashMap)iter.next();
        		String group = (String)userGroup.get("group");
        		String authorizedAccountName = (String)userGroup.get("account");
        		if ((group == null) || (authorizedAccountName == null)) {
        			 throw new InvalidParameterValueException("Invalid user group specified, fields 'group' and 'account' cannot be null, please specify groups in the form:  userGroupList[0].group=XXX&userGroupList[0].account=YYY");
        		}

        		Account authorizedAccount = _accountDao.findActiveAccount(authorizedAccountName, domainId);
        		if (authorizedAccount == null) {
        		    if (s_logger.isDebugEnabled()) {
        		        s_logger.debug("Nonexistent account: " + authorizedAccountName + ", domainid: " + domainId + " when trying to authorize ingress for " + groupName + ":" + protocol + ":" + startPortOrType + ":" + endPortOrCode);
        		    }
        		    throw new InvalidParameterValueException("Nonexistent account: " + authorizedAccountName + " when trying to authorize ingress for " + groupName + ":" + protocol + ":" + startPortOrType + ":" + endPortOrCode);
        		}

        		NetworkGroupVO groupVO = _networkGroupDao.findByAccountAndName(authorizedAccount.getId(), group);
        		if (groupVO == null) {
        		    if (s_logger.isDebugEnabled()) {
        		        s_logger.debug("Nonexistent group " + group + " for account " + authorizedAccountName + "/" + domainId);
        		    }
        		    throw new InvalidParameterValueException("Invalid group (" + group + ") given, unable to authorize ingress.");
        		}
        		authorizedGroups.add(groupVO);
        	}
        }
		
        final Transaction txn = Transaction.currentTxn();
		final Set<NetworkGroupVO> authorizedGroups2 = new TreeSet<NetworkGroupVO>(new NetworkGroupVOComparator());

		authorizedGroups2.addAll(authorizedGroups); //Ensure we don't re-lock the same row
		txn.start();
		NetworkGroupVO networkGroup = _networkGroupDao.findByAccountAndName(accountId, groupName);
		if (networkGroup == null) {
			s_logger.warn("Network security group not found: name= " + groupName);
			return null;
		}
		//Prevents other threads/management servers from creating duplicate ingress rules
		NetworkGroupVO networkGroupLock = _networkGroupDao.acquireInLockTable(networkGroup.getId());
		if (networkGroupLock == null)  {
			s_logger.warn("Could not acquire lock on network security group: name= " + groupName);
			return null;
		}
		List<IngressRuleVO> newRules = new ArrayList<IngressRuleVO>();
		try {
			//Don't delete the group from under us.
			networkGroup = _networkGroupDao.lockRow(networkGroup.getId(), false);
			if (networkGroup == null) {
				s_logger.warn("Could not acquire lock on network group " + groupName);
				return null;
			}

			for (final NetworkGroupVO ngVO: authorizedGroups2) {
				final Long ngId = ngVO.getId();
				//Don't delete the referenced group from under us
				if (ngVO.getId() != networkGroup.getId()) {
					final NetworkGroupVO tmpGrp = _networkGroupDao.lockRow(ngId, false);
					if (tmpGrp == null) {
						s_logger.warn("Failed to acquire lock on network group: " + ngId);
						txn.rollback();
						return null;
					}
				}
				IngressRuleVO ingressRule = _ingressRuleDao.findByProtoPortsAndAllowedGroupId(networkGroup.getId(), protocol, startPort, endPort, ngVO.getId());
				if (ingressRule != null) {
					continue; //rule already exists.
				}
				ingressRule  = new IngressRuleVO(networkGroup.getId(), startPort, endPort, protocol, ngVO.getId(), ngVO.getName(), ngVO.getAccountName());
				ingressRule = _ingressRuleDao.persist(ingressRule);
				newRules.add(ingressRule);
			}
			for (String cidr: cidrList) {
				IngressRuleVO ingressRule = _ingressRuleDao.findByProtoPortsAndCidr(networkGroup.getId(),protocol, startPort, endPort, cidr);
				if (ingressRule != null) {
					continue;
				}
				ingressRule  = new IngressRuleVO(networkGroup.getId(), startPort, endPort, protocol, cidr);
				ingressRule = _ingressRuleDao.persist(ingressRule);
				newRules.add(ingressRule);
			}
			if (s_logger.isDebugEnabled()) {
	            s_logger.debug("Added " + newRules.size() + " rules to network group " + groupName);
			}
			txn.commit();
			final Set<Long> affectedVms = new HashSet<Long>();
			affectedVms.addAll(_networkGroupVMMapDao.listVmIdsByNetworkGroup(networkGroup.getId()));
			scheduleRulesetUpdateToHosts(affectedVms, true, null);
			return newRules;
		} catch (Exception e){
			s_logger.warn("Exception caught when adding ingress rules ", e);
			throw new CloudRuntimeException("Exception caught when adding ingress rules", e);
		} finally {
			if (networkGroupLock != null) {
				_networkGroupDao.releaseFromLockTable(networkGroupLock.getId());
			}
		}
	}
	
	@Override
	@DB @SuppressWarnings("rawtypes")
	public boolean revokeNetworkGroupIngress(RevokeNetworkGroupIngressCmd cmd) {
		
		//input validation
		Account account = UserContext.current().getAccount();
		Long userId  = UserContext.current().getUserId();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        Integer startPort = cmd.getStartPort();
        Integer endPort = cmd.getEndPort();
        Integer icmpType = cmd.getIcmpType();
        Integer icmpCode = cmd.getIcmpCode();
        String protocol = cmd.getProtocol();
        String networkGroup = cmd.getNetworkGroupName();
        String cidrList = cmd.getCidrList();
        Map groupList = cmd.getUserNetworkGroupList();
        String [] cidrs = null;
        Long accountId = null;
        Integer startPortOrType = null;
        Integer endPortOrCode = null;
        if (protocol == null) {
        	protocol = "all";
        }
        //FIXME: for exceptions below, add new enums to BaseCmd.PARAM_ to reflect the error condition more precisely
        if (!NetUtils.isValidNetworkGroupProto(protocol)) {
        	s_logger.debug("Invalid protocol specified " + protocol);
        	 throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid protocol " + protocol);
        }
        if ("icmp".equalsIgnoreCase(protocol) ) {
            if ((icmpType == null) || (icmpCode == null)) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid ICMP type/code specified, icmpType = " + icmpType + ", icmpCode = " + icmpCode);
            }
            if (icmpType == -1 && icmpCode != -1) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid icmp type range" );
            } 
            if (icmpCode > 255) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid icmp code " );
            }
            startPortOrType = icmpType;
            endPortOrCode= icmpCode;
        } else if (protocol.equals("all")) {
        	if ((startPort != null) || (endPort != null)) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Cannot specify startPort or endPort without specifying protocol");
            }
        	startPortOrType = 0;
        	endPortOrCode = 0;
        } else {
            if ((startPort == null) || (endPort == null)) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid port range specified, startPort = " + startPort + ", endPort = " + endPort);
            }
            if (startPort == 0 && endPort == 0) {
                endPort = 65535;
            }
            if (startPort > endPort) {
                s_logger.debug("Invalid port range specified: " + startPort + ":" + endPort);
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid port range " );
            }
            if (startPort > 65535 || endPort > 65535 || startPort < -1 || endPort < -1) {
                s_logger.debug("Invalid port numbers specified: " + startPort + ":" + endPort);
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid port numbers " );
            }
            
            if (startPort < 0 || endPort < 0) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid port range " );
            }
            startPortOrType = startPort;
            endPortOrCode= endPort;
        }

        if ((account == null) || isAdmin(account.getType())) {
            if ((accountName != null) && (domainId != null)) {
                // if it's an admin account, do a quick permission check
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Unable to find rules for network security group id = " + networkGroup + ", permission denied.");
                    }
                    throw new ServerApiException(BaseCmd.ACCOUNT_ERROR, "Unable to find rules for network security group id = " + networkGroup + ", permission denied.");
                }
                Account groupOwner =  _accountDao.findActiveAccount(accountName, domainId);
                if (groupOwner == null) {
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Unable to find account " + accountName + " in domain " + domainId);
                }
                accountId = groupOwner.getId();
            } else {
                if (account != null) {
                    accountId = account.getId();
                    domainId = account.getDomainId();
                }
            }
        } else {
            if (account != null) {
                accountId = account.getId();
                domainId = account.getDomainId();
            }
        }

        if (accountId == null) {
            throw new ServerApiException(BaseCmd.PARAM_ERROR, "Unable to find account for network security group " + networkGroup + "; failed to revoke ingress.");
        }

        NetworkGroupVO sg = _networkGroupDao.findByAccountAndName(accountId, networkGroup);
        if (sg == null) {
            s_logger.debug("Unable to find network security group with id " + networkGroup);
            throw new ServerApiException(BaseCmd.PARAM_ERROR, "Unable to find network security group with id " + networkGroup);
        }

        if (cidrList == null && groupList == null) {
        	s_logger.debug("At least one cidr or at least one security group needs to be specified");
        	throw new ServerApiException(BaseCmd.PARAM_ERROR, "At least one cidr or at least one security group needs to be specified");
        }
        List<String> authorizedCidrs = new ArrayList<String>();
        if (cidrList != null) {
        	if (protocol.equals("all")) {
                throw new ServerApiException(BaseCmd.PARAM_ERROR, "Cannot specify cidrs without specifying protocol and ports.");	
        	}
        	cidrs = cidrList.split(",");
        	for (String cidr: cidrs) {
        		if (!NetUtils.isValidCIDR(cidr)) {
                    s_logger.debug( "Invalid cidr (" + cidr + ") given, unable to revoke ingress.");	
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid cidr (" + cidr + ") given, unable to revoke ingress.");	
        		}
        		authorizedCidrs.add(cidr);
        	}
        }

        List<NetworkGroupVO> authorizedGroups = new ArrayList<NetworkGroupVO> ();
        if (groupList != null) {
            Collection userGroupCollection = groupList.values();
            Iterator iter = userGroupCollection.iterator();
            while (iter.hasNext()) {
                HashMap userGroup = (HashMap)iter.next();
        		String group = (String)userGroup.get("group");
        		String authorizedAccountName = (String)userGroup.get("account");
        		if ((group == null) || (authorizedAccountName == null)) {
        			 throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid user group specified, fields 'group' and 'account' cannot be null, please specify groups in the form:  userGroupList[0].group=XXX&userGroupList[0].account=YYY");
        		}

        		Account authorizedAccount = _accountDao.findActiveAccount(authorizedAccountName, domainId);
                if (authorizedAccount == null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Nonexistent account: " + authorizedAccountName + ", domainid: " + domainId + " when trying to revoke ingress for " + networkGroup + ":" + protocol + ":" + startPortOrType + ":" + endPortOrCode);
                    }
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Nonexistent account: " + authorizedAccountName + " when trying to revoke ingress for " + networkGroup + ":" + protocol + ":" + startPortOrType + ":" + endPortOrCode);
                }

                NetworkGroupVO groupVO = _networkGroupDao.findByAccountAndName(authorizedAccount.getId(), group);
                if (groupVO == null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Nonexistent group and/or accountId: " + accountId + ", groupName=" + group);
                    }
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Invalid account/group pair  (" + userGroup + ") given, unable to revoke ingress.");
                }
                authorizedGroups.add(groupVO);
        	}
        }

        // If command is executed via 8096 port, set userId to the id of System account (1)
        if (userId == null) {
            userId = Long.valueOf(1);
        }
		
		if (!_enabled) {
			return false;
		}
		int numDeleted = 0;
		final int numToDelete = cidrList.length() + authorizedGroups.size();
        final Transaction txn = Transaction.currentTxn();

		NetworkGroupVO networkGroupHandle = _networkGroupDao.findByAccountAndName(accountId, networkGroup);
		if (networkGroupHandle == null) {
			s_logger.warn("Network security group not found: name= " + networkGroup);
			return false;
		}
		try {
			txn.start();
			
			networkGroupHandle = _networkGroupDao.acquireInLockTable(networkGroupHandle.getId());
			if (networkGroupHandle == null)  {
				s_logger.warn("Could not acquire lock on network security group: name= " + networkGroup);
				return false;
			}
			for (final NetworkGroupVO ngVO: authorizedGroups) {
				numDeleted += _ingressRuleDao.deleteByPortProtoAndGroup(networkGroupHandle.getId(), protocol, startPort, endPort, ngVO.getId());
			}
			for (final String cidr: cidrs) {
				numDeleted += _ingressRuleDao.deleteByPortProtoAndCidr(networkGroupHandle.getId(), protocol, startPort, endPort, cidr);
			}
			s_logger.debug("revokeNetworkGroupIngress for group: " + networkGroup + ", numToDelete=" + numToDelete + ", numDeleted=" + numDeleted);
			
			final Set<Long> affectedVms = new HashSet<Long>();
			affectedVms.addAll(_networkGroupVMMapDao.listVmIdsByNetworkGroup(networkGroupHandle.getId()));
			scheduleRulesetUpdateToHosts(affectedVms, true, null);
			
			return true;
		} catch (Exception e) {
			s_logger.warn("Exception caught when deleting ingress rules ", e);
			throw new CloudRuntimeException("Exception caught when deleting ingress rules", e);
		} finally {
			if (networkGroup != null) {
				_networkGroupDao.releaseFromLockTable(networkGroupHandle.getId());
			}
			txn.commit();
		}
		
	}
	
	private static boolean isAdmin(short accountType) {
	    return ((accountType == Account.ACCOUNT_TYPE_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN));
	}

	@Override
    public NetworkGroupVO createNetworkGroup(CreateNetworkGroupCmd cmd) throws PermissionDeniedException, InvalidParameterValueException {
        if (!_enabled) {
            return null;
        }

        String accountName = cmd.getAccountName();
	    Long domainId = cmd.getDomainId();
	    Long accountId = null;

	    Account account = UserContext.current().getAccount();
        if (account != null) {
            if ((account.getType() == Account.ACCOUNT_TYPE_ADMIN) || (account.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN)) {
                if ((domainId != null) && (accountName != null)) {
                    if (!_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                        throw new PermissionDeniedException("Unable to create network group in domain " + domainId + ", permission denied.");
                    }

                    Account userAccount = _accountDao.findActiveAccount(accountName, domainId);
                    if (userAccount == null) {
                        throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId + ", failed to create network group " + cmd.getNetworkGroupName());
                    }

                    accountId = userAccount.getId();
                } else {
                    // the admin must be creating a network group for himself/herself
                    if (account != null) {
                        accountId = account.getId();
                        domainId = account.getDomainId();
                        accountName = account.getAccountName();
                    }
                }
            } else {
                accountId = account.getId();
                domainId = account.getDomainId();
                accountName = account.getAccountName();
            }
        }

        // if no account exists in the context, it's a system level command, look up the account
        if (accountId == null) {
            if ((accountName != null) && (domainId != null)) {
                Account userAccount = _accountDao.findActiveAccount(accountName, domainId);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                } else {
                    throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId + ", failed to create network group " + cmd.getNetworkGroupName());
                }
            } else {
                throw new InvalidParameterValueException("Missing account information (account: " + accountName + ", domain: " + domainId + "), failed to create network group " + cmd.getNetworkGroupName());
            }
        }

        if (_networkGroupDao.isNameInUse(accountId, domainId, cmd.getNetworkGroupName())) {
            throw new InvalidParameterValueException("Unable to create network group, a group with name " + cmd.getNetworkGroupName() + " already exisits.");
        }

        return createNetworkGroup(cmd.getNetworkGroupName(), cmd.getDescription(), domainId, accountId, accountName);
	}

	@DB
	@Override
	public NetworkGroupVO createNetworkGroup(String name, String description, Long domainId, Long accountId, String accountName) {
		if (!_enabled) {
			return null;
		}
		final Transaction txn = Transaction.currentTxn();
		AccountVO account = null;
		txn.start();
		try {
			account = _accountDao.acquireInLockTable(accountId); //to ensure duplicate group names are not created.
			if (account == null) {
				s_logger.warn("Failed to acquire lock on account");
				return null;
			}
			NetworkGroupVO group = _networkGroupDao.findByAccountAndName(accountId, name);
			if (group == null){
				group = new NetworkGroupVO(name, description, domainId, accountId, accountName);
				group =  _networkGroupDao.persist(group);
			}
			return group;
		} finally {
			if (account != null) {
				_accountDao.releaseFromLockTable(accountId);
			}
			txn.commit();
		}
		
    }
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		String enabled =_configDao.getValue("direct.attach.network.groups.enabled");
		if ("true".equalsIgnoreCase(enabled)) {
			_enabled = true;
		}
		if (!_enabled) {
			return false;
		}
		_answerListener = new NetworkGroupListener(this, _agentMgr, _workDao);
		_agentMgr.registerForHostEvents(_answerListener, true, true, true);
		
        _serverId = ((ManagementServer)ComponentLocator.getComponent(ManagementServer.Name)).getId();
        _executorPool = Executors.newScheduledThreadPool(10, new NamedThreadFactory("NWGRP"));
        _cleanupExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("NWGRP-Cleanup"));


 		return true;
	}


	@Override
	public String getName() {
		return this.getClass().getName();
	}


	@Override
	public boolean start() {
	    if (!_enabled) {
	        return true;
	    }
		_cleanupExecutor.scheduleAtFixedRate(new CleanupThread(), _timeBetweenCleanups, _timeBetweenCleanups, TimeUnit.SECONDS);
		return true;
	}


	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public NetworkGroupVO createDefaultNetworkGroup(Long accountId) {
		if (!_enabled) {
			return null;
		}
		NetworkGroupVO groupVO = _networkGroupDao.findByAccountAndName(accountId, NetworkGroupManager.DEFAULT_GROUP_NAME);
		if (groupVO == null ) {
			Account accVO = _accountDao.findById(accountId);
			if (accVO != null) {
				return createNetworkGroup(NetworkGroupManager.DEFAULT_GROUP_NAME, NetworkGroupManager.DEFAULT_GROUP_DESCRIPTION, accVO.getDomainId(), accVO.getId(), accVO.getAccountName());
			}
		}
		return groupVO;
	}
	
	@DB
	public void work() {
	    if (s_logger.isTraceEnabled()) {
	        s_logger.trace("Checking the database");
	    }
		final NetworkGroupWorkVO work = _workDao.take(_serverId);
		if (work == null) {
			return;
		}
		Long userVmId = work.getInstanceId();
		UserVm vm = null;
		Long seqnum = null;
		s_logger.info("Working on " + work.toString());
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		try {
			vm = _userVMDao.acquireInLockTable(work.getInstanceId());
			if (vm == null) {
				s_logger.warn("Unable to acquire lock on vm id=" + userVmId);
				return ;
			}
			Long agentId = null;
			VmRulesetLogVO log = _rulesetLogDao.findByVmId(userVmId);
			if (log == null) {
				s_logger.warn("Cannot find log record for vm id=" + userVmId);
				return;
			}
			seqnum = log.getLogsequence();

			if (vm != null && vm.getState() == State.Running) {
				Map<PortAndProto, Set<String>> rules = generateRulesForVM(userVmId);
				agentId = vm.getHostId();
				if (agentId != null ) {
					_rulesetLogDao.findByVmId(work.getInstanceId());
					NetworkIngressRulesCmd cmd = generateRulesetCmd(vm.getInstanceName(), vm.getGuestIpAddress(), vm.getGuestMacAddress(), vm.getId(), generateRulesetSignature(rules), seqnum, rules);
					Commands cmds = new Commands(cmd);
					try {
						_agentMgr.send(agentId, cmds, _answerListener);
					} catch (AgentUnavailableException e) {
						s_logger.debug("Unable to send updates for vm: " + userVmId + "(agentid=" + agentId + ")");
						_workDao.updateStep(work.getInstanceId(), seqnum, Step.Done);
					}
				}
			}
		} finally {
			if (vm != null) {
				_userVMDao.releaseFromLockTable(userVmId);
				_workDao.updateStep(work.getId(),  Step.Done);
			}
			txn.commit();
		}

	
	}

	@Override
	@DB
	public boolean addInstanceToGroups(final Long userVmId, final List<NetworkGroupVO> groups) {
		if (!_enabled) {
			return true;
		}
		if (groups != null) {
			final Set<NetworkGroupVO> uniqueGroups = new TreeSet<NetworkGroupVO>(new NetworkGroupVOComparator());
			uniqueGroups.addAll(groups);
			final Transaction txn = Transaction.currentTxn();
			txn.start();
			UserVm userVm = _userVMDao.acquireInLockTable(userVmId); //ensures that duplicate entries are not created.
			if (userVm == null) {
				s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
			}
			try {
				for (NetworkGroupVO networkGroup:uniqueGroups) {
					//don't let the group be deleted from under us.
					NetworkGroupVO ngrpLock = _networkGroupDao.lockRow(networkGroup.getId(), false);
					if (ngrpLock == null) {
						s_logger.warn("Failed to acquire lock on network group id=" + networkGroup.getId() + " name=" + networkGroup.getName());
						txn.rollback();
						return false;
					}
					if (_networkGroupVMMapDao.findByVmIdGroupId(userVmId, networkGroup.getId()) == null) {
						NetworkGroupVMMapVO groupVmMapVO = new NetworkGroupVMMapVO(networkGroup.getId(), userVmId);
						_networkGroupVMMapDao.persist(groupVmMapVO);
					}
				}
				txn.commit();
				return true;
			} finally {
				if (userVm != null) {
					_userVMDao.releaseFromLockTable(userVmId);
				}
			}
			

        }
		return false;
		
	}

	@Override
	@DB
	public void removeInstanceFromGroups(Long userVmId) {
		if (!_enabled) {
			return;
		}
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		UserVm userVm = _userVMDao.acquireInLockTable(userVmId); //ensures that duplicate entries are not created in addInstance
		if (userVm == null) {
			s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
		}
		int n = _networkGroupVMMapDao.deleteVM(userVmId);
		s_logger.info("Disassociated " + n + " network groups " + " from uservm " + userVmId);
		_userVMDao.releaseFromLockTable(userVmId);
		txn.commit();
	}

	@DB
	@Override
	public boolean deleteNetworkGroup(DeleteNetworkGroupCmd cmd) throws ResourceInUseException, PermissionDeniedException, InvalidParameterValueException{
		String name = cmd.getNetworkGroupName();
		String accountName = cmd.getAccountName();
		Long domainId = cmd.getDomainId();
		Account account = UserContext.current().getAccount();
		
		if (!_enabled) {
			return true;
		}
		
		//Verify input parameters
        Long accountId = null;
        if ((account == null) || isAdmin(account.getType())) {
            if ((accountName != null) && (domainId != null)) {
                // if it's an admin account, do a quick permission check
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Unable to find rules network group " + name + ", permission denied.");
                    }
                    throw new PermissionDeniedException("Unable to network group " + name + ", permission denied.");
                }

                Account groupOwner = _accountDao.findActiveAccount(accountName, domainId);
                if (groupOwner == null) {
                    throw new ServerApiException(BaseCmd.PARAM_ERROR, "Unable to find account " + accountName + " in domain " + domainId);
                }
                accountId = groupOwner.getId();
            } else {
                if (account != null) {
                    accountId = account.getId();
                    domainId = account.getDomainId();
                }
            }
        } else {
            if (account != null) {
                accountId = account.getId();
                domainId = account.getDomainId();
            }
        }

        if (accountId == null) {
            throw new InvalidParameterValueException("Unable to find account for network group " + name + "; failed to delete group.");
        }

        NetworkGroupVO sg = _networkGroupDao.findByAccountAndName(accountId, name);
        if (sg == null) {
            throw new ServerApiException(BaseCmd.PARAM_ERROR, "Unable to find network group " + name + "; failed to delete group.");
        }
        
        Long groupId = sg.getId();
		
		final Transaction txn = Transaction.currentTxn();
		txn.start();
		
		final NetworkGroupVO group = _networkGroupDao.lockRow(groupId, true);
		if (group == null) {
			s_logger.info("Not deleting group -- cannot find id " + groupId);
			return false;
		}
		
		if (group.getName().equalsIgnoreCase(NetworkGroupManager.DEFAULT_GROUP_NAME)) {
			txn.rollback();
			throw new PermissionDeniedException("The network group default is reserved");
		}
		
		List<IngressRuleVO> allowingRules = _ingressRuleDao.listByAllowedNetworkGroupId(groupId);
		if (allowingRules.size() != 0) {
			txn.rollback();
			throw new ResourceInUseException("Cannot delete group when there are ingress rules that allow this group");
		}
		
		List<IngressRuleVO> rulesInGroup = _ingressRuleDao.listByNetworkGroupId(groupId);
		if (rulesInGroup.size() != 0) {
			txn.rollback();
			throw new ResourceInUseException("Cannot delete group when there are ingress rules in this group");
		}
        _networkGroupDao.expunge(groupId);
        txn.commit();
        return true;
	}

    @Override
    public List<NetworkGroupRulesVO> searchForNetworkGroupRules(ListNetworkGroupsCmd cmd) throws PermissionDeniedException, InvalidParameterValueException {
        Account account = UserContext.current().getAccount();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        Long accountId = null;
        Long instanceId = cmd.getVirtualMachineId();
        String networkGroup = cmd.getNetworkGroupName();
        Boolean recursive = Boolean.FALSE;

        // permissions check
        if ((account == null) || isAdmin(account.getType())) {
            if (domainId != null) {
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                    throw new PermissionDeniedException("Unable to list network groups for account " + accountName + " in domain " + domainId + "; permission denied.");
                }
                if (accountName != null) {
                    Account acct = _accountDao.findActiveAccount(accountName, domainId);
                    if (acct != null) {
                        accountId = acct.getId();
                    } else {
                        throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
                    }
                }
            } else if (instanceId != null) {
                UserVmVO userVM = _userVMDao.findById(instanceId);
                if (userVM == null) {
                    throw new InvalidParameterValueException("Unable to list network groups for virtual machine instance " + instanceId + "; instance not found.");
                }
                if ((account != null) && !_domainDao.isChildDomain(account.getDomainId(), userVM.getDomainId())) {
                    throw new PermissionDeniedException("Unable to list network groups for virtual machine instance " + instanceId + "; permission denied.");
                }
            } else if (account != null) {
                // either an admin is searching for their own group, or admin is listing all groups and the search needs to be restricted to domain admin's domain
                if (networkGroup != null) {
                    accountId = account.getId();
                } else if (account.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                    domainId = account.getDomainId();
                    recursive = Boolean.TRUE;
                }
            }
        } else {
            if (instanceId != null) {
                UserVmVO userVM = _userVMDao.findById(instanceId);
                if (userVM == null) {
                    throw new InvalidParameterValueException("Unable to list network groups for virtual machine instance " + instanceId + "; instance not found.");
                }

                if (account != null) {
                    // check that the user is the owner of the VM (admin case was already verified
                    if (account.getId() != userVM.getAccountId()) {
                        throw new PermissionDeniedException("Unable to list network groups for virtual machine instance " + instanceId + "; permission denied.");
                    }
                }
            } else {
                accountId = ((account != null) ? account.getId() : null);
            }
        }

        Filter searchFilter = new Filter(NetworkGroupVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        Object keyword = cmd.getKeyword();

        SearchBuilder<NetworkGroupVO> sb = _networkGroupDao.createSearchBuilder();
        sb.and("accountId", sb.entity().getAccountId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);

        // only do a recursive domain search if the search is not limited by account or instance
        if ((accountId == null) && (instanceId == null) && (domainId != null) && Boolean.TRUE.equals(recursive)) {
            SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
            sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        }

        SearchCriteria<NetworkGroupVO> sc = sb.create();
        if (accountId != null) {
            sc.setParameters("accountId", accountId);
            if (networkGroup != null) {
                sc.setParameters("name", networkGroup);
            } else if (keyword != null) {
                SearchCriteria<NetworkGroupRulesVO> ssc = _networkGroupRulesDao.createSearchCriteria();
                ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
                ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
                sc.addAnd("name", SearchCriteria.Op.SC, ssc);
            }
        } else if (domainId != null) {
            if (Boolean.TRUE.equals(recursive)) {
                DomainVO domain = _domainDao.findById(domainId);
                sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }
        
        List<NetworkGroupVO> networkGroups = _networkGroupDao.search(sc, searchFilter);
        List<NetworkGroupRulesVO> networkRulesList = new ArrayList<NetworkGroupRulesVO>();
        for (NetworkGroupVO group : networkGroups) {
           networkRulesList.addAll(_networkGroupRulesDao.listNetworkRulesByGroupId(group.getId()));
        }
        
       if (instanceId != null) {
            return listNetworkGroupRulesByVM(instanceId.longValue());
       } 
       
        return networkRulesList;
    }

	private List<NetworkGroupRulesVO> listNetworkGroupRulesByVM(long vmId) {
	    List<NetworkGroupRulesVO> results = new ArrayList<NetworkGroupRulesVO>();
	    List<NetworkGroupVMMapVO> networkGroupMappings = _networkGroupVMMapDao.listByInstanceId(vmId);
	    if (networkGroupMappings != null) {
	        for (NetworkGroupVMMapVO networkGroupMapping : networkGroupMappings) {
	            NetworkGroupVO group = _networkGroupDao.findById(networkGroupMapping.getNetworkGroupId());
	            List<NetworkGroupRulesVO> rules = _networkGroupRulesDao.listNetworkGroupRules(group.getAccountId(), networkGroupMapping.getGroupName());
	            if (rules != null) {
	                results.addAll(rules);
	            }
	        }
	    }
	    return results;
	}

	@Override
	public void fullSync(long agentId, HashMap<String, Pair<Long, Long>> newGroupStates) {
		Set<Long> affectedVms = new HashSet<Long>();
		for (String vmName: newGroupStates.keySet()) {
			Long vmId = newGroupStates.get(vmName).first();
			Long seqno = newGroupStates.get(vmName).second();

			VmRulesetLogVO log = _rulesetLogDao.findByVmId(vmId);
			if (log != null && log.getLogsequence() != seqno) {
				affectedVms.add(vmId);
			}
		}
		if (affectedVms.size() > 0){
			s_logger.info("Network Group full sync for agent " + agentId + " found " + affectedVms.size() + " vms out of sync");
			scheduleRulesetUpdateToHosts(affectedVms, false, null);
		}
		
	}
	
	public void cleanupFinishedWork() {
		Date before = new Date(System.currentTimeMillis() - 24*3600*1000l);
		int numDeleted = _workDao.deleteFinishedWork(before);
		if (numDeleted > 0) {
			s_logger.info("Network Group Work cleanup deleted " + numDeleted + " finished work items older than " + before.toString());
		}
		
	}
	


	private void cleanupUnfinishedWork() {
		Date before = new Date(System.currentTimeMillis() - 30*1000l);
		List<NetworkGroupWorkVO> unfinished = _workDao.findUnfinishedWork(before);
		if (unfinished.size() > 0) {
			s_logger.info("Network Group Work cleanup found " + unfinished.size() + " unfinished work items older than " + before.toString());
			Set<Long> affectedVms = new HashSet<Long>();
			for (NetworkGroupWorkVO work: unfinished) {
				affectedVms.add(work.getInstanceId());
			}
			scheduleRulesetUpdateToHosts(affectedVms, false, null);
		} else {
			s_logger.debug("Network Group Work cleanup found no unfinished work items older than " + before.toString());
		}
	}

	@Override
	public String getNetworkGroupsNamesForVm(long vmId) 
	{
		try
		{
			List<NetworkGroupVMMapVO>networkGroupsToVmMap =  _networkGroupVMMapDao.listByInstanceId(vmId);
        	int size = 0;
        	int j=0;		
            StringBuilder networkGroupNames = new StringBuilder();

            if(networkGroupsToVmMap != null)
            {
            	size = networkGroupsToVmMap.size();
            	
            	for(NetworkGroupVMMapVO nG: networkGroupsToVmMap)
            	{
            		//get the group id and look up for the group name
            		NetworkGroupVO currentNetworkGroup = _networkGroupDao.findById(nG.getNetworkGroupId());
            		networkGroupNames.append(currentNetworkGroup.getName());
            	
            		if(j<(size-1))
            		{
            			networkGroupNames.append(",");
            			j++;
            		}
            	}
            }
			
			return networkGroupNames.toString();
		}
		catch (Exception e)
		{
			s_logger.warn("Error trying to get network groups for a vm: "+e);
			return null;
		}

	}
}
