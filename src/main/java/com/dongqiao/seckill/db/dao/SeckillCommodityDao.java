package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.SeckillCommodity;
import com.dongqiao.seckill.exception.ShopException;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Repository;

@Repository
public class SeckillCommodityDao extends DAO {

    public SeckillCommodity querySeckillCommodityById(long commodityId) {
        return getSession().get(SeckillCommodity.class, commodityId);
    }

    public void insertCommodity(SeckillCommodity comm) throws ShopException {
        try {
            begin();
            getSession().save(comm);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new ShopException("Could not insert", e);
        }
    }
}
