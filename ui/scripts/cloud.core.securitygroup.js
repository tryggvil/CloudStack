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

function securityGroupGetSearchParams() {
    var moreCriteria = [];	

	var $advancedSearchPopup = $("#advanced_search_popup");
	if (lastSearchType == "advanced_search" && $advancedSearchPopup.length > 0) {
	    var name = $advancedSearchPopup.find("#adv_search_name").val();							
		if (name!=null && trim(name).length > 0) 
			moreCriteria.push("&name="+todb(name));	
        
        if ($advancedSearchPopup.find("#adv_search_domain_li").css("display") != "none") {		
		    var domainId = $advancedSearchPopup.find("#adv_search_domain").val();		
		    if (domainId!=null && domainId.length > 0) 
			    moreCriteria.push("&domainid="+domainId);	
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

function afterLoadSecurityGroupJSP() {    
    
}

function doEditSecurityGroup($actionLink, $detailsTab, $midmenuItem1) {       
    var $readonlyFields  = $detailsTab.find("#name, #displaytext, #tags, #domain");
    var $editFields = $detailsTab.find("#name_edit, #displaytext_edit, #domain_edit"); 
             
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        $editFields.hide();
        $readonlyFields.show();   
        $("#save_button, #cancel_button").hide();       
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditsecurityGroup2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditSecurityGroup2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {     
    var jsonObj = $midmenuItem1.data("jsonObj");
    var id = jsonObj.id;
    
    // validate values   
    var isValid = true;					
    isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"), true);		
    isValid &= validateString("Display Text", $detailsTab.find("#displaytext_edit"), $detailsTab.find("#displaytext_edit_errormsg"), true);				
    if (!isValid) 
        return;	
     
    var array1 = [];    
    var name = $detailsTab.find("#name_edit").val();
    array1.push("&name="+todb(name));
    
    var displaytext = $detailsTab.find("#displaytext_edit").val();
    array1.push("&displayText="+todb(displaytext));
	
	var tags = $detailsTab.find("#tags_edit").val();
	array1.push("&tags="+todb(tags));	
	
	var domainid = $detailsTab.find("#domain_edit").val();
	array1.push("&domainid="+todb(domainid));	
	
	$.ajax({
	    data: createURL("command=updatesecurityGroup&id="+id+array1.join("")),
		dataType: "json",
		success: function(json) {			    
		    var jsonObj = json.updatesecurityGroupresponse.securityGroup;   
		    securityGroupToMidmenu(jsonObj, $midmenuItem1);
		    securityGroupToRightPanel($midmenuItem1);	
		    
		    $editFields.hide();      
            $readonlyFields.show();       
            $("#save_button, #cancel_button").hide();     	  
		}
	});
}

function securityGroupToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    /*    
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_system_securityGroup.png");	
    */
    
    $midmenuItem1.find("#first_row").text(fromdb(jsonObj.name).substring(0,25)); 
    $midmenuItem1.find("#second_row").text(fromdb(jsonObj.account).substring(0,25));  
}

function securityGroupToRightPanel($midmenuItem1) {
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    securityGroupJsonToDetailsTab();   
}

function securityGroupJsonToDetailsTab() {     
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null)
        return;
     
    var $thisTab = $("#right_panel_content #tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
    
    /*
    var id = jsonObj.id;    
    var jsonObj;   
    $.ajax({
        data: createURL("command=listNetworkGroups&id="+id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listnetworkgroupsresponse.securitygroup;            
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);  
            }
        }
    });   
    */    
 
    $thisTab.find("#id").text(fromdb(jsonObj.id));
    
    /*
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));
    $thisTab.find("#name").text(fromdb(jsonObj.name));
    $thisTab.find("#name_edit").val(fromdb(jsonObj.name));
    
    $thisTab.find("#displaytext").text(fromdb(jsonObj.displaytext));
    $thisTab.find("#displaytext_edit").val(fromdb(jsonObj.displaytext));
    
    var diskSize = securityGroupGetDiskSize(jsonObj);   
    $thisTab.find("#disksize").text(diskSize);    
        
    $thisTab.find("#tags").text(fromdb(jsonObj.tags));    
    $thisTab.find("#tags_edit").val(fromdb(jsonObj.tags));    
      
    $thisTab.find("#domain").text(fromdb(jsonObj.domain));   
    $thisTab.find("#domain_edit").val(fromdb(jsonObj.domainid));   
    */
    
    //actions ***
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();    
    buildActionLinkForTab("Edit Security Group", securityGroupActionMap, $actionMenu, $midmenuItem1, $thisTab);	  
    buildActionLinkForTab("Delete Security Group", securityGroupActionMap, $actionMenu, $midmenuItem1, $thisTab);	  
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();         
}

function securityGroupClearRightPanel() {
    securityGroupClearDetailsTab();
}

function securityGroupClearDetailsTab() {
    var $thisTab = $("#right_panel_content").find("#tab_content_details");     
    $thisTab.find("#id").text("");    
    $thisTab.find("#grid_header_title").text("");
    $thisTab.find("#name").text("");
    $thisTab.find("#name_edit").val("");    
    $thisTab.find("#displaytext").text("");
    $thisTab.find("#displaytext_edit").val("");    
    $thisTab.find("#disksize").text("");
    $thisTab.find("#tags").text("");   
    $thisTab.find("#domain").text("");   
    
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty(); 
    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
}

var securityGroupActionMap = {   
    "Edit Security Group": {
        dialogBeforeActionFn: doEditSecurityGroup
    },   
    "Delete Security Group": {              
        api: "deleteNetworkGroup",     
        isAsyncJob: false,           
        inProcessText: "Deleting Security Group....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {   
            $midmenuItem1.slideUp("slow", function() {
                $(this).remove();
            });    
            clearRightPanel();
            securityGroupClearRightPanel();
        }
    }    
}  