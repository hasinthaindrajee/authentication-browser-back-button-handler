package com.custom.login.endpoint.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

import static com.custom.login.endpoint.util.CustomFileUtil.buildEndpointUtilProperties;

/**
 * @scr.component name="CustomLoginEndpointUtilComponent" immediate="true"
 */
public class CustomLoginEndpointUtilComponent {

    private static final Log log = LogFactory.getLog(CustomLoginEndpointUtilComponent.class);

    protected void activate(ComponentContext context) {

        try {
            buildEndpointUtilProperties();
        } catch (Throwable e) {
            log.error("Failed to initialize CustomLoginEndpointUtilComponent.", e);
        }
        log.info("CustomLoginEndpointUtilComponent is activated");
    }

    protected void deactivate(ComponentContext context) {

        log.info("CustomLoginEndpointUtilComponent is deactivated");
    }
}
