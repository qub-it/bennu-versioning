package com.qubit.solution.fenixedu.bennu.versioning.domain;

import java.sql.Connection;
import java.sql.SQLException;

import org.fenixedu.bennu.core.domain.Bennu;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
        closePool();
    }

    private HikariDataSource pool = null;

    private synchronized void closePool() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    public synchronized Connection getConnectionFromPool() throws SQLException {
        if (pool == null && !StringUtils.isEmpty(getJdbcURL())) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getJdbcURL());
            config.setUsername(getUsername());
            config.setPassword(getPassword());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(25);
            pool = new HikariDataSource(config);
        }
        return pool != null ? pool.getConnection() : null;
    }

    public static Connection createConnection() throws SQLException {
        VersioningTargetConfiguration configuration = VersioningTargetConfiguration.getInstance();
        return configuration.getConnectionFromPool();
    }

}
