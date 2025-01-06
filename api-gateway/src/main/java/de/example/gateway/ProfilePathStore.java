package de.example.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reads and stores the mapping from path prefix to profile.
 */
@Component
@ConfigurationProperties(prefix = "custom-routing.profile-mappings")
public class ProfilePathStore {

    /**
     * The default profile to use if no specific mapping is found.
     */
    private String defaultProfile;

    /**
     * The mapping from path prefix to profile.
     */
    private Map<String, String> nonDefaultProfilesByPrefix;

    @Nullable
    public String getProfileByPath(String path) {
        if (path == null || !path.startsWith("/api/")) {
            return null;
        }

        var moduleServicePrefix = path.split("/")[2];
        return nonDefaultProfilesByPrefix.getOrDefault(moduleServicePrefix, defaultProfile);
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public void setNonDefaultProfilesByPrefix(Map<String, String> nonDefaultProfilesByPrefix) {
        this.nonDefaultProfilesByPrefix = nonDefaultProfilesByPrefix;
    }
}
