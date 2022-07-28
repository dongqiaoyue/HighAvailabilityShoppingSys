package com.dongqiao.seckill.db.dao;

import com.dongqiao.seckill.db.po.SeckillActivity;

import java.util.List;

public interface SeckillActivityDao {

    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus);

    public void inertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);

    public boolean deductStock(long activityId);

    public boolean lockStock(long activityId);

    void revertStock(Long seckillActivityId);

}
