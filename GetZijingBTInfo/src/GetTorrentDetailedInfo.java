import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetTorrentDetailedInfo {

	// TODO Auto-generated method stub
	// 获得种子的简介信息
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(new File(args[0]));
		while (scanner.hasNextLine()) {
			String url = scanner.nextLine();
			try {
				System.out.println(url);
				Document document = getMoviePage(url);
				if (document == null) {
					System.err.println("fail: " + url);
					continue;
				}
				String html = parseDetailHTML(document);
				 System.out.println(html);

			} catch (Exception e) {
				 e.printStackTrace();
				System.err.println("fail: " + url);
			}
		}

	}

	public static Document getMoviePage(String url) {
		Connection c = Jsoup.connect(url);
		c
				.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2017.2 Safari/537.36 OPR/24.0.1537.0 (Edition Developer)");
		c.cookie("login", "bsidb");
		c.cookie("uid", "3435");
		c.cookie("md5", "%3D%0CjZ%82%96%D6%2C%01S%3FY%26%3F%B6B");
		c.cookie("per_page", "50");
		// "login="bsidb"; uid="3435"; md5="%3D%0CjZ%82%96%D6%2C%01S%3FY%26%3F%B6B"; per_page="50";
		Connection.Response res = null;
		while (true) {
			try {
				c.timeout(10000);
				res = c.execute();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("we will try it later");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Try again.");
		}
		Document doc = null;
		try {
			doc = res.parse();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Parse fails");
		}
		System.out.println("Got!");
		return doc;
	}

	// 解析HTML，获得简介部分
	public static String parseDetailHTML(Document document) {
		Elements e = document.select("tr#notes.file_info");
		Element detailLine = e.first().nextElementSibling();
		Element detail = detailLine.child(1);
		return detail.html();
	}

}
