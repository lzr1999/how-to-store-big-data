package filesynchronizer;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;


public class FileSynchronizer extends FileAlterationListenerAdaptor{
	private final long maxPartSize = 5<<20;
	private final int maxLoop = 3;
	private String bucketName;
	private BasicAWSCredentials credentials;
	private ClientConfiguration ccfg;
	private EndpointConfiguration endpoint;
	private AmazonS3 s3;
	
	public FileSynchronizer(String bucketName, String accessKey, String secretKey,
			String serviceEndpoint, String signingRegion) {
		this.bucketName = bucketName;
		this.credentials = new BasicAWSCredentials(accessKey, secretKey);
		this.ccfg = new ClientConfiguration().withUseExpectContinue(false);
		this.endpoint = new EndpointConfiguration(serviceEndpoint, signingRegion);
		this.s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withClientConfiguration(ccfg)
				.withEndpointConfiguration(endpoint).withPathStyleAccessEnabled(true).build();
	}
	
	
	public long getMaxSize() {
		return this.maxPartSize;
	}
	
	
	public void multipartUpload(File file,String uploadId,long filePosition, 
			int partNum, ArrayList<PartETag> partETags) {
		// 分块传输函数
		long partSize = this.maxPartSize;
		String keyName = "";
		for(int k=0;k<this.maxLoop;k++) {
			try {
				// Step0: get the file name
				keyName = Paths.get(file.getAbsolutePath()).getFileName().toString();
				if((partNum-1)*this.maxPartSize < filePosition) {
					partNum ++ ;
				}
				long contentLength = file.length();
			
				// Step 1: Initialize.
				if(uploadId == "") {
					InitiateMultipartUploadRequest initRequest = 
							new InitiateMultipartUploadRequest(bucketName, keyName);
					uploadId = s3.initiateMultipartUpload(initRequest).getUploadId();
				}
				System.out.format("Created upload ID was %s\n", uploadId);
				if(partETags == null) {
					partETags = new ArrayList<PartETag>();
				}
	
				// Step 2: Upload parts.
				for (int i = partNum; filePosition < contentLength;i++) {
					System.out.println(String.format("%s\t%s",filePosition, contentLength));
					// Last part can be less than 5 MB. Adjust part size.
					partSize = Math.min(this.maxPartSize, contentLength - filePosition);
	
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
					partNum++;
//					saveUploadInfo(file.getAbsolutePath(),uploadId, filePosition, partNum, partETags);
					InfoSaver.saveInfo(file.getAbsolutePath(), uploadId, filePosition, partNum, partETags);
				}
	
				// Step 3: Complete.
				System.out.println("Completing upload");
				CompleteMultipartUploadRequest compRequest = 
						new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);
				s3.completeMultipartUpload(compRequest);
//				deleteUploadInfo(uploadId);
				InfoSaver.deleteInfo(uploadId);
				break;
			}catch (IllegalArgumentException e) {
				System.err.println(e.toString());
				if(e.toString().contains("Failed to open file")) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}catch (Exception e) {
				System.err.println(e.toString());
				if (uploadId != null && !uploadId.isEmpty()) {
					// Cancel when error occurred
					System.out.println("Aborting upload");
					s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, keyName, uploadId));
				}
//				System.exit(1);
			}
		}
		System.out.println("Done!");
	}
	
	
	public void simpleUpload(File file) {
		// 简单传输，不用分块传输
		for(int k=0;k<this.maxLoop;k++) {
			try {
				String keyName = Paths.get(file.getAbsolutePath()).getFileName().toString();
				s3.putObject(bucketName,keyName,file);
				return;
			}catch (Exception e) {
				System.err.println(e.toString());
				System.out.println("[Info]:retreive again");
			}
		}
		System.out.println("[Info]:sorry network interuption may occur, a simple restart may help!");
	}
	
	public void deleteFile(File file) {
		String keyName = Paths.get(file.getAbsolutePath()).getFileName().toString();
		 try {
	            s3.deleteObject(new DeleteObjectRequest(bucketName, keyName));
	        } catch (AmazonServiceException e) {
	            // The call was transmitted successfully, but Amazon S3 couldn't process 
	            // it, so it returned an error response.
	            e.printStackTrace();
	        } catch (SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	            e.printStackTrace();
	        }
	    }
		
}

