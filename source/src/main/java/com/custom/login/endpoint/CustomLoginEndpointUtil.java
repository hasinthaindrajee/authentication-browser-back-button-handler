package com.custom.login.endpoint;

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * CustomLoginEndpointUtil.
 */
public class CustomLoginEndpointUtil {

    private static final String SP_REDIRECT_URL_RESOURCE_PATH = "/identity/config/relyingPartyRedirectUrls";
    private static final Log log = LogFactory.getLog(CustomLoginEndpointUtil.class);

    public static String getSessionDataKeyStatus(String relyingParty, String sessionDataKey, String tenantDomain) {

        if(log.isDebugEnabled()){

            log.debug(String.format("Checking the the session status. " +
                    "sessionDataKey : '%s', relyingParty : '%s', tenantDomain : '%s'",
                    sessionDataKey, relyingParty, tenantDomain));

        }

        JsonObject result = new JsonObject();
        if (StringUtils.isBlank(relyingParty) || StringUtils.isBlank(sessionDataKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Required data ( relyingParty or sessionDataKey) to proceed is not available in the request.");
            }

            // Can't handle
            result.addProperty("status", "success");
            return result.toString();
        }

        // Valid Request
        AuthenticationContext authenticationContextFromCache = FrameworkUtils.getAuthenticationContextFromCache(sessionDataKey);
        if (authenticationContextFromCache != null) {

            if(log.isDebugEnabled()){
                log.debug(String.format("A cached authentication context is available for the session data key : " +
                        "'%s'\nSession Identifier : '%s'\nCaller Session Key : '%s'",
                        sessionDataKey,
                        authenticationContextFromCache.getSessionIdentifier(),
                        authenticationContextFromCache.getCallerSessionKey()));
            }

            result.addProperty("status", "success");
            return result.toString();
        }

        String redirectUrl = getRelyingPartyRedirectUrl(relyingParty, tenantDomain);
        if (StringUtils.isBlank(redirectUrl)) {
            if (log.isDebugEnabled()) {
                log.debug("Redirect URL is not available for the relaying party - " + relyingParty);
            }
            // Can't handle
            result.addProperty("status", "success");
            return result.toString();
        }

        result.addProperty("status", "redirect");
        result.addProperty("redirectUrl", redirectUrl);
        return result.toString();
    }

    /**
     * Returns the redirect url configured in the registry against relying party.
     *
     * @param relyingParty Name of the relying party
     * @param tenantDomain Tenant Domain.
     * @return Redirect URL.
     */
    public static String getRelyingPartyRedirectUrl(String relyingParty, String tenantDomain) {
        if (log.isDebugEnabled()) {
            log.debug("retrieving configured url against relying party : " + relyingParty + "for tenant domain : " +
                    tenantDomain);
        }

        int tenantId;
        if (StringUtils.isEmpty(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain is not available. Hence using super tenant domain");
            }
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        } else {
            tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        }
        try {
            IdentityTenantUtil.initializeRegistry(-1234, tenantDomain);
            Registry registry = IdentityTenantUtil.getConfigRegistry(tenantId);
            if (registry.resourceExists(SP_REDIRECT_URL_RESOURCE_PATH)) {
                Resource resource = registry.get(SP_REDIRECT_URL_RESOURCE_PATH);
                if (resource != null) {
                    String redirectUrl = resource.getProperty(relyingParty);
                    if (StringUtils.isNotEmpty(redirectUrl)) {
                        return redirectUrl;
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while getting data from the registry.", e);
        } catch (IdentityException e) {
            log.error("Error while getting the tenant domain from tenant id : " + tenantId, e);
        }
        return null;
    }
}
