package com.dongqiao.seckill.web;

import com.dongqiao.seckill.exception.ShopException;
import com.dongqiao.seckill.services.SeckillActivityService;
import com.dongqiao.seckill.services.SeckillOverSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillOverSellController {

    @Autowired
    private SeckillOverSellService seckillOverSellService;


    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
//    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckil(@PathVariable long seckillActivityId) throws ShopException {
       return seckillOverSellService.processSeckill(seckillActivityId);
    }

    /**
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId) {
        boolean stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidateResult ? "success" : "sold out";
    }

}
