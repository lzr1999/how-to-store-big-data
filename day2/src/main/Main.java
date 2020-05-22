package main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


public class Main {

	private final static String bucketName = "liziran";
	private final static String accessKey = "C43D4E1AC76D0CF9EE06";
	private final static String secretKey = "WzJFNEM3NTIwMkZBQjk2MzA3NDdERjM4Mzc1MUZFRjgxQjU0NzAzMERd";
	private final static String serviceEndpoint = "http://scuts3.depts.bingosoft.net:29999";
	private final static String signingRegion = "";

	private static final String SUFFIX = "/";

	public static void main(String[] args) {

		final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		final ClientConfiguration ccfg = new ClientConfiguration().withUseExpectContinue(false);

		final EndpointConfiguration endpoint = new EndpointConfiguration(serviceEndpoint, signingRegion);

		final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withClientConfiguration(ccfg)
				.withEndpointConfiguration(endpoint).withPathStyleAccessEnabled(true).build();

		for (Bucket bucket : s3.listBuckets()) {
			System.out.println(" - " + bucket.getName());
		}

		// 在bucket中创建目录
		String folderName = "file";
		createFolder(bucketName, folderName, s3);

		// 在新建的folder中上传文件
		String fileName = folderName + SUFFIX + "javaHello.txt";
		s3.putObject(new PutObjectRequest(bucketName, fileName, new File("file/javaHello.txt"))
				.withCannedAcl(CannedAccessControlList.PublicRead));
		s3.deleteObject(bucketName, fileName);
		String folderName1 = "travel";
		deleteFolder(bucketName, folderName1, s3);

	}

	public static void createFolder(String bucketName, String folderName, AmazonS3 client) {

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		// 创建空白内容
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + SUFFIX, emptyContent,
				metadata);

		client.putObject(putObjectRequest);
	}


	public static void deleteFolder(String bucketName, String folderName, AmazonS3 client) {
		List<S3ObjectSummary> fileList = client.listObjects(bucketName, folderName).getObjectSummaries();
		for (S3ObjectSummary file : fileList) {
			client.deleteObject(bucketName, file.getKey());
		}
		client.deleteObject(bucketName, folderName);
	}
}
