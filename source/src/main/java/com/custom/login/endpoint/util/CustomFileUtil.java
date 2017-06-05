package com.custom.login.endpoint.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * CustomFileUtil.
 */
public class CustomFileUtil {

    private static final Log log = LogFactory.getLog(com.custom.login.endpoint.util.CustomFileUtil.class);

    private static Map<String, String> relayingPartyRedirectUrlMap = new HashMap<>();

    private CustomFileUtil() {

    }

    public static String getRedirectURLForRelayingParty(String relayingParty) {

        return relayingPartyRedirectUrlMap.get(relayingParty);
    }

    public static void buildEndpointUtilProperties() {

        Path path = Paths.get(getCarbonHomeDirectory().toString(), "repository",
                "conf", CustomEndpointConstants.ENDPOINT_CONFIG_FILE_NAME);

        if (Files.exists(path)) {
            Properties properties = new Properties();
            try (Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
                properties.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Error while loading '" + CustomEndpointConstants.ENDPOINT_CONFIG_FILE_NAME
                        + "' configuration file", e);
            }

            for (String relayingParty : properties.stringPropertyNames()) {
                String redirectUrl = properties.getProperty(relayingParty);
                if (StringUtils.isBlank(relayingParty) || StringUtils.isBlank(redirectUrl)) {
                    log.warn("Invalid configuration found. relayingParty - " + relayingParty + " redirectUrl - " +
                            redirectUrl);
                    continue;
                }
                relayingPartyRedirectUrlMap.put(relayingParty.trim(), redirectUrl.trim());
            }
        }
    }

    private static Path getCarbonHomeDirectory() {

        return Paths.get(System.getProperty(CustomEndpointConstants.CARBON_HOME));
    }
}
