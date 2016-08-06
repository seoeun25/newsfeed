package com.nexr.newsfeed.jpa;

import com.google.inject.Inject;
import com.nexr.newsfeed.NewsfeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;

/**
 * Base class of the QueryExecutor.
 *
 * @param <T>
 * @param <E>
 */
public abstract class QueryExecutor<T, E extends Enum<E>> {

    private static Logger LOG = LoggerFactory.getLogger(QueryExecutor.class);

    protected JPAService JPAService;

    protected QueryExecutor() {

    }

    @Inject
    public void setJPAService(JPAService JPAService) {
        this.JPAService = JPAService;
    }

    public void insert(T bean) throws NewsfeedException {
        if (bean != null) {
            EntityManager em = JPAService.getEntityManager();
            try {
                em.getTransaction().begin();
                em.persist(bean);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                throw new NewsfeedException(e);
            } finally {
                if (em.getTransaction().isActive()) {
                    LOG.warn("insert ended with an active transaction, rolling back");
                    em.getTransaction().rollback();
                }
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    public abstract T get(E namedQuery, Object... parameters) throws NewsfeedException;

    public abstract List<T> getList(E namedQuery, Object... parameters) throws NewsfeedException;

    public abstract Query getUpdateQuery(E namedQuery, T bean, EntityManager em) throws NewsfeedException;

    public abstract Query getSelectQuery(E namedQuery, EntityManager em, Object... parameters)
            throws NewsfeedException;

    public abstract int executeUpdate(E namedQuery, T jobBean) throws NewsfeedException;
}

