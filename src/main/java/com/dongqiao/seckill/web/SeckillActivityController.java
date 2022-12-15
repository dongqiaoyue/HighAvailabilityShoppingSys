package com.dongqiao.seckill.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.dongqiao.seckill.db.dao.OrderDao;
import com.dongqiao.seckill.db.dao.SeckillActivityDao;
import com.dongqiao.seckill.db.dao.SeckillCommodityDao;
import com.dongqiao.seckill.db.po.Order;
import com.dongqiao.seckill.db.po.SeckillActivity;
import com.dongqiao.seckill.db.po.SeckillCommodity;
import com.dongqiao.seckill.exception.ShopException;
import com.dongqiao.seckill.services.SeckillActivityService;
import com.dongqiao.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    RedisService redisService;

    /**
     * seckill activity list
     *
     * @param resultMap
     * @return
     */
    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        try (Entry entry = SphU.entry("seckills")) {
            List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
            resultMap.put("seckillActivities", seckillActivities);
            return "seckill_activity";
        } catch (BlockException ex) {
            log.error(ex.toString());
            return "wait";
        }
    }

    /**
     * commodity details
     * @param resultMap
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;

        String seckillActivityInfo = redisService.getValue("seckillActivity:" + seckillActivityId);
        if (StringUtils.isNotEmpty(seckillActivityInfo)) {
            log.info("redis缓存数据:" + seckillActivityInfo);
            seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
        } else {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }

        String seckillCommodityInfo = redisService.getValue("seckillCommodity:" + seckillActivity.getCommodityId());
        if (StringUtils.isNotEmpty(seckillCommodityInfo)) {
            log.info("redis缓存数据:" + seckillCommodityInfo);
            seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
        } else {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }

        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    //    @ResponseBody
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException, ShopException {
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        seckillActivityService.pushSeckillInfoToRedis(seckillActivity.getId());
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    /**
     * @return
     */
    @RequestMapping("/")
    public String addSeckillActivity(){
        return "add_activity";
    }

    /**
     * @return
     */
    @RequestMapping("/addProduct")
    public String addCommodity(){
        return "add_commodity";
    }

    @RequestMapping("/addCommodityAction")
    public String addSeckillActivityAction(
            @RequestParam("commodityName") String commodityName,
            @RequestParam("commodityDesc") String commodityDesc,
            @RequestParam("commodityPrice") Integer commodityPrice,
            Map<String, Object> resultMap
    ) throws ParseException, ShopException {

        SeckillCommodity comm = new SeckillCommodity();
        comm.setCommodityDesc(commodityDesc);
        comm.setCommodityName(commodityName);
        comm.setCommodityPrice(commodityPrice);
        seckillCommodityDao.insertCommodity(comm);
        resultMap.put("seckillCommodity", comm);
        return "add_success_comm";
    }

    /**
     * process seckill request
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId, @PathVariable long seckillActivityId) {
        boolean stockValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
             * 判断用户是否在已购名单中
             */
//            if (redisService.isInLimitMember(seckillActivityId, userId)) {
//                //提示用户已经在限购名单中，返回结果
//                modelAndView.addObject("resultInfo", "对不起，您已经在限购名单中");
//                modelAndView.setViewName("seckill_result");
//                return modelAndView;
//            }
            /*
             * 确认是否能够进行秒杀
             */
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo","Ordered success, Order Number：" + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                //添加用户到已购名单中
//                redisService.addLimitMember(seckillActivityId, userId);
            } else {
                modelAndView.addObject("resultInfo","Sold out");
            }
        } catch (Exception e) {
            log.error("Error:" + e.toString());
            modelAndView.addObject("resultInfo","Fail");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /**
     * check
     * @param orderNo
     * @return
     */
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        log.info("Order querying, order number:  " + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

    /**
     * payment
     * @return
     */
    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }

    /**
     * 获取当前服务器端的时间
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间
        return date;
    }
}
