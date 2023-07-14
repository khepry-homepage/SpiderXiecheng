package com.nowcoder.communtiy.WebMagic;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConsolePipeline implements Pipeline {

    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("get page: " + resultItems.getRequest().getUrl());
        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            if (entry.getKey().equals("titles")) {
                List<String> titles = (List<String>)entry.getValue();
                for (String title : titles) {
                    System.out.println("title:\t" + title);
                }
            } else {
                System.out.println(entry.getKey() + ":\t" + entry.getValue());
            }
        }
    }
}
