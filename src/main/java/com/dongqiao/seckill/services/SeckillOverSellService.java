package com.dongqiao.seckill.services;

import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.po.SeckillActivity;
import com.dongqiao.seckill.exception.ShopException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    public String  processSeckill(long activityId) throws ShopException {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(activityId);
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        if (availableStock > 0) {
            result = "success";
            System.out.println(result);
            availableStock = availableStock - 1;
            seckillActivity.setAvailableStock(new Integer("" + availableStock));
            seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
            result = "sold out";
            System.out.println(result);
        }
        return result;
    }
}
