/**
 * 从紫荆的种子列表页面获取所有的有效种子URL列表
 * Created by user36 on 6/25/14.
 */
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class GetTorrentList {
    public static void main(String[] args) throws FileNotFoundException {
        LinkedList<String> tIDList = new LinkedList<String>();
        int pageCount = 0;
        final int MAX_PAGE_COUNT = 894;//2014.6.25日的最大页数

        while(pageCount <= MAX_PAGE_COUNT){
            String currentWorkingPageURL = "http://zijingbt.njuftp.org/index.html?page=" + pageCount;
            System.err.println("[PAGE]" + pageCount);
            Document pageDocument = getURLDocument(currentWorkingPageURL);
            List<String> list = getTorrentIDList(pageDocument);
            if(list.size() != 50 && pageCount < MAX_PAGE_COUNT){
                System.out.println("[LIST Size ERROR!]");
                return;
            }
            tIDList.addAll(list);
            System.err.println("[INFO] tID List Size :" + tIDList.size());
            pageCount++;
            try {
                Thread.sleep(1000);//Wait for 1 second.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PrintWriter writer = new PrintWriter("tIDList.txt");
        for(String tid : tIDList){
            writer.println(tid);
        }
        writer.close();
    }

    //获取指定URL的页面
    public static Document getURLDocument(String url) {
        Connection c = Jsoup.connect(url);
        c
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2017.2 Safari/537.36 OPR/24.0.1537.0 (Edition Developer)");
        c.cookie("login", "bsidb");
        c.cookie("uid", "3435");
        c.cookie("md5", "%3D%0CjZ%82%96%D6%2C%01S%3FY%26%3F%B6B");
        c.cookie("per_page", "50");
        // "login="bsidb"; uid="3435"; md5="%3D%0CjZ%82%96%D6%2C%01S%3FY%26%3F%B6B"; per_page="50";
        Connection.Response res = null;

        int tryCount = 0;
        final int MAX_TRY_COUNT = 3;//最多尝试三次
        while (true && tryCount < MAX_TRY_COUNT) {
            try {
                c.timeout(10000);
                res = c.execute();
                break;
            } catch (Exception e) {
                System.err.println("[GET URL ERROR]try to get " + url + ", " + tryCount + " time fails");
//                e.printStackTrace();
                System.err.println("we will try it later");
            }
            try {
                Thread.sleep(5000);//5s后重试
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.err.println("Try again.");
            tryCount++;
        }
        if(tryCount == MAX_TRY_COUNT){
            System.err.println("[GET URL ERROR] url : " + url + " reaches max try times.Fails.\n");
            return null;//返回null对象
        }
        Document doc = null;
        try {
            doc = res.parse();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[GET URL ERROR]Parse fails for " + url);
        }
        //System.out.println("Got!");
        return doc;
    }

    /**
     * 获取某一页上的所有种子介绍页的URL链接列表
     * @param page 紫荆种子列表页URL
     * @return 种子ID列表
     */
    public static List<String> getTorrentIDList(Document page){
        //System.out.println("Hello!");
        LinkedList<String> tIDList = new LinkedList<String>();
        //选择所有链接中class属性以stats开头的元素，即种子标题信息元素
        Elements topElements = page.select("a[class^=stats]");
        Elements anotherElements = page.select("a[class^=class]");
     //   System.out.println("top:" +topElements.size() +" another:" + anotherElements.size());
        Elements allElements = new Elements();
        allElements.addAll(topElements);
        allElements.addAll(anotherElements);
        int count = 0;
        for(Element element :allElements){
            String link =  element.attr("href");
            String tid = link.split("=")[1];
            if(tid.matches("[0-9]*")) {
                tIDList.add(tid);
                count ++;
            }
        }
       // System.out.println(count);
        return tIDList;
    }


}
