
package com.dongqiao.seckill.mq;


import com.alibaba.fastjson.JSON;
import com.dongqiao.seckill.db.dao.OrderDao;
import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.exception.ShopException;
import com.dongqiao.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received order status check request: " + message);
        Order order = JSON.parseObject(message, Order.class);
        //1.查询订单
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());

        //2.判读订单是否完成支付
        if (orderInfo.getOrderStatus() != 2) {
            //3.未完成支付关闭订单
            log.info("The user hasn't pay: " + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            try {
                orderDao.updateOrder(orderInfo);
            } catch (ShopException e) {
                throw new RuntimeException(e);
            }
            //4.恢复数据库库存
            try {
                seckillActivityDao.revertStock(order.getSeckillActivityId());
            } catch (ShopException e) {
                throw new RuntimeException(e);
            }
            // 恢复 redis 库存
//            redisService.revertStock("stock:" + order.getSeckillActivityId());
            //5.将用户从已购名单中移除
//            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());
        }
    }
}

