Please follow the following instructions in order to apply the changes. We are rewriting the above given instruction since there are differences in the above given instructions and this set of instructions. So please be kind enough to follow the below instructions. 


First, you need to apply patch WSO2-CARBON-PATCH-4.4.0-0393 attached to this ticket in your environment. Please download WSO2-CARBON-PATCH-4.4.0-0393.zip and follow the directions in readme in order to apply the patch.

Download the CustomLoginPageHandlerArtifacts_RegistryBased.zip. and extract it to find the artifacts. You can follow the bellow steps to apply this solution.

Copy CustomLoginEndpointUtils-1.0.0.jar which is inside artifacts folder, to the <IS_HOME>/repository/components/dropins/ folder

Copy check_session_id.jsp file in the artifact directory to the <IS_HOME>/repository/deployment/server/webapps/authenticationendpoint/ folder. (note that the new file is slightly different from the one which is shared with you earlier)

Open <IS_HOME>/repository/deployment/server/webapps/authenticationendpoint/WEB-INF/web.xml file and add following entries,,
    <servlet>
        <servlet-name>check_session_id.do</servlet-name>
        <jsp-file>/check_session_id.jsp</jsp-file>
    </servlet>
    ...
    <servlet-mapping>
        <servlet-name>check_session_id.do</servlet-name>
        <url-pattern>/check_session_id.do</url-pattern>
    </servlet-mapping>

Open login page at <IS_HOME>/repository/deployment/server/webapps/authenticationendpoint/login.jsp and update the <head> element as follows, (This is slightly different from the code we shared initially)

    <head>
    ...
         <script>
            function checkSessionKey() {
                $.ajax({
                    type: "GET",
                    url: 'check_session_id.do?sessionDataKey=' + getParameterByName('sessionDataKey') + '&relyingParty=' + getParameterByName('relyingParty') + '&tenantDomain=' + getParameterByName('tenantDomain'),
                    success: function (data) {
                        var response = JSON.parse(data);
                        if (response && response.status == "redirect" && response.redirectUrl && response.redirectUrl.length > 0) {
                            window.location.href = response.redirectUrl;
                        }
                    }
                });
            }

            function getParameterByName(name, url) {
                if (!url) {
                    url = window.location.href;
                }
                name = name.replace(/[\[\]]/g, "\\$&");
                var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                        results = regex.exec(url);
                if (!results) return null;
                if (!results[2]) return '';
                return decodeURIComponent(results[2].replace(/\+/g, " "));
            }
        </script>
    </head>


In the same login.jsp file add below change to <body> element.
    <body onload="checkSessionKey()">
Now add the following line to the as the last line in the <script> element in same login.jsp.
       ...
        window.onunload = function(){};
    </script>

    </body>
    </html>


For your reference, I have attached login.jsp with above changes inside the CustomLoginPageHandlerArtifacts_RegistryBased.zip.

Restart the server.


Now you need to add a registry resource which will contain redirect urls of different Service Providers. In order to do it follow the following steps.


Login to the admin console 
Click on Main Menu -> Registry -> Browse ->
Now you will see the registry browswer

Browse the registry and go to /_system/config/identity/config (Finally click on "config" and once you are in this location the Location will be shown as  "/_system/config/identity/config" in registry browswer Location)

Once you navigate there, follow the following steps to add a registry resource.

Click on AddResource 

Fill the form you get with following information. 

	Method : Create text content
	Name : relyingPartyRedirectUrls
Click in "Add" button.


The created registry resource can be seen once you click on Add button. Click on it (relyingPartyRedirectUrls). Once you click and go inside that resource, you will see a section as "Properties" Click on the "+" sign you see at the right hand conrner of property section. This will allow you to add a property to the resource. Keey should be the relying party name and value should be the redirectURL you want the application to be redirected when the back button is pressed.


Click on add new property. There you will be prompted to add property key and property value. 

Relying party name with redirect URL needs to configure in this.
<Oauth2_client_id>=<login_redirect_url>
<Issuer Name>=<login_redirect_url>

  NOTE :
         Relying party for SAML = Issuer Name
         Relying party for Oauth2 = OAuth Client Key
	

Following are two example values for key and value 

Key                               Value 

wso2.my.dashboard		  https://localhost:9443/dashboard/index.jag

Once you fill key and value click on "Add" button. 

Now you can try out your scenario.

Note that these configurations are per-tenant. You need to do this configuration after loging into SPs tenant Admin console. 


