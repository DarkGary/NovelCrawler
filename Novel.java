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
	private int maxPage;//小說最大頁數
	private String[][] data;//小說內容[頁數][章節]
	private int novelNumber;//小說編號
	private int sectionNumber[];//index = pagenumber
	private Thread tg[];
	public Novel(int novelNumber) throws IOException, InterruptedException{//傳入小說編號
		this.novelNumber = novelNumber;
		maxPage = -1;
		tg = new Thread[getMaxPage()];
		loadAll();
		joinAll();
	}
	public void loadAll() throws IOException{
		for(int i = 2 ; i <= getMaxPage() ; i++){
			loadByPage(i);
		}
		
	}
	public void joinAll() throws InterruptedException{
		for(int i = 1 ; i < getMaxPage() ; i++){
			tg[i].join();
		}
	}
	public int getMaxPage(){
		Connection conn;
		Document doc = null;
		Elements element = null;
		if(maxPage < 0){
			while(maxPage < 0){
				try {
					conn = Jsoup.connect(getUrlByPage(1));
					conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
			        doc = conn.get();
			        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
			        element = doc.select("a.last");
			        String x[] = element.get(0).text().split(" ");
			        maxPage = Integer.parseInt(x[1]);
			        
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
	public void loadByPage(int pageNumber){
		int arrayNumber = pageNumber-1;
		tg[arrayNumber] = new Load(pageNumber);
		tg[arrayNumber].start();
	}
	public String getUrlByPage(int pageNumber) throws IOException{
		return "http://ck101.com/thread-" + novelNumber + "-"+ pageNumber +"-1.html";
	}
	public int getNovelNumber(){
		return novelNumber;
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException{
		long begin = System.currentTimeMillis();
		Novel n = new Novel(3378932);
//		System.out.println("Novel Number : " + n.getNovelNumber());
//		System.out.println("Max page : " + n.getMaxPage());
//		System.out.println("SectionNumber : " + n.sectionNumber[0]);
		
		long over = System.currentTimeMillis();
		FileOutputStream out = new FileOutputStream("out.txt");
		for(int i = 0 ; i < n.sectionNumber[0]; i++){
			out.write(n.data[0][i].getBytes("UTF-8"));
		}
		System.out.println("使用的時間為： "
	            + (over - begin) + " 毫秒 " );

	}
	
	public class Load extends Thread{

		int pageNumber;
		public Load(int pageNumber){
			this.pageNumber = pageNumber;
		}
		@Override
		public void run() {
			if(getMaxPage() >= pageNumber){
				
				int arrayNumber = pageNumber-1;
				while(sectionNumber[arrayNumber] <= 0){
					try{
						
						Connection conn = Jsoup.connect(getUrlByPage(pageNumber));
				        conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
				        Document doc = conn.get();
				        doc.select("br").append("\\n");
				        doc.select("i.pstatus").remove();
				        Elements content = doc.select("td.t_f");
				        sectionNumber[arrayNumber] = content.size();
				        data[arrayNumber] = new String[sectionNumber[arrayNumber]];
				        for(int i = 0 ; i < sectionNumber[arrayNumber] ; i++){
				        	data[arrayNumber][i] = content.get(i).text();
				        }
					}catch(IOException e){
						sectionNumber[arrayNumber] = -1;
					}
					//System.out.println(""+pageNumber + "頁的章節數量為:" + sectionNumber[arrayNumber]);
				}
			}else{
				System.out.printf("pageNumber error");
			}
		}
		
	}
	
}
