package novelcatch;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Novel {
	
	private static final String HOST_NAME = "http://ck101.com/";
	private static final 
		String REQUEST_USER_AGENT_HEADER = "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0";
	
	private int maxPage;
	private String[][] data;
	private int novelNumber;
	private int sectionNumber[];
	private Thread tg[];
	
	public Novel(int novelNumber) throws IOException, InterruptedException {
		this.novelNumber = novelNumber;
		maxPage = -1;
		tg = new Thread[getMaxPage()];
		loadAll();
		joinAll();
	}
	
	public void loadAll() throws IOException {
		for(int i = 2 ; i <= getMaxPage() ; i++) {
			loadByPage(i);
		}
		
	}
	
	public void joinAll() throws InterruptedException {
		for(int i = 1 ; i < getMaxPage() ; i++) {
			tg[i].join();
		}
	}
	
	public int getMaxPage() {
		Connection conn;
		Document doc = null;
		Elements element = null;
		if(maxPage < 0) {
			while(maxPage < 0) {
				try {
					conn = Jsoup.connect(getUrlByPage(1));
					conn.header("User-Agent", REQUEST_USER_AGENT_HEADER);
			        doc = conn.get();
			        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
			        element = doc.select("a.last");
			        String x[] = element.get(0).text().split(" ");
			        maxPage = Integer.parseInt(x[1]);
				} catch (IOException e) {
					maxPage = -1;
				}
			}
			
			doc.select("i.pstatus").remove();
			doc.select("br").append("\\n");
		    doc.select("p").prepend("\\n\\n");
		    
			element = doc.select("td.t_f");
			sectionNumber = new int[maxPage];
	        sectionNumber[0] = element.size();
	        data = new String[maxPage][];
	        data[0] = new String[sectionNumber[0]];
	        for(int i = 0 ; i < sectionNumber[0] ; i++){
	        	data[0][i] = element.get(i).text().replaceAll("\\\\n", "\n").replace("\u00a0", "");
	        }
		}    
		return maxPage;
	}
	
	public void loadByPage(int pageNumber) {
		int arrayNumber = pageNumber-1;
		tg[arrayNumber] = new Load(pageNumber);
		tg[arrayNumber].start();
	}
	
	public String getUrlByPage(int pageNumber) throws IOException {
		return HOST_NAME + "thread-" + novelNumber + "-" + pageNumber + "-1.html";
	}
	
	public int getNovelNumber() {
		return novelNumber;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Novel n = new Novel(3378932);
		FileOutputStream out = new FileOutputStream("out.txt");
		for(int i = 0 ; i < n.sectionNumber[0]; i++) {
			out.write(n.data[0][i].getBytes("UTF-8"));
		}
	}
	
	public class Loader extends Thread {

		private int pageNumber;
		
		public Load(int pageNumber) {
			this.pageNumber = pageNumber;
		}
		
		@Override
		public void run() {
			if(getMaxPage() >= pageNumber) {
				int arrayNumber = pageNumber-1;
				while(sectionNumber[arrayNumber] <= 0) {
					try {
						Connection conn = Jsoup.connect(getUrlByPage(pageNumber));
				        conn.header("User-Agent", REQUEST_USER_AGENT_HEADER);
				        Document doc = conn.get();
				        doc.select("br").append("\\n");
				        doc.select("i.pstatus").remove();
				        Elements content = doc.select("td.t_f");
				        sectionNumber[arrayNumber] = content.size();
				        data[arrayNumber] = new String[sectionNumber[arrayNumber]];
				        for(int i = 0 ; i < sectionNumber[arrayNumber] ; i++) {
				        	data[arrayNumber][i] = content.get(i).text();
				        }
					} catch (IOException e) {
						sectionNumber[arrayNumber] = -1;
					}
				}
			} else {
				System.out.printf("pageNumber error");
			}
		}
		
	}
	
}
