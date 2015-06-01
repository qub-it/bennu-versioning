package com.qubit.solution.fenixedu.bennu.versioning.domain;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;

public class VersioningTargetConfiguration extends VersioningTargetConfiguration_Base {

    protected VersioningTargetConfiguration() {
        super();
        VersioningTargetConfiguration versioningTargetConfiguration = Bennu.getInstance().getVersioningTargetConfiguration();
        if (versioningTargetConfiguration != null && versioningTargetConfiguration != this) {
            throw new IllegalStateException("There can only be one VersioningTargetConfiguration");
        }
        setRootDomainObject(Bennu.getInstance());
    }

    public static VersioningTargetConfiguration getInstance() {
        VersioningTargetConfiguration versioningTargetConfiguration = Bennu.getInstance().getVersioningTargetConfiguration();
        if (versioningTargetConfiguration == null) {
            versioningTargetConfiguration = createVersioningTargetConfiguration();
        }
        return versioningTargetConfiguration;
    }

    @Atomic
    private static VersioningTargetConfiguration createVersioningTargetConfiguration() {
        return new VersioningTargetConfiguration();
    }

    public void edit(String jdbcURL, String username, String password) {
        setJdbcURL(jdbcURL);
        setUsername(username);
        setPassword(password);
    }
}