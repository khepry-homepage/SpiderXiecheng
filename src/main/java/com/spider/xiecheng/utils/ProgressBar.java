package com.spider.xiecheng.utils;

public class ProgressBar {
    private String start_symbol = "[";
    private String end_symbol = "]";
    private String progress_symbol = "=";
    // ANSI转义序列：绿色
    public static final String ANSI_GREEN = "\u001B[32m";
    // ANSI转义序列：重置颜色
    public static final String ANSI_RESET = "\u001B[0m";
    private int total = 100;
    public void update(int current) {
        assert (current <= 100);
        int percentage = current * 100 / total;
        int completed = percentage / 2;
        // 打印进度条
        System.out.print(ANSI_GREEN + start_symbol);
        for (int i = 0; i < completed; i++) {
            System.out.print(progress_symbol);
        }
        for (int i = completed; i < 50; i++) {
            System.out.print(" ");
        }
        System.out.print(end_symbol + " " + percentage + "%" + ANSI_RESET);

        // 移动光标到进度条起始位置
        System.out.print("\r");
    }
}
