<%@ page import="java.util.*" %>

<%@ page import="com.cloud.utils.*" %>

<%
    Locale browserLocale = request.getLocale();
    CloudResourceBundle t = CloudResourceBundle.getBundle("resources/resource", browserLocale);
%>



<!-- router detail panel (begin) -->
<div class="main_title" id="right_panel_header">
     
    <div class="main_titleicon">
        <img src="images/title_routersicon.gif" alt="Routers" /></div>
   
    <h1>
        Router
    </h1>
</div>
<div class="contentbox" id="right_panel_content">
    <div class="info_detailbox errorbox" id="after_action_info_container_on_top" style="display: none">
        <p id="after_action_info">
        </p>
    </div>
    <div class="tabbox" style="margin-top: 15px;">
        <div class="content_tabs on">
            <%=t.t("Details")%></div>
    </div> 
    <div id="tab_content_details">
    	<div class="rightpanel_mainloader_panel" style="display:block;">
              <div class="rightpanel_mainloaderbox">
                   <div class="rightpanel_mainloader_animatedicon"></div>
                   <p>Loading &hellip;</p>    
              </div>               
        </div>
        
        <div class="grid_container">  
        	<div class="grid_header">
            	<div id="grid_header_title" class="grid_header_title">(title)</div>
                <div id="action_link" class="grid_actionbox" id="account_action_link">
                    <div class="grid_actionsdropdown_box" id="action_menu" style="display: none;">
                        <ul class="actionsdropdown_boxlist" id="action_list">
                        	<li><%=t.t("no.available.actions")%></li>
                        </ul>
                    </div>
                </div>
                <div class="gridheader_loaderbox" id="spinning_wheel" style="border: 1px solid #999;
                display: none;">
                    <div class="gridheader_loader" id="icon">
                    </div>
                    <p id="description">
                        Detaching Disk &hellip;</p>
                </div>
            </div>          
            <div class="grid_rows odd">
                <div class="vm_statusbox">   
                    <div id="view_console_container">                       
                    	<div id="view_console_template" style="display:block">
    						<div class="vm_consolebox" id="box0">
    						</div>
   							<div class="vm_consolebox" id="box1" style="display: none">
    						</div>
						</div>   
                    </div>
                    <div class="vm_status_textbox">
                        <div class="vm_status_textline green" id="state">
                        </div>
                        <br />
                        <p id="ipAddress">
                        </p>
                    </div>
                </div>
            </div>   
            <div class="grid_rows even">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Zone")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="zonename">
                    </div>
                </div>
            </div>
            <div class="grid_rows odd">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Name")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="name">
                    </div>
                </div>
            </div>
            <div class="grid_rows even">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Public IP")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="publicip">
                    </div>
                </div>
            </div>
            <div class="grid_rows odd">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Private IP")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="privateip">
                    </div>
                </div>
            </div>
            <div class="grid_rows even">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Guest IP")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="guestipaddress">
                    </div>
                </div>
            </div>
            <div class="grid_rows odd">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Host")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="hostname">
                    </div>
                </div>
            </div>
            <div class="grid_rows even">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Network Domain")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="networkdomain">
                    </div>
                </div>
            </div>
            <div class="grid_rows odd">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Account")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="account">
                    </div>
                </div>
            </div>
            <div class="grid_rows even">
                <div class="grid_row_cell" style="width: 20%;">
                    <div class="row_celltitles">
                        <%=t.t("Created")%>:</div>
                </div>
                <div class="grid_row_cell" style="width: 79%;">
                    <div class="row_celltitles" id="created">
                    </div>
                </div>
            </div>
        </div>
    </div>   
</div>
<!-- router detail panel (end) -->

<!-- view console template (begin)  -->
<div id="view_console_template" style="display:none">
    <div class="vm_consolebox" id="box0">
    </div>
    <div class="vm_consolebox" id="box1" style="display: none">
    </div>
</div>
<!-- view console template (end)  -->
