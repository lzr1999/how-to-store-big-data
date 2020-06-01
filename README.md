# how-to-store-big-data
## 2班李梓然<br>
### 5月27号实操：<br>
1.今天完成的任务<br>
1)了解了什么是外部表:<br>
外部表只能在Oracle 9i之后来使用。简单地说，外部表，是指不存在于数据库中的表。通过向Oracle提供描述外部表的元数据，我们可以把一个操作系统文件当成一个只读的数据库表，就像这些数据存储在一个普通数据库表中一样来进行访问。外部表是对数据库表的延伸。<br>
2)了解了外部表与内部表区别<br>
未被external修饰的是内部表（managed table），被external修饰的为外部表（external table）；
区别：
- 内部表数据由Hive自身管理，外部表数据由HDFS管理；
- 内部表数据存储的位置是hive.metastore.warehouse.dir（默认：/user/hive/warehouse），外部表数据的存储位置由自己制定（如果没有LOCATION，Hive将在HDFS上的/user/hive/warehouse文件夹下以外部表的表名创建一个文件夹，并将属于这个表的数据存放在这里）；
- 删除内部表会直接删除元数据（metadata）及存储数据；删除外部表仅仅会删除元数据，HDFS上的文件并不会被删除；
- 对内部表的修改会将修改直接同步给元数据，而对外部表的表结构和分区进行修改，则需要修复（MSCK REPAIR TABLE table_name;）
3)学会了怎么创建外部表：<br>
```java  
  
create external table if not exists t_rk_jbxx_result1(word string comment '分词',freq int comment '次数')comment '' row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile location 's3n://liziran/t_rk_jbxx_result/';
    
```
### 5月26号实操：<br>
1.今天完成的任务<br>
1)理解了RDD是什么:<br>
- RDD(Resilient Distributed Datasets,弹性分布式数据集)，是Spark最为核心的概念。
- RDD的特点：
   - 是一个分区的只读记录的集合；
   - 一个具有容错机制的特殊集；
   - 只能通过在稳定的存储器或其他RDD上的确定性操作（转换）来创建；
   - 可以分布在集群的节点上，以函数式操作集合的方式，进行各种并行操作
- RDD之所以为“弹性”的特点
   - 基于Lineage的高效容错（第n个节点出错，会从第n-1个节点恢复，血统容错）；
   - Task如果失败会自动进行特定次数的重试（默认4次）；
   - Stage如果失败会自动进行特定次数的重试（可以值运行计算失败的阶段），只计算失败的数据分片；
   - 数据调度弹性：DAG TASK 和资源管理无关；
   - checkpoint；
   - 自动的进行内存和磁盘数据存储的切换；
 2）学习了RDD的操作<br>
 ```java  
  
1.数据集合

    val data = Array(1, 2,3, 4, 5, 6, 7, 8, 9)
    val distData = sc.parallelize(data, 3)

2.外部数据源

    val distFile1 = sc.textFile("data.txt") //本地当前目录下文件
    val distFile2=sc.textFile("hdfs://192.168.1.100:9000/input/data.txt") //HDFS文件
    val distFile3 =sc.textFile("file:/input/data.txt") //本地指定目录下文件
    val distFile4 =sc.textFile("/input/data.txt") //本地指定目录下文件
    textFile("/input/001.txt, /input/002.txt ") //读取多个文件
    textFile("/input") //读取目录
    textFile("/input /*.txt") //含通配符的路径
    textFile("/input /*.gz") //读取压缩文件
  

```
```java  
  
1.rdd算子作用：

    1）输入：在Spark程序运行中，数据从外部数据空间（如分布式存储：textFile读取HDFS等，parallelize方法输入Scala集合或数据）输入Spark，数据进入Spark运行时数据空间，转化为Spark中的数据块，通过BlockManager进行管理。
    2）运行：在Spark数据输入形成RDD后便可以通过变换算子，如fliter等，对数据进行操作并将RDD转化为新的RDD，通过Action算子，触发Spark提交作业。如果数据需要复用，可以通过Cache算子，将数据缓存到内存。
    3）输出：程序运行结束数据会输出Spark运行时空间，存储到分布式存储中（如saveAsTextFile输出到HDFS），或Scala数据或集合中（collect输出到Scala集合，count返回Scala int型数据）。

2.rdd算子分类：

    1）Value数据类型的Transformation算子，这种变换并不触发提交作业，针对处理的数据项是Value型的数据。
    2）Key-Value数据类型的Transfromation算子，这种变换并不触发提交作业，针对处理的数据项是Key-Value型的数据对。
    3）Action算子，这类算子会触发SparkContext提交Job作业。

    
```
```java  
  
1.map

    map是对RDD中的每个元素都执行一个指定的函数来产生一个新的RDD；RDD之间的元素是一对一关系；
    val rdd1 = sc.parallelize(1 to 9, 3)
    val rdd2 = rdd1.map(x => x*2)
    rdd2.collect
    res3: Array[Int] = Array(2, 4, 6, 8, 10, 12, 14, 16, 18)

2.filter

    Filter是对RDD元素进行过滤；返回一个新的数据集，是经过func函数后返回值为true的原元素组成；
    val rdd3 = rdd2. filter (x => x> 10)
    rdd3.collect
    res4: Array[Int] = Array(12, 14, 16, 18)

3.flatMap

    flatMap类似于map，但是每一个输入元素，会被映射为0到多个输出元素（因此，func函数的返回值是一个Seq，而不是单一元素），RDD之间的元素是一对多关系；
    val rdd4 = rdd3. flatMap (x => x to 20)
    res5: Array[Int] = Array(12, 13, 14, 15, 16, 17, 18, 19, 20, 14, 15, 16, 17, 18, 19, 20, 16, 17, 18, 19, 20, 18, 19, 20)

4.mapPartitions

    mapPartitions是map的一个变种。map的输入函数是应用于RDD中每个元素，而mapPartitions的输入函数是每个分区的数据，也就是把每个分区中的内容作为整体来处理的。

5.mapPartitionsWithIndex

    mapPartitionsWithSplit与mapPartitions的功能类似， 只是多传入split index而已，所有func 函数必需是 (Int, Iterator<T>) =>Iterator<U> 类型。

6.sample

    sample(withReplacement,fraction,seed)是根据给定的随机种子seed，随机抽样出数量为frac的数据。withReplacement：是否放回样；fraction：比例，0.1表示10% ；
    val a = sc.parallelize(1 to 10000, 3)
    a.sample(false, 0.1, 0).count
    res24: Long = 960

7.union

    union(otherDataset)是数据合并，返回一个新的数据集，由原数据集和otherDataset联合而成。
    val rdd8 = rdd1.union(rdd3)
    rdd8.collect
    res14: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 14, 16, 18)

8.intersection

    intersection(otherDataset)是数据交集，返回一个新的数据集，包含两个数据集的交集数据；
    val rdd9 = rdd8.intersection(rdd1)
    rdd9.collect
    res16: Array[Int] = Array(6, 1, 7, 8, 2, 3, 9, 4, 5)

9.distinct

    distinct([numTasks]))是数据去重，返回一个数据集，是对两个数据集去除重复数据，numTasks参数是设置任务并行数量。
    val rdd10 = rdd8.union(rdd9).distinct
    rdd10.collect
    res19: Array[Int] = Array(12, 1, 14, 2, 3, 4, 16, 5, 6, 18, 7, 8, 9)

10.groupByKey

    groupByKey([numTasks])是数据分组操作，在一个由（K,V）对组成的数据集上调用，返回一个（K,Seq[V])对的数据集。
    val rdd0 = sc.parallelize(Array((1,1), (1,2) , (1,3) , (2,1) , (2,2) , (2,3)), 3)
    val rdd11 = rdd0.groupByKey()
    rdd11.collect
    res33: Array[(Int, Iterable[Int])] = Array((1,ArrayBuffer(1, 2, 3)), (2,ArrayBuffer(1, 2, 3)))

11.reduceByKey

    reduceByKey(func, [numTasks])是数据分组聚合操作，在一个（K,V)对的数据集上使用，返回一个（K,V）对的数据集，key相同的值，都被使用指定的reduce函数聚合到一起。
    val rdd12 = rdd0.reduceByKey((x,y) => x + y)
    rdd12.collect
    res34: Array[(Int, Int)] = Array((1,6), (2,6))

12.aggregateByKey

    aggreateByKey(zeroValue: U)(seqOp: (U, T)=> U, combOp: (U, U) =>U) 和reduceByKey的不同在于，reduceByKey输入输出都是(K,
    V)，而aggreateByKey输出是(K,U)，可以不同于输入(K, V) ，aggreateByKey的三个参数：
    zeroValue: U，初始值，比如空列表{} ；
    seqOp: (U,T)=> U，seq操作符，描述如何将T合并入U，比如如何将item合并到列表 ；
    combOp: (U,U) =>U，comb操作符，描述如果合并两个U，比如合并两个列表 ；
    所以aggreateByKey可以看成更高抽象的，更灵活的reduce或group 。
    val z = sc.parallelize(List(1,2,3,4,5,6), 2)
    z.aggreate(0)(math.max(_, _), _ + _)
    res40: Int = 9
    val z = sc.parallelize(List((1, 3), (1, 2), (1, 4), (2, 3)))
    z.aggregateByKey(0)(math.max(_, _), _ + _)
    res2: Array[(Int, Int)] = Array((2,3), (1,9))

13.combineByKey

    combineByKey是对RDD中的数据集按照Key进行聚合操作。聚合操作的逻辑是通过自定义函数提供给combineByKey。
    combineByKey[C](createCombiner: (V) ⇒ C, mergeValue: (C, V) ⇒ C, mergeCombiners: (C, C)
    ⇒ C, numPartitions: Int):RDD[(K, C)]把(K,V) 类型的RDD转换为(K,C)类型的RDD，C和V可以不一样。combineByKey三个参数：
    val data = Array((1, 1.0), (1, 2.0), (1, 3.0), (2, 4.0), (2, 5.0), (2, 6.0))
    val rdd = sc.parallelize(data, 2)
    val combine1 = rdd.combineByKey(createCombiner = (v:Double) => (v:Double, 1),
    mergeValue = (c:(Double, Int), v:Double) => (c._1 + v, c._2 + 1),
    mergeCombiners = (c1:(Double, Int), c2:(Double, Int)) => (c1._1 + c2._1, c1._2 + c2._2),numPartitions = 2 )combine1.collect
    res0: Array[(Int, (Double, Int))] = Array((2,(15.0,3)), (1,(6.0,3)))

14.sortByKey

    sortByKey([ascending],[numTasks])是排序操作，对(K,V)类型的数据按照K进行排序，其中K需要实现Ordered方法。
    val rdd14 = rdd0.sortByKey()
    rdd14.collect
    res36: Array[(Int, Int)] = Array((1,1), (1,2), (1,3), (2,1), (2,2), (2,3))

15.join

    join(otherDataset, [numTasks])是连接操作，将输入数据集(K,V)和另外一个数据集(K,W)进行Join， 得到(K, (V,W))；该操作是对于相同K的V和W集合进行笛卡尔积 操作，也即V和W的所有组合；
    val rdd15 = rdd0.join(rdd0)
    rdd15.collect
    res37: Array[(Int, (Int, Int))] = Array((1,(1,1)), (1,(1,2)), (1,(1,3)), (1,(2,1)), (1,(2,2)), (1,(2,3)), (1,(3,1)), (1,(3,2)), (1,(3,3)), (2,(1,1)),(2,(1,2)), (2,(1,3)), (2,(2,1)), (2,(2,2)), (2,(2,3)), (2,(3,1)), (2,(3,2)), (2,(3,3)))
    连接操作除join 外，还有左连接、右连接、全连接操作函数： leftOuterJoin、rightOuterJoin、fullOuterJoin。

16.cogroup

    cogroup(otherDataset, [numTasks])是将输入数据集(K, V)和另外一个数据集(K, W)进行cogroup，得到一个格式为(K, Seq[V], Seq[W])的数据集。
    val rdd16 = rdd0.cogroup(rdd0)
    rdd16.collect
    res38: Array[(Int, (Iterable[Int], Iterable[Int]))] = Array((1,(ArrayBuffer(1, 2, 3),ArrayBuffer(1, 2, 3))), (2,(ArrayBuffer(1, 2,3),ArrayBuffer(1, 2, 3))))

17.cartesian

    cartesian(otherDataset)是做笛卡尔积：对于数据集T和U 进行笛卡尔积操作， 得到(T, U)格式的数据集。
    val rdd17 = rdd1.cartesian(rdd3)
    rdd17.collect
    res39: Array[(Int, Int)] = Array((1,12), (2,12), (3,12), (1,14), (1,16), (1,18), (2,14), (2,16), (2,18), (3,14), (3,16), (3,18), (4,12), (5,12),(6,12), (4,14), (4,16), (4,18), (5,14), (5,16), (5,18), (6,14), (6,16), (6,18), (7,12), (8,12), (9,12), (7,14), (7,16), (7,18), (8,14), (8,16),(8,18), (9,14), (9,16), (9,18))

    
```
### 5月25号实操：<br>
1.今天完成的任务:<br>
1)实现本地文件修改同步到S3
```java  
  
public void onFileChange(File file) {
		System.out.println("[修改]:" + file.getAbsolutePath());
		if(file.length() > fileSynchronizer.getMaxSize())
			fileSynchronizer.multipartUpload(file, "", 0, 1, null);
		else {
			fileSynchronizer.simpleUpload(file);
		}
	}
    
```
2）实现本地文件删除同步到S3
```java  
  
public void onFileDelete(File file) {
		System.out.println("[删除]:" + file.getAbsolutePath());
		fileSynchronizer.deleteFile(file);
	}
public void deleteFile(File file) {
		String keyName = Paths.get(file.getAbsolutePath()).getFileName().toString();
		 try {
	            s3.deleteObject(new DeleteObjectRequest(bucketName, keyName));
	        } catch (AmazonServiceException e) {
	            e.printStackTrace();
	        } catch (SdkClientException e) {
	            e.printStackTrace();
	        }
	    }
    
```
### 5月22号实操:<br>
1.今天完成的任务:<br>
1)获取s3 Bucket的list。
```java  
  
for (Bucket bucket : s3.listBuckets()) {
			System.out.println(" - " + bucket.getName());
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
