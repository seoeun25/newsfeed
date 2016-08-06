package com.nexr.newsfeed.jpa;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openjpa.lib.jdbc.DecoratingDataSource;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

public class JPAService {

    public static final String CONF_URL = "jdbc.url";
    public static final String CONF_DRIVER = "jdbc.driver";
    public static final String CONF_USERNAME = "jdbc.username";
    public static final String CONF_PASSWORD = "jdbc.password";
    public static final String CONF_DB_SCHEMA = "schema.name";
    public static final String CONF_CONN_DATA_SOURCE = "connection.data.source";
    public static final String CONF_CONN_PROPERTIES = "connection.properties";
    public static final String CONF_MAX_ACTIVE_CONN = "pool.max.active.conn";
    public static final String CONF_CREATE_DB_SCHEMA = "create.db.schema";
    public static final String CONF_VALIDATE_DB_CONN = "validate.db.connection";
    public static final String CONF_VALIDATE_DB_CONN_EVICTION_INTERVAL = "validate.db.connection.eviction.interval";
    public static final String CONF_VALIDATE_DB_CONN_EVICTION_NUM = "validate.db.connection.eviction.num";
    public static final String CONF_VALIDATE_DB_CONN_QUERY = "validate.db.connection.query";

    private static Logger log = LoggerFactory.getLogger(JPAService.class);
    private final String name;
    private final Context context;
    private final String persistenceUnit;
    private EntityManagerFactory factory;

    @Inject
    public JPAService(Context context, @Named("persistenceName") String name, @Named("persistenceUnit") String persistenceUnit) {
        this.context = context;
        this.name = name;
        this.persistenceUnit = persistenceUnit;
        log.info("persistenceUnit : {}", persistenceUnit);
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BasicDataSource getBasicDataSource() {
        BasicDataSource basicDataSource = null;
        OpenJPAEntityManagerFactorySPI spi = (OpenJPAEntityManagerFactorySPI) factory;
        Object connectionFactory = spi.getConfiguration().getConnectionFactory();
        if (connectionFactory instanceof DecoratingDataSource) {
            DecoratingDataSource decoratingDataSource = (DecoratingDataSource) connectionFactory;
            basicDataSource = (BasicDataSource) decoratingDataSource.getInnermostDelegate();
        } else if (connectionFactory instanceof BasicDataSource) {
            basicDataSource = (BasicDataSource) connectionFactory;
        }
        return basicDataSource;
    }

    private void start() throws NewsfeedException {
        String dbSchema = context.getConfig(name + "." + CONF_DB_SCHEMA);
        String url = context.getConfig(name + "." + CONF_URL);
        String driver = context.getConfig(name + "." + CONF_DRIVER);
        String user = context.getConfig(name + "." + CONF_USERNAME);
        String password = context.getConfig(name + "." + CONF_PASSWORD).trim();
        String maxConn = context.getConfig(name + "." + CONF_MAX_ACTIVE_CONN).trim();
        String dataSource = context.getConfig(name + "." + CONF_CONN_DATA_SOURCE);
        String connPropsConfig = context.getConfig(name + "." + CONF_CONN_PROPERTIES);
        boolean autoSchemaCreation = Boolean.parseBoolean(context.getConfig(name + "." + CONF_CREATE_DB_SCHEMA));
        boolean validateDbConn = Boolean.parseBoolean(context.getConfig(name + "." + CONF_VALIDATE_DB_CONN));
        String evictionInterval = context.getConfig(name + "." + CONF_VALIDATE_DB_CONN_EVICTION_INTERVAL).trim();
        String evictionNum = context.getConfig(name + "." + CONF_VALIDATE_DB_CONN_EVICTION_NUM).trim();
        String validationQuery = context.getConfig(name + "." + CONF_VALIDATE_DB_CONN_QUERY);

        if (!url.startsWith("jdbc:")) {
            throw new NewsfeedException("invalid JDBC URL, must start with 'jdbc:'");
        }
        String dbType = url.substring("jdbc:".length());
        if (dbType.indexOf(":") <= 0) {
            throw new NewsfeedException("invalid JDBC URL, missing vendor 'jdbc:[VENDOR]:...'");
        }

        String connProps = "DriverClassName={0},Url={1},Username={2},Password={3},MaxActive={4}";
        connProps = MessageFormat.format(connProps, driver, url, user, password, maxConn);
        Properties props = new Properties();
        if (autoSchemaCreation || validateDbConn) {
            connProps += ",TestOnBorrow=true,TestOnReturn=true,TestWhileIdle=true";
            if (validateDbConn) {
                String interval = "timeBetweenEvictionRunsMillis=" + evictionInterval;
                String num = "numTestsPerEvictionRun=" + evictionNum;
                connProps += "," + interval + "," + num;
                connProps += ",ValidationQuery=" + validationQuery;
                connProps = MessageFormat.format(connProps, dbSchema);
            }
            if (autoSchemaCreation) {
                props.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            }
        } else {
            connProps += ",TestOnBorrow=false,TestOnReturn=false,TestWhileIdle=false";
        }
        if (connPropsConfig != null) {
            connProps += "," + connPropsConfig;
        }
        props.setProperty("openjpa.ConnectionProperties", connProps);

        props.setProperty("openjpa.ConnectionDriverName", dataSource);

        factory = Persistence.createEntityManagerFactory(persistenceUnit, props);

        EntityManager entityManager = getEntityManager();
        // TODO No need to find. Persistence.xml

        log.info("All entities initialized");
        entityManager.getTransaction().begin();
        OpenJPAEntityManagerFactorySPI spi = (OpenJPAEntityManagerFactorySPI) factory;
        String logMsg = spi.getConfiguration().getConnectionProperties().replaceAll("Password=.*?,", "Password=***,");
        log.info("JDBC configuration: {}", logMsg);
        entityManager.getTransaction().commit();
        entityManager.close();

    }

    @VisibleForTesting
    public void instrument() {
        final BasicDataSource dataSource = getBasicDataSource();
        log.info("Active Num {}", dataSource.getNumActive());
        log.info("Idle Num {}", dataSource.getNumIdle());
    }

    public EntityManager getEntityManager() {
        return factory.createEntityManager();
    }

    public Object executeGet(String namedQueryName, Query query, EntityManager em) {
        try {

            Object obj = null;
            try {
                obj = query.getSingleResult();
            } catch (NoResultException e) {
                // return null when no matched result
            }
            return obj;
        } finally {
            processFinally(em, namedQueryName, false);
        }
    }

    public List<?> executeGetList(String namedQueryName, Query query, EntityManager em) {
        try {

            List<?> resultList = null;
            try {
                resultList = query.getResultList();
            } catch (NoResultException e) {
                // return null when no matched result
            }
            return resultList;
        } finally {
            processFinally(em, namedQueryName, false);
        }
    }

    public int executeUpdate(String namedQueryName, Query query, EntityManager em) throws NewsfeedException {
        try {

            log.trace("Executing Update/Delete Query [{0}]", namedQueryName);
            em.getTransaction().begin();
            int ret = query.executeUpdate();
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            return ret;
        } catch (PersistenceException e) {
            throw new NewsfeedException("Failed to update", e);
        } finally {
            processFinally(em, namedQueryName, true);
        }
    }

    private void processFinally(EntityManager em, String name, boolean checkActive) {
        if (checkActive) {
            try {
                if (em.getTransaction().isActive()) {
                    log.warn("[{}] ended with an active transaction, rolling back", name);
                    em.getTransaction().rollback();
                }
            } catch (Exception ex) {
                log.warn("Could not check/rollback transaction after [{}], {}", name + ex.getMessage(), ex);
            }
        }
        try {
            if (em.isOpen()) {
                em.close();
            } else {
                log.warn("[{0}] closed the EntityManager, it should not!", name);
            }
        } catch (Exception ex) {
            log.warn("Could not close EntityManager after [{}], {}", name + ex.getMessage(), ex);
        }
    }

    public void shutdown() {
        close();
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    private void close() {
        if (!getBasicDataSource().isClosed()) {
            try {
                getBasicDataSource().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
