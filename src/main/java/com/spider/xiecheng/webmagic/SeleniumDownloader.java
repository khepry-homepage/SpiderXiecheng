package com.spider.xiecheng.webmagic;

import com.spider.xiecheng.utils.Constants;
import com.spider.xiecheng.utils.ProgressBar;
import com.spider.xiecheng.config.HotelConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;

import java.io.*;
import java.time.Duration;
import java.util.*;

public class SeleniumDownloader implements Downloader, Constants {
    private HotelConfig hotelConfig;
    private WebDriver driver;
    private WebDriverWait driverWait;
    private ProgressBar progressBar;
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
    public SeleniumDownloader() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features");
//        options.addArguments("--headless"); //  无界面模式
        //options.addArguments("--disable-gpu");
        // !设置为开发者模式，避免网站识别出selenium
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        this.driver = new ChromeDriver(options);
        this.hotelConfig = new HotelConfig();
        this.progressBar = new ProgressBar();
        this.driverWait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
    }

    /**
     * json格式
     * {
     *     "爬取类型" : {
     *          "相关info(如地区)" : {
     *
     *          },
     *          "酒店列表" : [
     *              "酒店评论页面列表" : []
     *          ]
     *     }
     * }
     * @param request
     * @param task
     * @return
     */
    @Override
    public Page download(Request request, Task task) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("get xiecheng login cookie?(y/n):");
        if (scanner.next().equalsIgnoreCase("y")) {
            // 获取当前会话的所有 cookie
            driver.get(request.getUrl());
            System.out.print("login success?(enter any):");
            // 等待登录动作完成
            scanner.next();
            Set<Cookie> cookies = driver.manage().getCookies();
            // 保存 cookie
            saveCookies(cookies, "cookies.txt");
        }

        Map<String, Object> data = new HashMap<>();         //  爬虫信息
        List<List<String>> hotels = new ArrayList<>();      //  酒店评论页列表
        Set<Cookie> cookies;
        //  加载免登录验证的cookie
        driver.get(request.getUrl());
        driver.manage().deleteAllCookies();
        cookies = loadCookies("cookies.txt");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                driver.manage().addCookie(cookie);
            }
        }
        driver.navigate().refresh();
        WebElement destination = driverWait.until(driver -> {
            WebElement element = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='hotels-destination']")));
            return !element.getAttribute("value").isEmpty() ? element : null;
        });
        destination.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        destination.sendKeys(hotelConfig.getRegion());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        driver.findElement(By.className("hs_search-btn-container_R0HuJ")).click();
        WebElement keyword = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='keyword']")));
        keyword.sendKeys(hotelConfig.getKeyword());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        driver.findElement(By.className("search-btn-wrap")).click();
        System.out.print("scroll to bottom?(y/n):");
        if (scanner.next().equalsIgnoreCase("y")) {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            while (driver.findElements(By.xpath("//div[@class='list-btn-more']//div[@class='btn-box']")).size() == 0) {
                // 使用JavascriptExecutor将页面滚动到底部
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            }
            jsExecutor.executeScript("window.scrollTo(0, 0)");
        }
        List<WebElement> elements = driverWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                By.xpath("//li[@class='list-item-target']")));
        //  记录根窗口句柄
        String rootWindowHandle = driver.getWindowHandle();
        int current = 0;
        // 在新窗口中执行其他操作
        System.out.println("*******************开始提取酒店评价*****************");
        progressBar.update(current * 100 / elements.size());
        //  遍历所有酒店
        for (WebElement element : elements) {
            List<String> reviewHtmls = new ArrayList<>();
            WebElement reviewElement = element.findElement(By.xpath(".//p[@class='count']//a"));
            try {
                driverWait.until(ExpectedConditions.elementToBeClickable(reviewElement)).click();
            } catch (TimeoutException exception) {
                continue;
            }
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
                By parentElementLocator = By.xpath("//div[@class='m-review-tag']//div[@class='m-fastfilter']"); // 指定父元素的XPath表达式
                By childElementLocator = By.xpath(".//button"); // 指定子元素的XPath表达式
                By loadingElementLocator = By.xpath("//div[@class='m-reviewLoading']"); // 指定加载动画的XPath表达式
                int count = 0;
                while (++count <= MAX_REVIEW_PAGE_COUNT) {
                    driverWait.until(ExpectedConditions.invisibilityOfElementLocated(loadingElementLocator));
                    // 携程酒店详情页面首次加载仅有2条评论，此处确保详情页加载完全
                    driverWait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parentElementLocator, childElementLocator));
                    reviewHtmls.add(driver.getPageSource());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        //  跳转下页
                        driverWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='forward active']"))).click();
                    } catch (TimeoutException exception) {
                        //  已达终页
                        break;
                    }
                }
                // 关闭新窗口并切换回原始窗口
                driver.close();
                driver.switchTo().window(rootWindowHandle);
            }
            hotels.add(reviewHtmls);
            progressBar.update(++current * 100 / elements.size());
        }
        System.out.println("************************完成**********************");
        data.put(HOTEL_LIST_KEY, hotels);
        data.put(SEARCH_CONFIG, hotelConfig);
        Page page = new Page();
        page.putField("data", data);
        driver.close();
        return page;
    }

    @Override
    public void setThread(int i) {

    }
}
