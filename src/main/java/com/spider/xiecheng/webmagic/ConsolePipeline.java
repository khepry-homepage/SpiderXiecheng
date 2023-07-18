package com.spider.xiecheng.webmagic;

import com.spider.xiecheng.utils.Constants;
import com.spider.xiecheng.utils.ProgressBar;
import com.spider.xiecheng.entity.Hotel;
import com.spider.xiecheng.entity.Review;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ConsolePipeline implements Pipeline, Constants {

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> data = resultItems.get("processed_data");
        List<Hotel> hotels = (List<Hotel>) data.get(HOTEL_LIST_KEY);
        for (Hotel hotel : hotels) {
            for (Review review : hotel.getReviews()) {
                System.out.printf(ProgressBar.ANSI_GREEN + "酒店名:%s ---- 地址:%s\n" + ProgressBar.ANSI_RESET, hotel.getName(), hotel.getAddress());
                System.out.printf("reviewer:%-20s| ", review.getReviewer());
                System.out.printf("content:%-30s| ", review.getContent());
                System.out.printf("date:%s\n", new SimpleDateFormat("yyyy-MM-dd").format(review.getDate()));
            }
        }
    }
}
