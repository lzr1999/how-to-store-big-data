# how-to-store-big-data
## 2班李梓然<br>
### 5月22号实操:<br>
1.今天完成的任务:<br>
1)获取s3 Bucket的list。
```java  
  
for (Bucket bucket : s3.listBuckets()) {
			System.out.println(" - " + bucket.getName());
		}
    
```
2)实现了将本地文件夹上传至S3。<br>
```java  
  
public static void createFolder(String bucketName, String folderName, AmazonS3 client) {

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		// 创建空白内容
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + SUFFIX, emptyContent,
				metadata);

		client.putObject(putObjectRequest);
	}
  
```
3）实现了S3 bucket中文件的删除和目录的删除。<br>
```java  
  public static void deleteFolder(String bucketName, String folderName, AmazonS3 client) {
		List<S3ObjectSummary> fileList = client.listObjects(bucketName, folderName).getObjectSummaries();
		for (S3ObjectSummary file : fileList) {
			client.deleteObject(bucketName, file.getKey());
		}
		client.deleteObject(bucketName, folderName);
	}
  
```
2.遇到的问题以及如何解决：<br>
在网上找了很久怎么直接把本地的整个目录以及目录里的文件上传到S3 bucket的方法，没有找到。最后采用了在S3 bucket创建同名目录在上传文件的做法。<br>

### 5月21号实操：<br>
1.今天完成的任务：<br>
1）使用java监听文件目录的变化<br>
2）java程序如何生成可执行文件<br>
2.遇到的问题以及如何解决：<br>
1）选择什么方法监听文件目录：上网查阅了很多博客，最初用common-io这个工具库尝试实现没有成功，最终用jnotify成功实现指定路径监听文件变化。这只是其中的两个方法，之后会尝试其他方法使用并选择性能最优的方法。<br>
2）生成可执行文件遇到的问题：首先要生成可执行的jar包，由于最初export错误生成jar包而不是可执行jar包，导致失败，后来发现原因后用exe4j软件犯了很多错误，比如search sequence没设置jre路径导致最后失败等，最终成功生成了能运行的exe文件。
