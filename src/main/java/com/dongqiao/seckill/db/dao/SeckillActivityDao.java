package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.SeckillActivity;
import com.dongqiao.seckill.exception.ShopException;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeckillActivityDao extends DAO {

    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus) {
        Query<SeckillActivity> query = getSession().createQuery("FROM SeckillActivity where activityStatus =: status");
        query.setParameter("status", activityStatus);
        List<SeckillActivity> list = query.list();

        return list;
    }

    public void inertSeckillActivity(SeckillActivity seckillActivity) throws ShopException {
        try {
            begin();
            getSession().save(seckillActivity);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not insert", e);
        }
    }

    public SeckillActivity querySeckillActivityById(long activityId) {
        return getSession().get(SeckillActivity.class, activityId);
    }

    public void updateSeckillActivity(SeckillActivity seckillActivity) throws ShopException {
        try {
            begin();
            getSession().update(seckillActivity);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not update", e);
        }
    }

    public boolean deductStock(long activityId) throws ShopException {
        try {
            begin();
            Query query = getSession().createQuery("update SeckillActivity set lockStock = lockStock - 1 where id = :i");
            query.setParameter("i", activityId);
            query.executeUpdate();
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not deduct", e);
        }

        return true;
    }

    public boolean lockStock(long activityId) throws ShopException {
        try {
            begin();
            Query query = getSession().createQuery("update SeckillActivity set availableStock = availableStock - 1, lockStock = lockStock + 1 where id = :i and availableStock > 0");
            query.setParameter("i", activityId);
            query.executeUpdate();
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not lock", e);
        }

        return true;
    }

    public void revertStock(Long activityId) throws ShopException {
        try {
            begin();
            Query query = getSession().createQuery("update SeckillActivity set lockStock = lockStock - 1, availableStock = availableStock + 1 where id = :i");
            query.setParameter("i", activityId);
            query.executeUpdate();
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not revert", e);
        }
    }

}
