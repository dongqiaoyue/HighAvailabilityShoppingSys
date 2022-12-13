package com.dongqiao.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.dongqiao.seckill.db.dao.OrderDao;
import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.exception.ShopException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        //1.解析创建订单请求消息
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("接收到创建订单请求：" + message);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());
        //2.扣减库存
        boolean lockStockResult = false;
        try {
            lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        } catch (ShopException e) {
            throw new RuntimeException(e);
        }
        if (lockStockResult) {
            //订单状态 0:没有可用库存，无效订单 1:已创建等待付款
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        //3.插入订单
        try {
            orderDao.insertOrder(order);
        } catch (ShopException e) {
            throw new RuntimeException(e);
        }
    }
}
