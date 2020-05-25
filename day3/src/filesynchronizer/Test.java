package filesynchronizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

import jdk.internal.org.objectweb.asm.Handle;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import com.alibaba.fastjson.JSONArray;


public class Test {
	private final static String bucketName = "liziran";
	private final static String accessKey = "C43D4E1AC76D0CF9EE06";
	private final static String secretKey = "WzJFNEM3NTIwMkZBQjk2MzA3NDdERjM4Mzc1MUZFRjgxQjU0NzAzMERd";
	private final static String serviceEndpoint = "http://scuts3.depts.bingosoft.net:29999";
	private static long partSize = 5 << 20;
	private final static String signingRegion = "";
//	public static long filePosition = 0;
//	public static int i = 1;
//	public static String uploadId="";
//	private boolean isFirstTime = true;
//	private static ArrayList<PartETag> partETags;
	private static Document document;
//	public Test() {
//		saveRunningInfo();
//	}
	public static void main(String[] args) {
		String xmlPath = System.getProperty("user.dir")+"\\foo.xml";
		System.out.println(xmlPath);
		File file = new File(xmlPath);
		try {
			if(!file.exists() || file.length() < 1) {
				file.createNewFile();
				document = DocumentHelper.createDocument();
				document.addElement("root");
			}
			else {
				SAXReader reader = new SAXReader();
				document = reader.read(file);
			}	
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		FileSynchronizer synchronizer = new FileSynchronizer(bucketName,
				accessKey, secretKey, serviceEndpoint, signingRegion);
		synchronizer.multipartUpload(new File("D:\\cpp\\新建文本文档.txt"), "", 0, 1, null);
//		Element root = document.getRootElement();
//		long filePosition = 0;
//		int partNum = 1;
//		ArrayList<PartETag> partETags = null;
//		String uploadId = "";
//		if(root.hasContent()) {
//			for(Element task:root.elements())
//			{
//				uploadId = task.elementText("uploadId");
//				filePosition = Long.valueOf(task.elementText("filePosition").toString());
//				partNum  = Integer.valueOf(task.elementText("partNum").toString());
//				if(task.elementText("partETags") != "null" && uploadId != "")
//					partETags = (ArrayList<PartETag>) JSONArray.parseArray(task.elementText("partETags"), PartETag.class);
//				else {
//					partETags = new ArrayList<PartETag>();
//				}
//				synchronizer.multipartUpload(new File("D:\\download\\mingw-w64-v7.0.0.zip"), 
//						uploadId, filePosition, partNum, partETags);
//			}
//		}
		
//		Test test = new Test();
//		saveInfo();
//		SignalHandler handler =new SignalHandler() {
//			@Override
//			public void handle(Signal arg0) {
//				saveInfo();
//			}
//		};
//		Signal.handle(new Signal("TERM"), handler);
		
//		SAXReader reader = new SAXReader();
//		try {
//			Document document = reader.read("D:\\foo.xml");
//			Element root = document.getRootElement();
//			i = Integer.valueOf(root.elementText("i"));
//			uploadId = root.elementText("uploadId");
//			filePosition = Long.valueOf(root.elementText("filePosition"));
//			if(root.elementText("partETags") != "null" && uploadId != "")
//				partETags = (ArrayList<PartETag>) JSONArray.parseArray(root.elementText("partETags"), PartETag.class);
//			else {
//				partETags = new ArrayList<PartETag>();
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			System.out.println(e.toString());
//		}
//		
//		File file = new File("D:\\download\\mingw-w64-v7.0.0.zip");
//		multipartUpload(file);
		
	}
	/*
	public static void multipartUpload(File file) {
		System.out.println("enter multipart upload mudule...");
		final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
		final ClientConfiguration ccfg = new ClientConfiguration()
				.withUseExpectContinue(true);

		final EndpointConfiguration endpoint = 
						new EndpointConfiguration(serviceEndpoint, signingRegion);

		final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(ccfg)
                .withEndpointConfiguration(endpoint)
                .withPathStyleAccessEnabled(true)
                .build();

		String keyName = Paths.get(file.getAbsolutePath()).getFileName().toString();
		// Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
		long contentLength = file.length();
		try {
			// Step 1: Initialize.
			InitiateMultipartUploadRequest initRequest = 
					new InitiateMultipartUploadRequest(bucketName, keyName);
			if(uploadId == "") { 
				uploadId = s3.initiateMultipartUpload(initRequest).getUploadId();
			}
			System.out.format("Created upload ID was %s\n", uploadId);
			
			// Step 2: Upload parts.
			for (int j = i; filePosition < contentLength; i++, j++) {
				// Last part can be less than 5 MB. Adjust part size.
				partSize = Math.min(partSize, contentLength - filePosition);

				// Create request to upload a part.
				UploadPartRequest uploadRequest = new UploadPartRequest()
						.withBucketName(bucketName)
						.withKey(keyName)
						.withUploadId(uploadId)
						.withPartNumber(i)
						.withFileOffset(filePosition)
						.withFile(file)
						.withPartSize(partSize);

				// Upload part and add response to our list.
				System.out.format("Uploading part %d\n", i);
				partETags.add(s3.uploadPart(uploadRequest).getPartETag());

				filePosition += partSize;
				saveInfo();
				System.out.println("now file position is " + filePosition);
				TimeUnit.SECONDS.sleep(3);//秒
				System.out.println("continue to upload next part");
			}

			// Step 3: Complete.
			System.out.println("Completing upload");
			CompleteMultipartUploadRequest compRequest = 
					new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);

			s3.completeMultipartUpload(compRequest);
		} catch (Exception e) {
			System.err.println(e.toString());
			if (uploadId != null && !uploadId.isEmpty()) {
				// Cancel when error occurred
				System.out.println("Aborting upload");
				s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, keyName, uploadId));
			}
			System.exit(1);
		}
		System.out.println("Done!");
	}
	
	
	private static void saveInfo() {
		try {
//			FileWriter fw = new FileWriter("D:\\t.log");
//			System.out.println("Im going to end");
//			fw.write("the application ended! " + (new Date()).toString()+ a);
//			fw.close();
			System.out.println("[INFO]:saving xml to disk");
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("root");
			root.addElement("filePosition")
					.addText(String.valueOf(Test.filePosition));
			root.addElement("i")
					.addText(String.valueOf(Test.i));
			root.addElement("uploadId")
					.addText(Test.uploadId);
			root.addElement("partETags").addText(JSON.toJSONString(partETags));
			FileWriter out = new FileWriter("D:\\foo.xml");
			document.write(out);
			out.close();
			
		} catch (Exception e) {
			System.out.println(e.toString());
			// TODO: handle exception
		}
	}
	private void saveRunningInfo() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
//					FileWriter fw = new FileWriter("D:\\t.log");
//					System.out.println("Im going to end");
//					fw.write("the application ended! " + (new Date()).toString()+ a);
//					fw.close();
					System.out.println("[INFO]:saving xml to disk");
					Document document = DocumentHelper.createDocument();
					Element root = document.addElement("root");
					root.addElement("filePosition")
							.addText(String.valueOf(Test.filePosition));
					root.addElement("i")
							.addText(String.valueOf(Test.i));
					root.addElement("uploadId")
							.addText(Test.uploadId);
					root.addElement("partETags").addText(JSON.toJSONString(partETags));
					FileWriter out = new FileWriter("D:\\foo.xml");
					document.write(out);
					out.close();
					
				} catch (Exception e) {
					System.out.println(e.toString());
					// TODO: handle exception
				}
			}
		});
	}
	*/
	/*
	 * private static void checkUnfinished2() {
		String xmlPath = System.getProperty("user.dir")+"\\foo.xml";
		File file = new File(xmlPath);
		try {
			if(!file.exists() || file.length() < 1) {
				file.createNewFile();
				document = DocumentHelper.createDocument();
				document.addElement("root");
			}
			else {
				SAXReader reader = new SAXReader();
				document = reader.read(file);
			}	
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		FileSynchronizer synchronizer = new FileSynchronizer(bucketName,
				accessKey, secretKey, serviceEndpoint, signingRegion);
		Element root = document.getRootElement();
		if(root.hasContent()) {
			long filePosition = 0;
			int partNum = 1;
			ArrayList<PartETag> partETags = null;
			String uploadId = "";
			String filePath = "";
			for(Element task:root.elements())
			{
				uploadId = task.elementText("uploadId");
				filePath = task.elementText("filePath");
				filePosition = Long.valueOf(task.elementText("filePosition").toString());
				partNum  = Integer.valueOf(task.elementText("partNum").toString());
				if(task.elementText("partETags") != "null" && uploadId != "")
					partETags = (ArrayList<PartETag>) JSONArray.parseArray(task.elementText("partETags"), PartETag.class);
				else {
					partETags = new ArrayList<PartETag>();
				}
				synchronizer.multipartUpload(new File(filePath), 
						uploadId, filePosition, partNum, partETags);
			}
		}
	}
	*/

}
