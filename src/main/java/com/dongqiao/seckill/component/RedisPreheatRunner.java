package com.dongqiao.seckill.component;

import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.po.SeckillActivity;
import com.dongqiao.seckill.util.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisPreheatRunner  implements ApplicationRunner {
    @Resource
    RedisService redisService;

    @Resource
    SeckillActivityDao seckillActivityDao;

    /**
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        for (SeckillActivity seckillActivity : seckillActivities) {
            redisService.setValue("stock:" + seckillActivity.getId(),
                    (long) seckillActivity.getAvailableStock());
        }
    }
}
