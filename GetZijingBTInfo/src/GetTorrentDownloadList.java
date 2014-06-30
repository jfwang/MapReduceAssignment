import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * 获取紫荆种子的已完成下载列表
 * Created by user36 on 6/25/14.
 */
public class GetTorrentDownloadList {
    static PrintWriter outputWriter;
    static PrintWriter failWriter;

    /**
     * 向文件写入一条下载记录
     * 写入格式是“种子ID \t 用户1,用户2,用户3,... \n”
     *
     * @param tid     种子ID
     * @param uidList 用户列表
     */
    public static void writeRecord(String tid, List<String> uidList) {
        if(uidList == null) return;
        outputWriter.print(tid + "\t");
        Iterator<String> it;
        StringBuilder builder = new StringBuilder();
        for (it = uidList.iterator(); it.hasNext(); ) {
            builder.append(it.next());
            if (it.hasNext()) builder.append(',');
        }
        outputWriter.println(builder.toString());
    //    System.out.println(builder.toString());
        outputWriter.flush();
    }


    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        outputWriter = new PrintWriter("torrentDownloadList.txt");
        failWriter = new PrintWriter("fail.txt");
        //从tID.unique.txt中读取种子ID列表
        Scanner scanner = new Scanner(new File("tID.unique.txt"));
        while (scanner.hasNextLine()) {
            String tid = scanner.nextLine();
            System.err.println("---- " + tid + " --->");
            List<String> uidList = getDownloadUserList(tid);
            writeRecord(tid, uidList);
            Thread.sleep(500);
        }
        outputWriter.close();
        scanner.close();
    }

    /**
     * 获取某个种子的已完成下载用户列表
     *
     * @param tid 种子ID
     * @return 用户ID列表
     */
    public static List<String> getDownloadUserList(String tid) {
        //获得种子已下载用户列表的信息页
        String url = "http://zijingbt.njuftp.org/stats.html?id=" + tid + "&show=completes#completes";
        Document document = getURLDocument(url);
        if(document == null){
            failWriter.println(tid);
            failWriter.flush();
            return null;
        }
        assert (document != null);
        //获得用户名单
        Elements table = document.getElementsByAttributeValue("summary","completes");
        Elements members = table.select("a.member");
        members.addAll(table.select("a.friends"));
        members.addAll(table.select("a.uploader"));
        LinkedList<String> linkedList = new LinkedList<String>();
        for(Element member :members){
            //System.out.println(member.ownText());
            linkedList.add(member.ownText());
        }
        return linkedList;
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
        if (tryCount == MAX_TRY_COUNT) {
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

}