package com.dongqiao.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.exception.ShopException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;

/**
 * 支付完成消息处理
 * 扣减库存
 */
@Slf4j
@Component
@Transactional
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        //1.解析创建订单请求消息
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received order create request: " + message);
        Order order = JSON.parseObject(message, Order.class);
        //2.扣减库存
        try {
            seckillActivityDao.deductStock(order.getSeckillActivityId());
        } catch (ShopException e) {
            throw new RuntimeException(e);
        }
    }
}
