package com.nexr.newsfeed.jpa;

import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FriendQueryExceutor extends QueryExecutor<Friend, FriendQueryExceutor.FriendQuery> {
    private static Logger log = LoggerFactory.getLogger(FriendQueryExceutor.class);

    public Friend get(FriendQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        Object ret = JPAService.executeGet(namedQuery.name(), query, em);
        if (ret == null) {
            log.debug("query [{}]", query.toString());
            throw new NewsfeedException("Friend Not Found ");
        }
        Friend bean = constructBean(namedQuery, ret, parameters);
        return bean;
    }

    public Query getSelectQuery(FriendQuery namedQuery, EntityManager em, Object... parameters)
            throws NewsfeedException {
        Query query = em.createNamedQuery(namedQuery.name());
        switch (namedQuery) {
            case GET_FOLLOWING:
                query.setParameter("userId", parameters[0]);
                query.setParameter("followingId", parameters[1]);
                break;
            case GET_FOLLOWINGS:
                query.setParameter("userId", parameters[0]);
                break;
            case GET_FOLLOWING_ALL:
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot set parameters for " + namedQuery.name());
        }
        return query;
    }

    public Object insertFriend(Friend friend) throws NewsfeedException {
        if (friend != null) {
            EntityManager em = JPAService.getEntityManager();
            try {
                em.getTransaction().begin();
                em.persist(friend);
                if (em.getTransaction().isActive()) {
                    em.getTransaction().commit();
                }
                return friend.getId();
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

    @Override
    public int executeUpdate(FriendQuery namedQuery, Friend bean) throws NewsfeedException {
        return 0;
    }

    public List<Friend> getList(FriendQuery namedQuery, Object... parameters) throws NewsfeedException {
        EntityManager em = JPAService.getEntityManager();
        Query query = getSelectQuery(namedQuery, em, parameters);
        List<?> retList = (List<?>) JPAService.executeGetList(namedQuery.name(), query, em);
        List<Friend> list = new ArrayList<>();
        if (retList != null) {
            for (Object ret : retList) {
                list.add(constructBean(namedQuery, ret));
            }
        }
        return list;
    }

    @Override
    public Query getUpdateQuery(FriendQuery namedQuery, Friend bean, EntityManager em) throws NewsfeedException {
        return null;
    }

    private Friend constructBean(FriendQuery namedQuery, Object ret, Object... parameters)
            throws NewsfeedException {
        Friend bean;
        Object[] arr;
        switch (namedQuery) {
            case GET_FOLLOWING:
            case GET_FOLLOWINGS:
            case GET_FOLLOWING_ALL:
                bean = new Friend();
                arr = (Object[]) ret;
                bean.setUserId((Long) arr[0]);
                bean.setFollowingId((Long) arr[1]);
                bean.setCreatedTime((Date) arr[2]);
                break;
            default:
                throw new NewsfeedException("QueryExecutor cannot construct bean for " + namedQuery.name());
        }
        return bean;
    }

    public enum FriendQuery {
        GET_FOLLOWINGS,
        GET_FOLLOWING,
        GET_FOLLOWING_ALL
    }


}
