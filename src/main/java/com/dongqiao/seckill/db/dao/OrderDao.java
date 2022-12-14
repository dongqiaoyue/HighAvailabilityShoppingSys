package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.exception.ShopException;
import org.hibernate.query.Query;


import org.hibernate.HibernateException;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDao extends DAO {

    public void insertOrder(Order order) throws ShopException {
        try {
            begin();
            getSession().save(order);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not insert the order", e);
        }
    }

    public Order queryOrder(String orderNo) {
//        return getSession().get(Order.class, orderNo);
        Query query = getSession().createQuery("from Order where orderNo=:id");
        query.setParameter("id", orderNo);
         return (Order) query.uniqueResult();
    }

    public void updateOrder(Order order) throws ShopException {
        try {
            begin();
            getSession().update(order);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not update the order", e);
        }
    }
}
