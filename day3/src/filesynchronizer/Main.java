package filesynchronizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.model.PartETag;

public class Main {
	private final static String bucketName = "liziran";
	private final static String accessKey = "C43D4E1AC76D0CF9EE06";
	private final static String secretKey = "WzJFNEM3NTIwMkZBQjk2MzA3NDdERjM4Mzc1MUZFRjgxQjU0NzAzMERd";
	private final static String serviceEndpoint = "http://scuts3.depts.bingosoft.net:29999";
	private final static String signingRegion = "";

	public static void main(String[] args) {
//		System.out.print("please input the target rootDir:");
//		Scanner scanner = new Scanner(System.in);
//		// 指定同步目录
//		String rootDir = scanner.nextLine();
//		scanner.close();
		String rootDir = "D:\\cpp";
		FileSynchronizer synchronizer = new FileSynchronizer(bucketName, 
				accessKey, secretKey, serviceEndpoint, signingRegion);
		FileListener listener = new FileListener(synchronizer);
		checkUnfinished(synchronizer);
		// 轮询间隔
		long interval = TimeUnit.SECONDS.toMillis(1);
		// 创建观察者，订阅listener主题
		FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));
		observer.addListener(listener);
		// 创建管理器
		FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
		try {
			monitor.start();
			System.out.println("file listener is working:");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	private static void checkUnfinished(FileSynchronizer synchronizer) {
		// 检查是否有中断的任务，jsonPath是中断信息保存位置
		String jsonPath = System.getProperty("user.dir")+"\\foo.json";
		// 读取中断信息
		try {
			FileReader fis = new FileReader(new File(jsonPath));
			BufferedReader reader = new BufferedReader(fis);
			StringBuffer buffer = new StringBuffer();
			String s = null;
			while((s=reader.readLine())!=null) {
				buffer.append(s);
			}
			if(buffer.length()>1)
				InfoSaver.myjson = JSON.parseObject(buffer.toString());
			fis.close();
		} catch (FileNotFoundException e) {
			System.err.println("json file not found");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String uploadId = "";
		String filePath = "";
		long filePosition = 0;
		int partNum = 1;
		ArrayList<PartETag> partETags;
		// 恢复传输任务
		if(!InfoSaver.myjson.isEmpty()) {
		for(String key:InfoSaver.myjson.keySet()) {
			JSONObject unfinishedEntry = InfoSaver.myjson.getJSONObject(key);
			uploadId = key;
			filePath = unfinishedEntry.getString("filePath");
			filePosition = unfinishedEntry.getLong("filePosition");
			partNum = unfinishedEntry.getIntValue("partNum");
			partETags = (ArrayList<PartETag>) JSONArray.parseArray(
						unfinishedEntry.getString("partETags"), PartETag.class);
			synchronizer.multipartUpload(new File(filePath), 
					uploadId, filePosition, partNum, partETags);
		}
		}
	}
}
