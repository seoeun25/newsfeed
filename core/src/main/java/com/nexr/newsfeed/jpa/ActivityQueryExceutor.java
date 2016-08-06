package com.nexr.newsfeed.jpa;

import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Activity;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityQueryExceutor extends QueryExecutor<Activity, ActivityQueryExceutor.ActivityQuery> {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ActivityQueryExceutor.class);

    public Activity get(ActivityQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        Object ret = JPAService.executeGet(namedQuery.name(), query, em);
        if (ret == null) {
            log.debug("query [{}]", query.toString());
            throw new NewsfeedException("Activity Not Found");
        }
        Activity bean = constructBean(namedQuery, ret, parameters);
        return bean;
    }

    public Query getSelectQuery(ActivityQuery namedQuery, EntityManager em, Object... parameters)
            throws NewsfeedException {
        Query query = em.createNamedQuery(namedQuery.name());
        switch (namedQuery) {
            case GET_BYFOLLOWING_FORWARD_ASC:
            case GET_BYFOLLOWING_FORWARD_DESC:
            case GET_BYFOLLOWING_BACKWARD_ASC:
            case GET_BYFOLLOWING_BACKWARD_DESC:
                query.setParameter("createdTime", parameters[0]);
                query.setParameter("followings", parameters[1]);
                if (parameters.length >= 3) {
                    query.setMaxResults((Integer) parameters[2]);
                }
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot set parameters for " + namedQuery.name());
        }
        return query;
    }

    @Override
    public int executeUpdate(ActivityQuery namedQuery, Activity jobBean) throws NewsfeedException {
        return 0;
    }

    public List<Activity> getList(ActivityQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        List<?> retList = (List<?>) JPAService.executeGetList(namedQuery.name(), query, em);
        List<Activity> list = new ArrayList<>();
        if (retList != null) {
            for (Object ret : retList) {
                list.add(constructBean(namedQuery, ret));
            }
        }
        return list;
    }

    @Override
    public Query getUpdateQuery(ActivityQuery namedQuery, Activity bean, EntityManager em) throws NewsfeedException {
        return null;
    }

    public Object insertActivity(Activity activity) throws NewsfeedException {
        if (activity != null) {
            EntityManager em = JPAService.getEntityManager();
            try {
                em.getTransaction().begin();
                em.persist(activity);
                if (em.getTransaction().isActive()) {
                    em.getTransaction().commit();
                }
                return activity.getId();
            } catch (PersistenceException e) {
                throw new NewsfeedException(e);
            } finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
        return null;
    }

    private Activity constructBean(ActivityQuery namedQuery, Object ret, Object... parameters)
            throws NewsfeedException {
        Activity bean;
        Object[] arr;
        switch (namedQuery) {
            case GET_BYFOLLOWING_FORWARD_ASC:
            case GET_BYFOLLOWING_FORWARD_DESC:
            case GET_BYFOLLOWING_BACKWARD_ASC:
            case GET_BYFOLLOWING_BACKWARD_DESC:
                bean = new Activity();
                arr = (Object[]) ret;
                bean.setId((Long) arr[0]);
                bean.setUserId((Long) arr[1]);
                bean.setMessage((String) arr[2]);
                bean.setCreatedTime((Date) arr[3]);
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot construct bean for " + namedQuery.name());
        }
        return bean;
    }

    public enum ActivityQuery {
        GET_BYFOLLOWING_FORWARD_ASC,
        GET_BYFOLLOWING_FORWARD_DESC,
        GET_BYFOLLOWING_BACKWARD_ASC,
        GET_BYFOLLOWING_BACKWARD_DESC
    }


}
