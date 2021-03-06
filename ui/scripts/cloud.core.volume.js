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

function volumeGetSearchParams() {
    var moreCriteria = [];	

	var $advancedSearchPopup = $("#advanced_search_popup");
	if (lastSearchType == "advanced_search" && $advancedSearchPopup.length > 0) {
	    var name = $advancedSearchPopup.find("#adv_search_name").val();							
		if (name!=null && trim(name).length > 0) 
			moreCriteria.push("&name="+todb(name));	
		
		var zone = $advancedSearchPopup.find("#adv_search_zone").val();	
	    if (zone!=null && zone.length > 0) 
			moreCriteria.push("&zoneId="+zone);	
		
		if ($advancedSearchPopup.find("#adv_search_pod_li").css("display") != "none") {	
		    var pod = $advancedSearchPopup.find("#adv_search_pod").val();		
	        if (pod!=null && pod.length > 0) 
			    moreCriteria.push("&podId="+pod);
        }
        
        if ($advancedSearchPopup.find("#adv_search_domain_li").css("display") != "none") {		
		    var domainId = $advancedSearchPopup.find("#adv_search_domain").val();		
		    if (domainId!=null && domainId.length > 0) 
			    moreCriteria.push("&domainid="+domainId);	
    	}	
    	
		if ($advancedSearchPopup.find("#adv_search_account_li").css("display") != "none") {	
		    var account = $advancedSearchPopup.find("#adv_search_account").val();		
		    if (account!=null && account.length > 0) 
			    moreCriteria.push("&account="+account);		
		}	
	} 
	else {     			    		
	    var searchInput = $("#basic_search").find("#search_input").val();	 
        if (lastSearchType == "basic_search" && searchInput != null && searchInput.length > 0) {	           
            moreCriteria.push("&keyword="+todb(searchInput));	       
        }        
	}
	
	return moreCriteria.join("");          
}

function afterLoadVolumeJSP() {
    initDialog("dialog_create_template", 400); 
    initDialog("dialog_create_snapshot");        
    initDialog("dialog_recurring_snapshot", 420);	    
    initDialog("dialog_add_volume");	
    initDialog("dialog_attach_volume");	
    initDialog("dialog_add_volume_from_snapshot");	
    initDialog("dialog_create_template_from_snapshot", 400);    
	initDialog("dialog_confirmation_delete_snapshot");
	        
    $.ajax({
        data: createURL("command=listOsTypes"),
	    dataType: "json",
	    success: function(json) {
		    types = json.listostypesresponse.ostype;
		    if (types != null && types.length > 0) {
			    var osTypeField1 = $("#dialog_create_template #create_template_os_type").empty();
			    var osTypeField2 = $("#dialog_create_template_from_snapshot #os_type").empty();	
			    for (var i = 0; i < types.length; i++) {
				    osTypeField1.append("<option value='" + types[i].id + "'>" + types[i].description + "</option>");
				    osTypeField2.append("<option value='" + types[i].id + "'>" + types[i].description + "</option>");
			    }
		    }	
	    }
    });   
     
    $.ajax({
        data: createURL("command=listZones&available=true"),
	    dataType: "json",
	    success: function(json) {
		    var zones = json.listzonesresponse.zone;
		    var volumeZoneSelect = $("#dialog_add_volume").find("#volume_zone").empty();			
		    if (zones != null && zones.length > 0) {
		        for (var i = 0; i < zones.length; i++) {
			        volumeZoneSelect.append("<option value='" + zones[i].id + "'>" + fromdb(zones[i].name) + "</option>"); 
		        }
		    }				
	    }
	});	
	
	$.ajax({
        data: createURL("command=listDiskOfferings"),
	    dataType: "json",
	    success: function(json) {			    
	        var offerings = json.listdiskofferingsresponse.diskoffering;								
		    var volumeDiskOfferingSelect = $("#dialog_add_volume").find("#volume_diskoffering").empty();	
		    if (offerings != null && offerings.length > 0) {								
				for (var i = 0; i < offerings.length; i++) {		
					var $option = $("<option value='" + offerings[i].id + "'>" + fromdb(offerings[i].displaytext) + "</option>");	
					$option.data("jsonObj", offerings[i]);	
					volumeDiskOfferingSelect.append($option); 
				}	
				$("#dialog_add_volume").find("#volume_diskoffering").change();	
			}	
	    }
    });	 
    
    $("#dialog_add_volume").find("#volume_diskoffering").unbind("change").bind("change", function(event) {        
        var jsonObj = $(this).find("option:selected").data("jsonObj");
        if(jsonObj.isCustomized == true) {
            $("#dialog_add_volume").find("#size_container").show();
        }
        else {
            $("#dialog_add_volume").find("#size_container").hide();  
            $("#dialog_add_volume").find("#size").val("");
        }      
    });  
      
    //add button ***
    $("#midmenu_add_link").find("#label").text("Add Volume"); 
    $("#midmenu_add_link").show();     
    $("#midmenu_add_link").unbind("click").bind("click", function(event) {   
        $("#dialog_add_volume")
	    .dialog('option', 'buttons', { 			    
		    "Add": function() { 
		        var thisDialog = $(this);
		    			            										
		        // validate values							
			    var isValid = true;									
			    isValid &= validateString("Name", thisDialog.find("#add_volume_name"), thisDialog.find("#add_volume_name_errormsg"));					    
			    if(thisDialog.find("#size_container").css("display") != "none")
			        isValid &= validateNumber("Size", thisDialog.find("#size"), thisDialog.find("#size_errormsg"));				    			
			    if (!isValid) return;
			    
			    thisDialog.dialog("close");		
				
				var array1 = [];
				
				var name = thisDialog.find("#add_volume_name").val();	
				array1.push("&name="+todb(name));
								
			    var zoneId = thisDialog.find("#volume_zone").val();	
			    array1.push("&zoneId="+zoneId);
			    				    				
			    var diskofferingId = thisDialog.find("#volume_diskoffering").val();	
			    array1.push("&diskOfferingId="+diskofferingId);
			    
			    if(thisDialog.find("#size_container").css("display") != "none") {
			        var size = thisDialog.find("#size").val()
			        array1.push("&size="+size);
			    }
				
				var $midmenuItem1 = beforeAddingMidMenuItem() ;
				    					
			    $.ajax({
				    data: createURL("command=createVolume"+array1.join("")), 
				    dataType: "json",
				    success: function(json) {						        
				        var jobId = json.createvolumeresponse.jobid;				        
				        var timerKey = "createVolumeJob_"+jobId;
							    
				        $("body").everyTime(2000, timerKey, function() {
						    $.ajax({
							    data: createURL("command=queryAsyncJobResult&jobId="+json.createvolumeresponse.jobid),
							    dataType: "json",
							    success: function(json) {										       						   
								    var result = json.queryasyncjobresultresponse;
								    if (result.jobstatus == 0) {
									    return; //Job has not completed
								    } else {											    
									    $("body").stopTime(timerKey);
									    if (result.jobstatus == 1) {
										    // Succeeded										   
										    volumeToMidmenu(result.jobresult.volume, $midmenuItem1);
						                    bindClickToMidMenu($midmenuItem1, volumeToRightPanel, getMidmenuId);  
						                    afterAddingMidMenuItem($midmenuItem1, true);	         
									    } else if (result.jobstatus == 2) {
									        afterAddingMidMenuItem($midmenuItem1, false, fromdb(result.jobresult.errortext));										        								   				    
									    }
								    }
							    },
							    error: function(XMLHttpResponse) {
								    $("body").stopTime(timerKey);
									handleError(XMLHttpResponse, function() {
										afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));
									});
							    }
						    });
					    }, 0);						    					
				    },
				    error: function(XMLHttpResponse) {
						handleError(XMLHttpResponse, function() {
							afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));	
						});
				    }
			    });
		    }, 
		    "Cancel": function() { 				        				        
			    $(this).dialog("close"); 
		    } 
	    }).dialog("open");
	    
        return false;
    });  
       
	$("#snapshot_interval").change(function(event) {
		var thisElement = $(this);
		var snapshotInterval = thisElement.val();
		var jsonObj = thisElement.data("jsonObj");
		var $dialog = $("#dialog_recurring_snapshot");
		switch (snapshotInterval) {
			case "-1":
			    $dialog.find("#snapshot_form").hide();
				break;
			case "0": 
	            $dialog.find("#edit_time_colon, #edit_hour_container, #edit_meridiem_container, #edit_day_of_week_container, #edit_day_of_month_container").hide(); 
	            $dialog.find("#edit_past_the_hour, #edit_minute_container").show();	
				if (jsonObj != null) {
					$dialog.find("#edit_minute").val(jsonObj.schedule);            
					$dialog.find("#edit_max").val(jsonObj.maxsnaps); 
					$dialog.find("#edit_timezone").val(jsonObj.timezone);
				}
				$dialog.find("#snapshot_form").show();
	            break;
	        case "1":
	            $dialog.find("#edit_past_the_hour, #edit_day_of_week_container, #edit_day_of_month_container").hide(); 
	            $dialog.find("#edit_minute_container, #edit_hour_container, #edit_meridiem_container").show();	
				
				if (jsonObj != null) {
					var parts = jsonObj.schedule.split(":");
					var hour12, meridiem;
					var hour24 = parts[1];                                            
					if(hour24 < 12) {
						hour12 = hour24;
						meridiem = "AM";                                               
					}   
					else {
						hour12 = hour24 - 12;
						meridiem = "PM"
					}											
					if (hour12 < 10 && hour12.toString().length==1) 
						hour12 = "0"+hour12.toString();
									
					$dialog.find("#edit_minute").val(parts[0]);
					$dialog.find("#edit_hour").val(hour12); 
					$dialog.find("#edit_meridiem").val(meridiem);          
					$dialog.find("#edit_max").val(jsonObj.maxsnaps); 
					$dialog.find("#edit_timezone").val(jsonObj.timezone); 
				}
				$dialog.find("#snapshot_form").show();
	            break;
	        case "2":
	            $dialog.find("#edit_past_the_hour, #edit_day_of_month_container").hide(); 
	            $dialog.find("#edit_minute_container, #edit_hour_container, #edit_meridiem_container, #edit_day_of_week_container").show();		           
	            
				if (jsonObj != null) {
					var parts = jsonObj.schedule.split(":");
					var hour12, meridiem;
					var hour24 = parts[1];
					if(hour24 < 12) {
						hour12 = hour24;  
						meridiem = "AM";                                               
					}   
					else {
						hour12 = hour24 - 12;
						meridiem = "PM"
					}
					if (hour12 < 10 && hour12.toString().length==1) 
						hour12 = "0"+hour12.toString();
						
					$dialog.find("#edit_minute").val(parts[0]);
					$dialog.find("#edit_hour").val(hour12); 
					$dialog.find("#edit_meridiem").val(meridiem); 	
					$dialog.find("#edit_day_of_week").val(parts[2]);         
					$dialog.find("#edit_max").val(jsonObj.maxsnaps); 
					$dialog.find("#edit_timezone").val(jsonObj.timezone); 
				}
				$dialog.find("#snapshot_form").show();
	            break;
	        case "3":
	            $dialog.find("#edit_past_the_hour, #edit_day_of_week_container").hide(); 
	            $dialog.find("#edit_minute_container, #edit_hour_container, #edit_meridiem_container, #edit_day_of_month_container").show();		           
	            
				if (jsonObj != null) {
					var parts = jsonObj.schedule.split(":");
					var hour12, meridiem;
					var hour24 = parts[1];
					if(hour24 < 12) {
						hour12 = hour24;  
						meridiem = "AM";                                               
					}   
					else {
						hour12 = hour24 - 12;
						meridiem = "PM"
					}
					if (hour12 < 10 && hour12.toString().length==1) 
						hour12 = "0"+hour12.toString();
					$dialog.find("#edit_minute").val(parts[0]);
					$dialog.find("#edit_hour").val(hour12); 
					$dialog.find("#edit_meridiem").val(meridiem); 	
					$dialog.find("#edit_day_of_month").val(parts[2]);         
					$dialog.find("#edit_max").val(jsonObj.maxsnaps); 
					$dialog.find("#edit_timezone").val(jsonObj.timezone); 
				}
				$dialog.find("#snapshot_form").show();
	            break;
		}
	});
	// *** recurring snapshot dialog - event binding (end) ******************************	    
         
    //***** switch between different tabs (begin) ********************************************************************
    var tabArray = [$("#tab_details"), $("#tab_snapshot")];
    var tabContentArray = [$("#tab_content_details"), $("#tab_content_snapshot")];
    var afterSwitchFnArray = [volumeJsonToDetailsTab, volumeJsonToSnapshotTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);       
    //***** switch between different tabs (end) **********************************************************************    
}

function volumeToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_storage_volume.png");		
    
    $midmenuItem1.find("#first_row").text(fromdb(jsonObj.name).substring(0,25)); 
    $midmenuItem1.find("#second_row").text(jsonObj.type.substring(0,25));  
}

function volumeToRightPanel($midmenuItem1) {  
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);  
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);  
    $("#tab_details").click();   
}
 
function volumeJsonToDetailsTab(){  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null)
        return;
     
    var $thisTab = $("#right_panel_content #tab_content_details");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
    
    var id = jsonObj.id;
        
    $.ajax({
        data: createURL("command=listVolumes&id="+id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listvolumesresponse.volume;
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);                     
            }
        }
    });      
           
    $thisTab.find("#id").text(fromdb(jsonObj.id));
    $thisTab.find("#name").text(fromdb(jsonObj.name));    
    $thisTab.find("#zonename").text(fromdb(jsonObj.zonename));    
    $thisTab.find("#device_id").text(fromdb(jsonObj.deviceid));   
    $thisTab.find("#state").text(fromdb(jsonObj.state));    
    $thisTab.find("#storage").text(fromdb(jsonObj.storage));
    $thisTab.find("#account").text(fromdb(jsonObj.account));  
	$thisTab.find("#domain").text(fromdb(jsonObj.domain));
    $thisTab.find("#type").text(fromdb(jsonObj.type) + " (" + fromdb(jsonObj.storagetype) + " storage)");
    $thisTab.find("#size").text((jsonObj.size == "0") ? "" : convertBytes(jsonObj.size));	    
    if (jsonObj.virtualmachineid == null) 
		$thisTab.find("#vm_name").text("detached");
	else 
		$thisTab.find("#vm_name").text(getVmName(jsonObj.vmname, jsonObj.vmdisplayname) + " (" + fromdb(jsonObj.vmstate) + ")");		
    setDateField(jsonObj.created, $thisTab.find("#created"));	
       
    //actions ***    
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();
        
    buildActionLinkForTab("Take Snapshot", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab);	//show take snapshot
    buildActionLinkForTab("Recurring Snapshot", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab);	//show Recurring Snapshot
    
    if(jsonObj.state != "Creating" && jsonObj.state != "Corrupted" && jsonObj.name != "attaching") {
        if(jsonObj.type=="ROOT") {
            if (jsonObj.vmstate == "Stopped") { 
                buildActionLinkForTab("Create Template", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab);
            }
        } 
        else { 
	        if (jsonObj.virtualmachineid != null) {
		        if (jsonObj.storagetype == "shared" && (jsonObj.vmstate == "Running" || jsonObj.vmstate == "Stopped")) {
			        buildActionLinkForTab("Detach Disk", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab); //show detach disk
		        }
	        } else {
		        // Disk not attached
		        if (jsonObj.storagetype == "shared") {
			        buildActionLinkForTab("Attach Disk", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab);   //show attach disk
    			    			  		    
			        if(jsonObj.vmname == null || jsonObj.vmname == "none")
			            buildActionLinkForTab("Delete Volume", volumeActionMap, $actionMenu, $midmenuItem1, $thisTab); //show delete volume
		        }
	        }
        }
    }
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();     
} 

function volumeJsonToSnapshotTab() {       		
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if($midmenuItem1 == null)
	    return;
	
	var jsonObj = $midmenuItem1.data("jsonObj");	
	if(jsonObj == null)
	    return;
	
	var $thisTab = $("#right_panel_content").find("#tab_content_snapshot");	    
	$thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
    
    $.ajax({
		cache: false,
		data: createURL("command=listSnapshots&volumeid="+fromdb(jsonObj.id)),
		dataType: "json",
		success: function(json) {							    
			var items = json.listsnapshotsresponse.snapshot;																						
			if (items != null && items.length > 0) {
			    var $container = $thisTab.find("#tab_container").empty();
				var template = $("#snapshot_tab_template");				
				for (var i = 0; i < items.length; i++) {
					var newTemplate = template.clone(true);	               
	                volumeSnapshotJSONToTemplate(items[i], newTemplate); 
	                $container.append(newTemplate.show());	
				}			
			}	
			$thisTab.find("#tab_spinning_wheel").hide();    
            $thisTab.find("#tab_container").show();    			
		}
	});
} 
 
function volumeSnapshotJSONToTemplate(jsonObj, template) {
    template.data("jsonObj", jsonObj);     
    template.attr("id", "volume_snapshot_"+fromdb(jsonObj.id)).data("volumeSnapshotId", fromdb(jsonObj.id));    
    template.find("#grid_header_title").text(fromdb(jsonObj.name));			   
    template.find("#id").text(fromdb(jsonObj.id));
    template.find("#name").text(fromdb(jsonObj.name));			      
    template.find("#volumename").text(fromdb(jsonObj.volumename));	
    template.find("#intervaltype").text(fromdb(jsonObj.intervaltype));	    		   
    template.find("#account").text(fromdb(jsonObj.account));
    template.find("#domain").text(fromdb(jsonObj.domain));    
    setDateField(jsonObj.created, template.find("#created"));	 
	
	var $actionLink = template.find("#snapshot_action_link");		
	$actionLink.bind("mouseover", function(event) {
        $(this).find("#snapshot_action_menu").show();    
        return false;
    });
    $actionLink.bind("mouseout", function(event) {
        $(this).find("#snapshot_action_menu").hide();    
        return false;
    });		
	
	var $actionMenu = $actionLink.find("#snapshot_action_menu");
    $actionMenu.find("#action_list").empty();	
    
    buildActionLinkForSubgridItem("Create Volume", volumeSnapshotActionMap, $actionMenu, template);	
    buildActionLinkForSubgridItem("Delete Snapshot", volumeSnapshotActionMap, $actionMenu, template);	
    buildActionLinkForSubgridItem("Create Template", volumeSnapshotActionMap, $actionMenu, template);	
} 
 
function volumeClearRightPanel() {       
    volumeJsonClearDetailsTab();   
} 
  
function volumeJsonClearDetailsTab(){   
    var $thisTab = $("#right_panel_content").find("#tab_content_details");  
    $thisTab.find("#id").text("");
    $thisTab.find("#name").text("");    
    $thisTab.find("#zonename").text("");    
    $thisTab.find("#device_id").text("");   
    $thisTab.find("#state").text("");    
    $thisTab.find("#storage").text("");
    $thisTab.find("#account").text(""); 
    $thisTab.find("#type").text("");
    $thisTab.find("#size").text("");		
    $thisTab.find("#vm_name").text("");
    $thisTab.find("#created").text("");
    $thisTab.find("#domain").text("");
}
   
var volumeActionMap = {  
    "Attach Disk": {
        isAsyncJob: true,
        asyncJobResponse: "attachvolumeresponse",            
        dialogBeforeActionFn : doAttachDisk,
        inProcessText: "Attaching disk....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {                
            var jsonObj = json.queryasyncjobresultresponse.jobresult.volume;  
            volumeToMidmenu(jsonObj, $midmenuItem1);
            volumeJsonToDetailsTab($midmenuItem1);   
        }
    },
    "Detach Disk": {
        api: "detachVolume",            
        isAsyncJob: true,
        asyncJobResponse: "detachvolumeresponse",
        inProcessText: "Detaching disk....",
        afterActionSeccessFn: function(json, $midmenuItem1, id){   
            var jsonObj = json.queryasyncjobresultresponse.jobresult.volume;     
            volumeToMidmenu(jsonObj,  $midmenuItem1);
            volumeJsonToDetailsTab($midmenuItem1);   
        }
    },
    "Create Template": {
        isAsyncJob: true,
        asyncJobResponse: "createtemplateresponse",            
        dialogBeforeActionFn : doCreateTemplateFromVolume,
        inProcessText: "Creating template....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {}   
    },
    "Delete Volume": {
        api: "deleteVolume",            
        isAsyncJob: false,        
        inProcessText: "Deleting volume....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
            $midmenuItem1.slideUp("slow", function() {
                $(this).remove();
            });    
            clearRightPanel();
            volumeClearRightPanel();
        }
    },
    "Take Snapshot": {
        isAsyncJob: true,
        asyncJobResponse: "createsnapshotresponse",            
        dialogBeforeActionFn : doTakeSnapshot,
        inProcessText: "Taking Snapshot....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {}   
    },
    "Recurring Snapshot": {                 
        dialogBeforeActionFn : doRecurringSnapshot 
    }   
}   

function doCreateTemplateFromVolume($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
    $("#dialog_create_template").find("#volume_name").text(jsonObj.name);
    
	$("#dialog_create_template")
	.dialog('option', 'buttons', { 						
		"Create": function() { 		   
		    var thisDialog = $(this);
		    thisDialog.dialog("close"); 
									
			// validate values
	        var isValid = true;					
	        isValid &= validateString("Name", thisDialog.find("#create_template_name"), thisDialog.find("#create_template_name_errormsg"));
			isValid &= validateString("Display Text", thisDialog.find("#create_template_desc"), thisDialog.find("#create_template_desc_errormsg"));			
	        if (!isValid) return;		
	        
	        var name = trim(thisDialog.find("#create_template_name").val());
			var desc = trim(thisDialog.find("#create_template_desc").val());
			var osType = thisDialog.find("#create_template_os_type").val();					
			var isPublic = thisDialog.find("#create_template_public").val();
            var password = thisDialog.find("#create_template_password").val();				
			
			var id = $midmenuItem1.data("jsonObj").id;			
			var apiCommand = "command=createTemplate&volumeId="+id+"&name="+todb(name)+"&displayText="+todb(desc)+"&osTypeId="+osType+"&isPublic="+isPublic+"&passwordEnabled="+password;
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);					
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");
}   

function doTakeSnapshot($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_create_snapshot")					
    .dialog('option', 'buttons', { 					    
	    "Confirm": function() { 	
	        $(this).dialog("close");	
	    	
            var id = $midmenuItem1.data("jsonObj").id;	
			var apiCommand = "command=createSnapshot&volumeid="+id;
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	
	    },
	    "Cancel": function() { 					        
		    $(this).dialog("close"); 
	    } 
    }).dialog("open");	  
}		
	
function clearTopPanel(target) { // "target == null" means target at all (hourly + daily + weekly + monthly)
    var dialogBox = $("#dialog_recurring_snapshot");
    if(target == "hourly" || target == null) {
        dialogBox.find("#dialog_snapshot_hourly_info_unset").show();
	    dialogBox.find("#dialog_snapshot_hourly_info_set").hide();   
	    dialogBox.find("#read_hourly_max, #read_hourly_minute").text("N/A"); 	                  
        dialogBox.find("#hourly_edit_link, #hourly_delete_link").data("intervalType", "hourly").data("max", "").data("timezone", (g_timezone==null)?"Etc/GMT+12":g_timezone).data("minute", "00"); 
    }                
    if(target == "daily" || target == null) {   
        dialogBox.find("#dialog_snapshot_daily_info_unset").show();
	    dialogBox.find("#dialog_snapshot_daily_info_set").hide();
	    dialogBox.find("#read_daily_max, #read_daily_minute, #read_daily_hour, #read_daily_meridiem").text("N/A");  
        dialogBox.find("#daily_edit_link, #daily_delete_link").data("intervalType", "daily").data("max", "").data("timezone", (g_timezone==null)?"Etc/GMT+12":g_timezone).data("minute", "00").data("hour12", "00").data("meridiem", "AM");                                   
    }                
    if(target == "weekly" || target == null) {    
        dialogBox.find("#dialog_snapshot_weekly_info_unset").show();
	    dialogBox.find("#dialog_snapshot_weekly_info_set").hide();
	    dialogBox.find("#read_weekly_max, #read_weekly_minute, #read_weekly_hour, #read_weekly_meridiem, #read_weekly_day_of_week").text("N/A");     
        dialogBox.find("#weekly_edit_link, #weekly_delete_link").data("intervalType", "weekly").data("max", "").data("timezone", (g_timezone==null)?"Etc/GMT+12":g_timezone).data("minute", "00").data("hour12", "00").data("meridiem", "AM").data("dayOfWeek", "1");     
    }                
    if(target == "monthly" || target == null) {    
        dialogBox.find("#dialog_snapshot_monthly_info_unset").show();
	    dialogBox.find("#dialog_snapshot_monthly_info_set").hide();
	    dialogBox.find("#read_monthly_max, #read_monthly_minute, #read_monthly_hour, #read_monthly_meridiem, #read_monthly_day_of_month").text("N/A");  
        dialogBox.find("#monthly_edit_link, #monthly_delete_link").data("intervalType", "monthly").data("max", "").data("timezone", (g_timezone==null)?"Etc/GMT+12":g_timezone).data("minute", "00").data("hour12", "00").data("meridiem", "AM").data("dayOfMonth", "1");                                                                
    }
}

function clearBottomPanel() {	
    var dialogBox = $("#dialog_recurring_snapshot");
		    
    dialogBox.find("#edit_hour").val("00");
    cleanErrMsg(dialogBox.find("#edit_hour"), dialogBox.find("#edit_time_errormsg"));
    
    dialogBox.find("#edit_minute").val("00");
    cleanErrMsg(dialogBox.find("#edit_minute"), dialogBox.find("#edit_time_errormsg"));
    
    dialogBox.find("#edit_meridiem").val("AM");
    		        
    dialogBox.find("#edit_max").val("");	
    cleanErrMsg(dialogBox.find("#edit_max"), dialogBox.find("#edit_max_errormsg"));
    
    dialogBox.find("#edit_timezone").val((g_timezone==null)?"Etc/GMT+12":g_timezone); 
    cleanErrMsg(dialogBox.find("#edit_timezone"), dialogBox.find("#edit_timezone_errormsg"));
    	        
    dialogBox.find("#edit_day_of_week").val("1");
    cleanErrMsg(dialogBox.find("#edit_day_of_week"), dialogBox.find("#edit_day_of_week_errormsg"));
    
    dialogBox.find("#edit_day_of_month").val("1");
    cleanErrMsg(dialogBox.find("#edit_day_of_month"), dialogBox.find("#edit_day_of_month_errormsg"));
}	   
	
function doRecurringSnapshot($actionLink, $detailsTab, $midmenuItem1) {     
	var volumeId = $midmenuItem1.data("jsonObj").id;
	
	var dialogBox = $("#dialog_recurring_snapshot"); 
	clearTopPanel();
	
	$.ajax({
        data: createURL("command=listSnapshotPolicies&volumeid="+volumeId),
        dataType: "json",
        async: false,
        success: function(json) {								
            var items = json.listsnapshotpoliciesresponse.snapshotpolicy;
			var $snapInterval = dialogBox.find("#snapshot_interval");
            if(items!=null && items.length>0) {
				var item = items[0]; // We only expect a single policy.
				$snapInterval.val(item.intervaltype).data("jsonObj", item);
            } else {
				$snapInterval.val("-1").data("jsonObj", null);
			}
			clearBottomPanel();
			$snapInterval.change();
			
			dialogBox.dialog('option', 'buttons', { 
				"Apply": function() {
					var thisDialog = $(this);		   
					var volumeId = thisDialog.data("volumeId");
					var bottomPanel = thisDialog.find("#dialog_snapshotright");
				
					var intervalType = thisDialog.find("#snapshot_interval").val();
					var minute, hour12, hour24, meridiem, dayOfWeek, dayOfWeekString, dayOfMonth, schedule, max, timezone;   			                   
					switch(intervalType) {
						 case "-1":
							var $snapshotInterval = $(this).find("#snapshot_interval");
							var jsonObj = $snapshotInterval.data("jsonObj");                 
							if(jsonObj != null) {
								$.ajax({
									data: createURL("command=deleteSnapshotPolicies&id="+jsonObj.id),
									dataType: "json",                        
									success: function(json) {        
										$snapshotInterval.val("-1");
									},
									error: function(XMLHttpResponse) {                                                   					
										handleError(XMLHttpResponse);					
									}
								});	 
							}
							thisDialog.dialog("close");
							return false;
						 case "0":
							 var isValid = true;	 
							 isValid &= validateNumber("Keep # of snapshots", bottomPanel.find("#edit_max"), bottomPanel.find("#edit_max_errormsg"));	    	
							 if (!isValid) return;
							 intervalType = "hourly";
							 minute = bottomPanel.find("#edit_minute").val();		                     
							 schedule = minute;		                    
							 max = bottomPanel.find("#edit_max").val();	
							 timezone = bottomPanel.find("#edit_timezone").val();			                                                      
							 break;
							 
						 case "1":
							 var isValid = true;	 
							 isValid &= validateNumber("Keep # of snapshots", bottomPanel.find("#edit_max"), bottomPanel.find("#edit_max_errormsg"));	    	
							 if (!isValid) return;
							 intervalType = "daily";
							 minute = bottomPanel.find("#edit_minute").val();		
							 hour12 = bottomPanel.find("#edit_hour").val();
							 meridiem = bottomPanel.find("#edit_meridiem").val();			                    
							 if(meridiem=="AM")	 
								 hour24 = hour12;
							 else //meridiem=="PM"	 
								 hour24 = (parseInt(hour12)+12).toString();                
							 schedule = minute + ":" + hour24;		                    
							 max = bottomPanel.find("#edit_max").val();	
							 timezone = bottomPanel.find("#edit_timezone").val();		
							 break;
							 
						 case "2":
							 var isValid = true;	 
							 isValid &= validateNumber("Keep # of snapshots", bottomPanel.find("#edit_max"), bottomPanel.find("#edit_max_errormsg"));	    	
							 if (!isValid) return;
							 intervalType = "weekly";
							 minute = bottomPanel.find("#edit_minute").val();		
							 hour12 = bottomPanel.find("#edit_hour").val();
							 meridiem = bottomPanel.find("#edit_meridiem").val();			                    
							 if(meridiem=="AM")	 
								 hour24 = hour12;
							 else //meridiem=="PM"	 
								 hour24 = (parseInt(hour12)+12).toString();    
							 dayOfWeek = bottomPanel.find("#edit_day_of_week").val();  
							 dayOfWeekString = bottomPanel.find("#edit_day_of_week option:selected").text();
							 schedule = minute + ":" + hour24 + ":" + dayOfWeek;		                    
							 max = bottomPanel.find("#edit_max").val();	
							 timezone = bottomPanel.find("#edit_timezone").val();	
							 break;
							 
						 case "3":
							 var isValid = true;	 
							 isValid &= validateNumber("Keep # of snapshots", bottomPanel.find("#edit_max"), bottomPanel.find("#edit_max_errormsg"));	    	
							 if (!isValid) return;
							 intervalType = "monthly";
							 minute = bottomPanel.find("#edit_minute").val();		
							 hour12 = bottomPanel.find("#edit_hour").val();
							 meridiem = bottomPanel.find("#edit_meridiem").val();			                    
							 if(meridiem=="AM")	 
								 hour24 = hour12;
							 else //meridiem=="PM"	 
								 hour24 = (parseInt(hour12)+12).toString();    
							 dayOfMonth = bottomPanel.find("#edit_day_of_month").val();  		                     
							 schedule = minute + ":" + hour24 + ":" + dayOfMonth;		                    
							 max = bottomPanel.find("#edit_max").val();	
							 timezone = bottomPanel.find("#edit_timezone").val();			                    
							 break;		                
					}	
					var thisLink;
					$.ajax({
						data: createURL("command=createSnapshotPolicy&intervaltype="+intervalType+"&schedule="+schedule+"&volumeid="+volumeId+"&maxsnaps="+max+"&timezone="+todb(timezone)),
						dataType: "json",                        
						success: function(json) {	
							thisDialog.dialog("close");								
						},
						error: function(XMLHttpResponse) {                            					
							handleError(XMLHttpResponse);					
						}
					});	 
				},
				"Disable": function() {
					var $snapshotInterval = $(this).find("#snapshot_interval");
					var jsonObj = $snapshotInterval.data("jsonObj");                 
					if(jsonObj != null) {
						$.ajax({
							data: createURL("command=deleteSnapshotPolicies&id="+jsonObj.id),
							dataType: "json",                        
							success: function(json) {        
								$snapshotInterval.val("-1");
							},
							error: function(XMLHttpResponse) {                                                   					
								handleError(XMLHttpResponse);					
							}
						});	 
					}
					$(this).dialog("close"); 
				},
				"Close": function() { 
					$(this).dialog("close"); 
				}
			}).dialog("open").data("volumeId", volumeId);
        },
        error: function(XMLHttpResponse) {			                   					
            handleError(XMLHttpResponse);					
        }
    });   	    
}	

function populateVirtualMachineField(domainId, account, zoneId) {        
    $.ajax({
	    cache: false,
	    data: createURL("command=listVirtualMachines&state=Running&zoneid="+zoneId+"&domainid="+domainId+"&account="+account),
	    dataType: "json",
	    success: function(json) {			    
		    var instances = json.listvirtualmachinesresponse.virtualmachine;				
		    var volumeVmSelect = $("#dialog_attach_volume").find("#volume_vm").empty();					
		    if (instances != null && instances.length > 0) {
			    for (var i = 0; i < instances.length; i++) {
				    volumeVmSelect.append("<option value='" + instances[i].id + "'>" + getVmName(instances[i].name, instances[i].displayname) + "</option>"); 
			    }				    
		    }
			$.ajax({
				cache: false,
				data: createURL("command=listVirtualMachines&state=Stopped&zoneid="+zoneId+"&domainid="+domainId+"&account="+account),
				dataType: "json",
				success: function(json) {			    
					var instances = json.listvirtualmachinesresponse.virtualmachine;								
					if (instances != null && instances.length > 0) {
						for (var i = 0; i < instances.length; i++) {
							volumeVmSelect.append("<option value='" + instances[i].id + "'>" + getVmName(instances[i].name, instances[i].displayname) + "</option>");
						}				    
					}
				}
			});
	    }
    });
}		

function doAttachDisk($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");    
    populateVirtualMachineField(jsonObj.domainid, jsonObj.account, jsonObj.zoneid);
	    
    $("#dialog_attach_volume")					
    .dialog('option', 'buttons', { 					    
	    "OK": function() { 	
	        var $thisDialog = $(this);
		    				
			var isValid = true;				
			isValid &= validateDropDownBox("Virtual Machine", $thisDialog.find("#volume_vm"), $thisDialog.find("#volume_vm_errormsg"));	
			if (!isValid) 
			    return;
			    
			$thisDialog.dialog("close");	     
	        
	        var virtualMachineId = $thisDialog.find("#volume_vm").val();		
	        	    	
	    	var id = jsonObj.id;			
			var apiCommand = "command=attachVolume&id="+id+'&virtualMachineId='+virtualMachineId;
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);		
	    }, 
	    "Cancel": function() { 					        
		    $(this).dialog("close"); 
	    } 
    }).dialog("open");
}	

//Snapshot tab actions
var volumeSnapshotActionMap = {  
    "Create Volume": {              
        isAsyncJob: true,
        asyncJobResponse: "createvolumeresponse",
        dialogBeforeActionFn : doCreateVolumeFromSnapshotInVolumePage,
        inProcessText: "Creating Volume....",
        afterActionSeccessFn: function(json, id, $subgridItem) {   
            var $midmenuItem1 = $("#midmenu_item").clone();		        
            var item = json.queryasyncjobresultresponse.jobresult.volume;		   
			volumeToMidmenu(item, $midmenuItem1);
			bindClickToMidMenu($midmenuItem1, volumeToRightPanel, getMidmenuId);  						                    
			$midmenuItem1.find("#info_icon").removeClass("error").show();
	        $midmenuItem1.data("afterActionInfo", ("Creating volume from snapshot succeeded.")); 	
            $("#midmenu_container").append($midmenuItem1.fadeIn("slow"));	           
        }
    }   
    , 
    "Delete Snapshot": {              
        api: "deleteSnapshot",     
        isAsyncJob: true,
        asyncJobResponse: "deletesnapshotresponse",
		dialogBeforeActionFn : doSnapshotDelete,
        inProcessText: "Deleting snapshot....",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    } 
    ,
    "Create Template": {              
        isAsyncJob: true,
        asyncJobResponse: "createtemplateresponse",
        dialogBeforeActionFn : doCreateTemplateFromSnapshotInVolumePage,
        inProcessText: "Creating Template....",
        afterActionSeccessFn: function(json, id, $subgridItem) {}            
    }
}  

function doSnapshotDelete($actionLink, $subgridItem) {
	$("#dialog_confirmation_delete_snapshot")	
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 	
			var id = $subgridItem.data("jsonObj").id;
			var apiCommand = "command=deleteSnapshot&id="+id;                      
            doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem); 
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}
                                              
function doCreateVolumeFromSnapshotInVolumePage($actionLink, $subgridItem) { 
    var jsonObj = $subgridItem.data("jsonObj");
       
    $("#dialog_add_volume_from_snapshot")
    .dialog("option", "buttons", {	                    
     "Add": function() {	
         var thisDialog = $(this);	 
                                        
         var isValid = true;					
         isValid &= validateString("Name", thisDialog.find("#name"), thisDialog.find("#name_errormsg"));					          		
         if (!isValid) return;   
         
         thisDialog.dialog("close");       	                                             
         
         var name = thisDialog.find("#name").val();	                
         
         var id = jsonObj.id;
         var apiCommand = "command=createVolume&snapshotid="+id+"&name="+fromdb(name);    	
    	 doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);			
     },
     "Cancel": function() {	                         
         $(this).dialog("close");
     }
    }).dialog("open");     
}

function doCreateTemplateFromSnapshotInVolumePage($actionLink, $subgridItem) { 
    var jsonObj = $subgridItem.data("jsonObj");
       
    $("#dialog_create_template_from_snapshot")
    .dialog("option", "buttons", {
     "Add": function() {	
         var thisDialog = $(this);	 	                                                                        
         var isValid = true;					
         isValid &= validateString("Name", thisDialog.find("#name"), thisDialog.find("#name_errormsg"), false);		
         isValid &= validateString("Display Text", thisDialog.find("#display_text"), thisDialog.find("#display_text_errormsg"), false);				         		          		
         if (!isValid) return;                  	                                             
         
         thisDialog.dialog("close");	
         
         var name = thisDialog.find("#name").val();	 
         var displayText = thisDialog.find("#display_text").val();	 
         var osTypeId = thisDialog.find("#os_type").val(); 	  
         var password = thisDialog.find("#password").val();	                                         
       
         var id = jsonObj.id;
         var apiCommand = "command=createTemplate&snapshotid="+id+"&name="+todb(name)+"&displaytext="+todb(displayText)+"&ostypeid="+osTypeId+"&passwordEnabled="+password;    	 
    	 doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);				
     },
     "Cancel": function() {	                         
         $(this).dialog("close");
     }	                     
    }).dialog("open");	     
}
