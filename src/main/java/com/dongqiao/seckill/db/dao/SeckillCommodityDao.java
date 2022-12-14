package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.SeckillCommodity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public class SeckillCommodityDao extends DAO {

    public SeckillCommodity querySeckillCommodityById(long commodityId) {
        return getSession().get(SeckillCommodity.class, commodityId);
    }
}
