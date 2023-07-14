package com.nowcoder.communtiy;

import com.nowcoder.communtiy.config.HotelConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class XCHotelPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        //page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/[\\w\\-]+/[\\w\\-]+)").all());
        //page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/[\\w\\-])").all());
        //page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());

        List<String> titles = page.getHtml().xpath("//table[@id='contentTable']/tbody/tr/td[4]/a/text()").all();
        page.putField("titles", titles);
        if (page.getResultItems().get("titles")==null){
            //skip this page
            page.setSkip(true);
        }
//        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) throws InterruptedException {
        ApplicationContext  applicationContext = SpringApplication.run(XCHotelPageProcessor.class, args);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features");
        // !设置为开发者模式，避免网站识别出selenium
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        WebDriver driver = new ChromeDriver(options);
        HotelConfig hotelConfig = (HotelConfig) applicationContext.getBean("hotelConfig");
        Set<Cookie> cookies;
        Scanner scanner = new Scanner(System.in);
        System.out.print("get xiecheng login cookie?(y/n):");
        if (scanner.next().equalsIgnoreCase("y")) {
            driver.get(hotelConfig.getOriginUrl());
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            // 等待登录动作完成
            scanner.next();
            // 获取当前会话的所有 cookie
            cookies = driver.manage().getCookies();
            // 保存 cookie
            saveCookies(cookies, "cookies.txt");
            driver.quit();
        } else {
            driver.get(hotelConfig.getOriginUrl());
            driver.manage().deleteAllCookies();
            cookies = loadCookies("cookies.txt");
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    driver.manage().addCookie(cookie);
                }
            }
            driver.navigate().refresh();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            WebElement destination = driver.findElement(By.xpath("//input[@id='hotels-destination']"));
            destination.clear();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            destination.sendKeys("广州");
            WebElement search_btn = driver.findElement(By.className("hs_search-btn-container_R0HuJ"));
            Thread.sleep(1000);
            search_btn.click();
            System.out.print("scroll to bottom?(y/n):");
            if (scanner.next().equalsIgnoreCase("y")) {
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                while (driver.findElements(By.xpath("//div[@class='list-btn-more']//div[@class='btn-box']")).size() == 0) {
                    // 使用JavascriptExecutor将页面滚动到底部
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                }
                jsExecutor.executeScript("window.scrollTo(0, 0)");
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            }
            List<WebElement> elements = driver.findElements(By.xpath("//li[@class='list-item-target']"));
            //  记录根窗口句柄
            String rootWindowHandle = driver.getWindowHandle();
            boolean isAutoHotel = false;
            for (WebElement element : elements) {
                WebElement review_element = element.findElement(By.xpath(".//p[@class='count']//a"));
                review_element.click();
                // 获取所有窗口句柄
                Set<String> windowHandles = driver.getWindowHandles();
                // 判断是否发生了跳转
                if (windowHandles.size() > 1) {
                    // 循环判断新窗口句柄并切换到新窗口
                    for (String windowHandle : windowHandles) {
                        if (!windowHandle.equals(rootWindowHandle)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    // 在新窗口中执行其他操作
                    System.out.print("爬取该页评价并跳转下一页?(y/n/auto):");
                    String val = scanner.next();
                    boolean isAuto = val.equalsIgnoreCase("auto");
                    boolean isNext = val.equalsIgnoreCase("y");
                    while (isAuto || isNext) {
                        Thread.sleep(2000);
                        List<WebElement> reviews = driver.findElements(By.xpath("//div[@class='m-reviewCard-item']"));
                        for (WebElement review : reviews) {
                            // 格式化并打印数据，实现左对齐
                            System.out.printf("name:%-20s| ", review.findElement(By.xpath(".//p[@class='name']")).getText());
                            System.out.println("comment:" + review.findElement(By.xpath(".//div[@class='comment']//p")).getText());
                        }
                        try {
                            driver.findElement(By.xpath(".//a[@class='forward active']")).click();
                        } catch (NoSuchElementException exception) {
                            break;
                        }
                        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                        if (!isAuto) {
                            System.out.print("爬取该页评价并跳转下一页?(y/n):");
                            isNext = scanner.next().equalsIgnoreCase("y");
                        }
                    }

                    // 关闭新窗口并切换回原始窗口
                    driver.close();
                    driver.switchTo().window(rootWindowHandle);
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                }
                if (isAutoHotel == false) {
                    System.out.print("search next hotel?(y/n/auto):");
                    String val = scanner.next();
                    if (val.equalsIgnoreCase("auto")) {
                        isAutoHotel = true;
                    } else if (val.equalsIgnoreCase("n")) {
                        break;
                    }
                }
            }
        }
        System.out.print("exit?(y/n):");
        while (!scanner.next().equalsIgnoreCase("y")) {
        }
        System.exit(0);
    }
    private static Set<Cookie> loadCookies(String filePath) {
        try {
            FileInputStream fileIn = new FileInputStream(new File(filePath));
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Set<Cookie> cookies = (Set<Cookie>) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            System.out.println("Cookies loaded successfully.");
            return cookies;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void saveCookies(Set<Cookie> cookies, String filePath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(new File(filePath));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(cookies);
            objectOut.close();
            fileOut.close();
            System.out.println("Cookies saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}