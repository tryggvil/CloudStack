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

function alertGetSearchParams() {
    var moreCriteria = [];	

	var $advancedSearchPopup = $("#advanced_search_popup");
	if (lastSearchType == "advanced_search" && $advancedSearchPopup.length > 0) {
	    var type = $advancedSearchPopup.find("#adv_search_type").val();							
		if (type!=null && type.length > 0) 
			moreCriteria.push("&type="+todb(type));			
	} 
	else {     			    		
	    var searchInput = $("#basic_search").find("#search_input").val();	 
        if (lastSearchType == "basic_search" && searchInput != null && searchInput.length > 0) {	           
            moreCriteria.push("&keyword="+todb(searchInput));	       
        }        
	}
	
	return moreCriteria.join("");          
}

function afterLoadAlertJSP() {

}

function alertToMidmenu(jsonObj, $midmenuItem1) {      
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
        
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_alerts.png");	
    
    setDateField(jsonObj.sent, $midmenuItem1.find("#second_row"));
    $midmenuItem1.find("#first_row").text(toAlertType(jsonObj.type)); 
}

function alertToRightPanel($midmenuItem1) {   
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    alertJsonToDetailsTab();   
}

function alertJsonToDetailsTab() {   
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null)
        return;
          
    var $thisTab = $("#right_panel_content").find("#tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();        
      
    $thisTab.find("#id").text(fromdb(jsonObj.id));      
    $thisTab.find("#type").text(fromdb(jsonObj.type));
    $thisTab.find("#description").text(fromdb(jsonObj.description));    
    setDateField(jsonObj.sent, $thisTab.find("#sent"));	
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();  
}