package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.exception.ShopException;
import org.springframework.stereotype.Component;

import org.hibernate.HibernateException;

@Component
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
        return getSession().get(Order.class, orderNo);
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
