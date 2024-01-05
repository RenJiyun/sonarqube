package com.wlzq.activity.guess.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 同花顺行情
 */
@Data
public class QuotationDto {
    /*现价*/
    public final static Integer PRICE_CURRENT = 10;
    /*昨收价*/
    public final static Integer PRICE_CLOSEING = 6;

    /*结果集*/
    private DataResult DataResult;

    @Data
    public static class DataResult{
        /*计算周期*/
        private Integer Period;
        /*记录*/
        private Record Record;
    }

    @Data
    public static class Record {
        private List<StockPrice> Item;

        /*现价*/
        public Double getCurrentPrice() {

            return getStockPrice(PRICE_CURRENT);
        }

        /*昨收价*/
        public Double getClosingPrice() {

            return getStockPrice(PRICE_CLOSEING);
        }

        private Double getStockPrice(Integer priceType) {
            Optional<StockPrice> first = getItem().stream().filter(stockPrice -> Objects.equals(stockPrice.Id, priceType)).findFirst();
            return first.map(stockPrice -> ((BigDecimal) stockPrice.getValue()).doubleValue()).orElse(null);
        }
    }

    @Data
    public static class StockPrice{
        private Integer H;
        private Integer Market;
        private Integer Id;
        private Object Value;
    }
}
