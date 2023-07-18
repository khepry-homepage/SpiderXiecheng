package com.spider.xiecheng.webmagic;

import com.spider.xiecheng.utils.Constants;
import com.spider.xiecheng.utils.ExcelWriter;
import com.spider.xiecheng.utils.ProgressBar;
import com.spider.xiecheng.config.HotelConfig;
import com.spider.xiecheng.entity.Hotel;
import com.spider.xiecheng.entity.Review;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class WriteToExcelPipeline implements Pipeline, Constants {
    private ProgressBar progressBar = new ProgressBar();

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> data = resultItems.get("processed_data");
        HotelConfig hotelConfig = (HotelConfig) data.get(SEARCH_CONFIG);
        List<Hotel> hotels = (List<Hotel>) data.get(HOTEL_LIST_KEY);
        System.out.println("*******************写入excel文件*****************");
        int current = 0;
        progressBar.update(current * 100 / hotels.size());
        int row = 0, col = -1;
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.open("xiecheng_hotel");
        String[] titles = new String[]{"城市", "关键词", "酒店名", "地址", "评论用户名", "评论内容", "评论日期"};
        for (int i = 0; i < titles.length; i++) {
            excelWriter.write(row, ++col, titles[i]);
        }
        for (Hotel hotel : hotels) {
            for (Review review : hotel.getReviews()) {
                ++row;
                col = -1;
                excelWriter.write(row, ++col, hotelConfig.getRegion());
                excelWriter.write(row, ++col, hotelConfig.getKeyword());
                excelWriter.write(row, ++col, hotel.getName());
                excelWriter.write(row, ++col, hotel.getAddress());
                excelWriter.write(row, ++col, review.getReviewer());
                excelWriter.write(row, ++col, review.getContent());
                excelWriter.write(row, ++col, new SimpleDateFormat("yyyy-MM-dd").format(review.getDate()));
            }
            progressBar.update(++current * 100 / hotels.size());
        }
        excelWriter.save();
        System.out.println("***********************完成*********************");
    }
}
