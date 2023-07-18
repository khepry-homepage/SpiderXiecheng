package com.spider.xiecheng;

import com.spider.xiecheng.utils.Constants;
import com.spider.xiecheng.webmagic.SeleniumDownloader;
import com.spider.xiecheng.webmagic.WriteToExcelPipeline;
import com.spider.xiecheng.entity.Hotel;
import com.spider.xiecheng.entity.Review;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class XCHotelPageProcessor implements PageProcessor, Constants {

    private Site site = Site.me().setRetryTimes(0).setSleepTime(10000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        /**
         *      json格式
         *       {
         *           "爬取类型" : {
         *                "相关info(如地区)" : {},
         *                "酒店列表" : [
         *                    {
         *                        "酒店名",
         *                        "酒店地址",
         *                        "评论列表" : [
         *                          {
         *                              "评论人",
         *                              "评论内容",
         *                              "评论日期",
         *                          }]
         *                    }
         *                ]
         *           }
         *       }
         *
         */
        Map<String, Object> data = page.getResultItems().get("data");
        Map<String, Object> processedData = new HashMap<>();
        if (data == null) {
            //skip this page
            page.setSkip(true);
            return;
        }
        List<List<String>> hotels = (List<List<String>>) data.get(HOTEL_LIST_KEY);
        List<Hotel> processedHotels = new ArrayList<>();
        for (List<String> reviewHtmls : hotels) {
            List<Review> reviews = new ArrayList<>();
            Hotel processedHotel = new Hotel();
            for (String reviewHtml : reviewHtmls) {
                // 使用Jsoup解析HTML源文本
                Document document = Jsoup.parse(reviewHtml);
                // 使用类选择器查找对应的DOM元素
                if (null == processedHotel.getName()) {
                    processedHotel.setName(document.select("h1.detail-headline_name").text())
                            .setAddress(document.select("span.detail-headline_position_text").text());
                }
                Elements elements = document.select("div.m-reviewCard-item");
                for (Element element : elements) {
                    Date date;
                    try {
                        date = new SimpleDateFormat("yyyy年M月d日").parse(element.selectFirst("div.reviewDate").text().split(" ")[0]);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    reviews.add(new Review().setReviewer(element.selectFirst("p.name").text())
                            .setContent(element.selectFirst("div.comment > p").text())
                            .setDate(date));
                }
            }
            processedHotel.setReviews(reviews);
            processedHotels.add(processedHotel);
        }
        processedData.put(SEARCH_CONFIG, data.get(SEARCH_CONFIG));
        processedData.put(HOTEL_LIST_KEY, processedHotels);
        data.clear();
        page.putField("processed_data", processedData);
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) throws InterruptedException {
        Spider.create(new XCHotelPageProcessor())
                .addUrl("https://www.ctrip.com/")
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new WriteToExcelPipeline())
                //开启1个线程抓取
                .thread(1)
                //启动爬虫
                .run();
    }
}