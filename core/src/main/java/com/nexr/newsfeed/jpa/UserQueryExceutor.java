package com.nexr.newsfeed.jpa;

import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserQueryExceutor extends QueryExecutor<User, UserQueryExceutor.UserQuery> {

    public User get(UserQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        Object ret = JPAService.executeGet(namedQuery.name(), query, em);
        if (ret == null) {
            throw new NewsfeedException("User Not Found: " + query.toString());
        }
        User bean = constructBean(namedQuery, ret, parameters);
        return bean;
    }

    public Query getSelectQuery(UserQuery namedQuery, EntityManager em, Object... parameters)
            throws NewsfeedException {
        Query query = em.createNamedQuery(namedQuery.name());
        switch (namedQuery) {
            case GET_BYID:
                query.setParameter("id", parameters[0]);
                break;
            case GET_USER_ALL:
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot set parameters for " + namedQuery.name());
        }
        return query;
    }

    @Override
    public int executeUpdate(UserQuery namedQuery, User bean) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getUpdateQuery(namedQuery, bean, em);
        int ret = JPAService.executeUpdate(namedQuery.name(), query, em);
        return ret;
    }


    public List<User> getList(UserQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        List<?> retList = (List<?>) JPAService.executeGetList(namedQuery.name(), query, em);
        List<User> list = new ArrayList<>();
        if (retList != null) {
            for (Object ret : retList) {
                list.add(constructBean(namedQuery, ret));
            }
        }
        return list;
    }

    @Override
    public Query getUpdateQuery(UserQuery namedQuery, User bean, EntityManager em) throws NewsfeedException {
        Query query = em.createNamedQuery(namedQuery.name());
        switch (namedQuery) {
            case UPDATE_LASTVIEW_TIME:
                query.setParameter("lastViewTime", bean.getLastViewTime());
                query.setParameter("id", bean.getId());
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot set parameters for " + namedQuery.name());
        }
        return query;
    }

    public Object insertUser(User user) throws NewsfeedException {
        if (user != null) {
            EntityManager em = JPAService.getEntityManager();
            try {
                em.getTransaction().begin();
                em.persist(user);
                if (em.getTransaction().isActive()) {
                    em.getTransaction().commit();
                }
                return user.getId();
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

    private User constructBean(UserQuery namedQuery, Object ret, Object... parameters)
            throws NewsfeedException {
        User bean;
        Object[] arr;
        switch (namedQuery) {
            case GET_BYID:
            case GET_USER_ALL:
                bean = new User();
                arr = (Object[]) ret;
                bean.setId((Long) arr[0]);
                bean.setName((String) arr[1]);
                bean.setEmail((String) arr[2]);
                bean.setLastViewTime((Date) arr[3]);
                bean.setCreatedTime((Date) arr[4]);
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot construct bean for " + namedQuery.name());
        }
        return bean;
    }

    public enum UserQuery {
        GET_BYID,
        GET_USER_ALL,
        UPDATE_LASTVIEW_TIME;
    }


}
