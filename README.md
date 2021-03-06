# how-to-store-big-data
## 2班李梓然<br>
### 6月19号实操：<br>
1.今天完成的任务:<br>
1.学习模型调参利器 gridSearchCV （网格搜索）<br>
参考博客https://blog.csdn.net/weixin_41988628/article/details/83098130<br>
2.使用RMSE作为模型评估方法，选用Lasso回归：<br>
```java  
# 模型训练
# 使用lasso算法
# 对模型调参
class grid():
    def __init__(self, model):
        self.model = model

    def grid_get(self, X, y, param_grid):
        grid_search = GridSearchCV(self.model, param_grid, cv=5, scoring="neg_mean_squared_error")
        grid_search.fit(X, y)
        print(grid_search.best_params_, np.sqrt(-grid_search.best_score_))
        grid_search.cv_results_['mean_test_score'] = np.sqrt(-grid_search.cv_results_['mean_test_score'])
        print(pd.DataFrame(grid_search.cv_results_)[['params', 'mean_test_score', 'std_test_score']])


grid(Lasso()).grid_get(X_scaled, y_log,
                       {'alpha': [0.0004, 0.0005, 0.0007, 0.0006, 0.0009, 0.0008], 'max_iter': [10000]})
# 选择模型
model = Lasso(alpha=0.0005, max_iter=10000)
name = "Lasso"
model.fit(X_scaled, y_log)
pred = np.exp(model.predict(test_X_scaled))
result = pd.DataFrame({'Id': test.Id, 'SalePrice': pred})
result.to_csv("output.csv", index=False)


# 模型评估
# 定义RMSE的交叉验证评估指标
def rmse_cv(model, X, y):
    rmse = np.sqrt(-cross_val_score(model, X, y, scoring="neg_mean_squared_error", cv=5))
    return rmse


score = rmse_cv(model, X_scaled, y_log)
print("{}: {:.6f}, {:.4f}".format(name, score.mean(), score.std()))
# 将训练集按7：3切割为训练集和验证集
x_train, x_test, y_train, y_test = train_test_split(X_scaled, y_log, test_size=0.3)
model.fit(x_train, y_train)
# 将预测值与目标值放入一张图进行比较
y_pre = model.predict(x_test)
plt.scatter(y_pre, y_test, marker='o')
plt.scatter(y_test, y_test)
plt.show()

```
### 6月18号实操：<br>
1.今天完成的任务:<br>
1.学习了特征工程的方法：
- 无量纲化：使不同规格的数据转换到同一规格。常用的无量纲化方法有标准化和区间缩放法。标准化的前提是特征值服从正态分布，标准化后，其转换成标准正态分布；区间缩放法利用了边界值信息，将特征的取值区间缩放到某个特点的范围，例如[0,1]等。
- 对定量特征二值化：定量特征的二值化的核心在于设定一个阈值，小于等于阈值的赋值为0，大于阈值的赋值为1
- 对定性特征哑编码
- 特征选择：通常来说，从两个方面考虑来选择特征，特征是否发散和特征与目标的相关性
- 降维：当特征选择完成后，可以直接训练模型了，但是可能由于特征矩阵过大，导致计算量大、训练时间长的问题，因此降低特征矩阵维度也是必不可少的。常见的降维方法除了以上提到的基于L1惩罚项的模型以外，另外还有主成分分析法（PCA）和线性判别分析（LDA），线性判别分析本身也是一个分类模型。<br>
2.详细了解了PCA：<br>
参考博客https://blog.csdn.net/u012421852/article/details/80458340?utm_medium=distribute.pc_relevant.none-task-blog-baidujs-1<br>
3.对数据集进行特征筛选和降维：<br>
```java  
# 特征筛选
# 用Lasso进行特征筛选，选出较重要的一些特征进行组合
lasso = Lasso(alpha=0.001)
lasso.fit(X_scaled, y_log)
FI_lasso = pd.DataFrame({"Feature Importance": lasso.coef_}, index=data_pipe.columns)
FI_lasso.sort_values("Feature Importance", ascending=False)
FI_lasso[FI_lasso["Feature Importance"] != 0].sort_values("Feature Importance").plot(kind="barh", figsize=(15, 25))
plt.xticks(rotation=90)
plt.show()


# 组合特征
class add_feature(BaseEstimator, TransformerMixin):
    def __init__(self, additional=1):
        self.additional = additional

    def fit(self, X, y=None):
        return self

    def transform(self, X):
        if self.additional == 1:
            X["TotalHouse"] = X["TotalBsmtSF"] + X["1stFlrSF"] + X["2ndFlrSF"]
            X["TotalArea"] = X["TotalBsmtSF"] + X["1stFlrSF"] + X["2ndFlrSF"] + X["GarageArea"]
        else:
            X["TotalHouse"] = X["TotalBsmtSF"] + X["1stFlrSF"] + X["2ndFlrSF"]
            X["TotalArea"] = X["TotalBsmtSF"] + X["1stFlrSF"] + X["2ndFlrSF"] + X["GarageArea"]
            X["+_TotalHouse_OverallQual"] = X["TotalHouse"] * X["OverallQual"]
            X["+_GrLivArea_OverallQual"] = X["GrLivArea"] * X["OverallQual"]
            X["+_oMSZoning_TotalHouse"] = X["oMSZoning"] * X["TotalHouse"]
            X["+_oMSZoning_OverallQual"] = X["oMSZoning"] + X["OverallQual"]
            X["+_oMSZoning_YearBuilt"] = X["oMSZoning"] + X["YearBuilt"]
            X["+_oNeighborhood_TotalHouse"] = X["oNeighborhood"] * X["TotalHouse"]
            X["+_oNeighborhood_OverallQual"] = X["oNeighborhood"] + X["OverallQual"]
            X["+_oNeighborhood_YearBuilt"] = X["oNeighborhood"] + X["YearBuilt"]
            X["+_BsmtFinSF1_OverallQual"] = X["BsmtFinSF1"] * X["OverallQual"]
            X["-_oFunctional_TotalHouse"] = X["oFunctional"] * X["TotalHouse"]
            X["-_oFunctional_OverallQual"] = X["oFunctional"] + X["OverallQual"]
            X["-_LotArea_OverallQual"] = X["LotArea"] * X["OverallQual"]
            X["-_TotalHouse_LotArea"] = X["TotalHouse"] + X["LotArea"]
            X["-_oCondition1_TotalHouse"] = X["oCondition1"] * X["TotalHouse"]
            X["-_oCondition1_OverallQual"] = X["oCondition1"] + X["OverallQual"]
            X["Bsmt"] = X["BsmtFinSF1"] + X["BsmtFinSF2"] + X["BsmtUnfSF"]
            X["Rooms"] = X["FullBath"] + X["TotRmsAbvGrd"]
            X["PorchArea"] = X["OpenPorchSF"] + X["EnclosedPorch"] + X["3SsnPorch"] + X["ScreenPorch"]
            X["TotalPlace"] = X["TotalBsmtSF"] + X["1stFlrSF"] + X["2ndFlrSF"] + X["GarageArea"] + X["OpenPorchSF"] + X[
                "EnclosedPorch"] + X["3SsnPorch"] + X["ScreenPorch"]
            return X


# 可以通过pipeline对不同特征组合进行尝试
pipe = Pipeline([
    ('labenc', labelenc()),
    ('add_feature', add_feature(additional=2)),
    ('skew_dummies', skew_dummies(skew=1)),
])
```
```java  
# PCA去相关性
full_pipe = pipe.fit_transform(full)
full_pipe.shape
n_train = train.shape[0]
X = full_pipe[:n_train]
test_X = full_pipe[n_train:]
y = train.SalePrice
X_scaled = scaler.fit(X).transform(X)
y_log = np.log(train.SalePrice)
test_X_scaled = scaler.transform(test_X)
pca = PCA(n_components=410)
X_scaled = pca.fit_transform(X_scaled)
test_X_scaled = pca.transform(test_X_scaled)
X_scaled.shape, test_X_scaled.shape
```

### 6月17号实操：<br>
1.今天完成的任务:<br>
- 下载了kaggle网站数据集，网址是https://www.kaggle.com/c/house-prices-advanced-regression-techniques/data
- 完成了数据预处理（数据清洗，数据变换）
```java  
# 数据预处理
train.drop(train[(train["GrLivArea"] > 4000) & (train["SalePrice"] < 300000)].index, inplace=True)
full = pd.concat([train, test], ignore_index=True)
full.drop(['Id'], axis=1, inplace=True)
full.shape
# print(full.shape)
```
```java  
# 数据清洗，查看缺失值数量
miss_val = full.isnull().sum()
miss_val[miss_val > 0].sort_values(ascending=False)
# 根据LotArea和Neighborhood的中值输入LotFronage的缺失值，由于LotArea是一个连续的特征，使用qCut将其划分为10个部分
# LotFrontage这个特征与LotAreaCut和Neighborhood有比较大的关系，所以这里用这两个特征分组后的中位数进行插补
full.groupby(['Neighborhood'])[['LotFrontage']].agg(['mean', 'median', 'count'])
full["LotAreaCut"] = pd.qcut(full.LotArea, 10)
full.groupby(['LotAreaCut'])[['LotFrontage']].agg(['mean', 'median', 'count'])
full['LotFrontage'] = full.groupby(['LotAreaCut', 'Neighborhood'])['LotFrontage'].transform(
    lambda x: x.fillna(x.median()))
full['LotFrontage'] = full.groupby(['LotAreaCut'])['LotFrontage'].transform(lambda x: x.fillna(x.median()))
# 表示面积的特征，用0填充缺失值
cols = ["MasVnrArea", "BsmtUnfSF", "TotalBsmtSF", "GarageCars", "BsmtFinSF2", "BsmtFinSF1", "GarageArea"]
for col in cols:
    full[col].fillna(0, inplace=True)
# 这些特征用None填充缺失值
cols1 = ["PoolQC", "MiscFeature", "Alley", "Fence", "FireplaceQu", "GarageQual", "GarageCond", "GarageFinish",
         "GarageYrBlt", "GarageType", "BsmtExposure", "BsmtCond", "BsmtQual", "BsmtFinType2", "BsmtFinType1",
         "MasVnrType"]
for col in cols1:
    full[col].fillna("None", inplace=True)
# 用mode填充
cols2 = ["MSZoning", "BsmtFullBath", "BsmtHalfBath", "Utilities", "Functional", "Electrical", "KitchenQual", "SaleType",
         "Exterior1st", "Exterior2nd"]
for col in cols2:
    full[col].fillna(full[col].mode()[0], inplace=True)
full.isnull().sum()[full.isnull().sum() > 0]
NumStr = ["MSSubClass", "BsmtFullBath", "BsmtHalfBath", "HalfBath", "BedroomAbvGr", "KitchenAbvGr", "MoSold", "YrSold",
          "YearBuilt", "YearRemodAdd", "LowQualFinSF", "GarageYrBlt"]
for col in NumStr:
    full[col] = full[col].astype(str)
```

### 6月16号实操：<br>
1.今天完成的任务:<br>
1.数据预处理:<br>
数据预处理主要是指在对数据主要处理以前对数据进行的一些处理。数据预处理有四个任务，数据清洗、数据集成、数据变换和数据规约。在真实数据中，我们拿到的数据可能包含了大量的缺失值，可能包含大量的噪音，也可能因为人工录入错误（比如，医生的就医记录）导致有异常点存在，对我们挖据出有效信息造成了一定的困扰，所以我们需要通过一些方法，尽量提高数据的质量。<br>
2.具体内容:<br>
1）数据清洗：<br>
- 有监督清洗：在对应领域专家的指导下，收集分析数据，手工去除明显的噪声数据和重复记录，填补缺值数据等清洗动作
- 无监督清洗：根据一定的业务规则，预先定义好数据清洗算法，由计算机自动执行算法，对数据集进行清洗，然后产生清洗报告
常用的清洗规则主要包括：空值的检查和处理；非法值的检测和处理；不一致数据的检测和处理；相似重复记录的检测和处理。执行数据清洗规则时需要检查拼写错误，去掉重复的（duplicate）记录，补上不完全的（incomplete）记录，解决不一致的（inconsistent）记录，用测试查询来验证数据，最后需要生成数据清晰报告。在清洗结果验证中，需要对定义的清洗转换规则的正确性和效率进行验证和评估，当不满足清洗要求时要对清洗规则或系统参数进行调整和改进，数据清洗过程中往往需要多次迭代的进行分析，设计和验证。<br>
2）数据集成:
数据集成就是将多个数据源合并存放在一个一致的数据存储（如数据仓库）中的过程。
- 实体识别
   - 同名异义
   - 名字相同但实际代表的含义不同
   - 异名同义
   - 名字不同但代表的意思相同
   - 单位不统一
- 冗余属性识别<br>
3)数据变换:
- 简单函数变换
- 规范化
   - 最小最大规范化:值与最小值的差 再除以极差得到规范后的值
   - 零-均值规范化:值与平均值的差 在除以标准差,这种规范的方式是当前最多的数据标准化方法
   - 小数定标规范法:移动的小数位数取决于绝对值的最大值。
- 连续属性离散化：连续属性的离散化就是在数据的取值范围内设定若干个离散的划分点，将取值范围划分为一些离散化的区间，最后用不同的符号或者整数值代表落在每个子区间中的数据值。<br>
- 等宽法
- 等频法
- 基于聚类分析的方法<br>
4)数据规约：
在大数据集上进行复杂的数据分析和挖掘需要很长的时间，数据规约产生更小但保持数据完整性的新数据集。
在规约后的数据集上进行分析和挖掘将更有效率。<br>
数据规约的意义在于：
- 降低无效、错误数据对建模的影响，提高建模的准确性。
- 少量且具代表性的数据将大幅缩减数据挖掘所需的时间。
- 降低存储数据的成本<br>
### 6月15号实操：<br>
1.今天完成的任务:<br>
1）什么是回归分析:<br>
回归分析是一种预测性的建模技术，它研究的是因变量（目标）和自变量（预测器）之间的关系。这种技术通常用于预测分析，时间序列模型以及发现变量之间的因果关系。例如，司机的鲁莽驾驶与道路交通事故数量之间的关系，最好的研究方法就是回归。<br>
2)回归分析常用算法：<br>
- Linear Regression线性回归：它是最为人熟知的建模技术之一。线性回归通常是人们在学习预测模型时首选的技术之一。在这种技术中，因变量是连续的，自变量可以是连续的也可以是离散的，回归线的性质是线性的。线性回归使用最佳的拟合直线（也就是回归线）在因变量（Y）和一个或多个自变量（X）之间建立一种关系。用一个方程式来表示它，即Y=b+a*X + e，其中b表示截距，a表示直线的斜率，e是误差项。这个方程可以根据给定的预测变量（s）来预测目标变量的值.
- Logistic Regression逻辑回归:逻辑回归是用来计算“事件=Success”和“事件=Failure”的概率。当因变量的类型属于二元（1 / 0，真/假，是/否）变量时，我们就应该使用逻辑回归。这里，Y的值从0到1，它可以用下方程表示。p表述具有某个特征的概率。
```java  
y=a+b*x^2
```
- Polynomial Regression多项式回归:对于一个回归方程，如果自变量的指数大于1，那么它就是多项式回归方程。如下方程所示：
```java  
y=a+b*x^2
```
- Ridge Regression岭回归：岭回归分析是一种用于存在多重共线性（自变量高度相关）数据的技术。在多重共线性情况下，尽管最小二乘法（OLS）对每个变量很公平，但它们的差异很大，使得观测值偏移并远离真实值。岭回归通过给回归估计上增加一个偏差度，来降低标准误差。
```java  
y=b+a*x+e (error term),  [error term is the value needed to correct for a prediction error between the observed and predicted value]
```

### 6月5号实操：<br>
1.今天完成的任务:<br>
学习了Sqoop工具模块的sqoop-export<br>
1）常用参数<br>
```java  
--connect <jdbc-uri>：指定JDBC连接的数据库地址。
--connection-manager <class-name>：指定要使用的连接管理器类。
--driver <class-name>：手动指定要使用的JDBC驱动类。
--hadoop-mapred-home <dir>：指定$ HADOOP_MAPRED_HOME路径
--help：打印使用说明
--password-file：为包含认证密码的文件设置路径。
-P：从控制台读取密码。
--password <password>：设置验证密码。
--username <username>：设置验证用户名。
--verbose：在工作时打印更多信息。
--connection-param-file <filename>：提供连接参数的可选属性文件。
--relaxed-isolation：将连接事务隔离设置为未提交给映射器的读取。
```
2）验证参数：<br>
```java  
--validate：启用对复制数据的验证，仅支持单个表复制。
--validator <class-name>：指定要使用的验证程序类。
--validation-threshold <class-name>：指定要使用的验证阈值类。
--validation-failurehandler <class-name>：指定要使用的验证失败处理程序类
```
3）导出控制参数：<br>
```java  
--columns <col,col,col…>：要导出到表格的列。
--direct：使用直接导出快速路径。
--export-dir <dir>：用于导出的HDFS源路径。
-m,--num-mappers <n>：使用n个mapper任务并行导出。
--table <table-name>：要填充的表。
--call <stored-proc-name>：存储过程调用。
--update-key <col-name>：锚点列用于更新。如果有多个列，请使用以逗号分隔的列列表。
--update-mode <mode>：指定在数据库中使用不匹配的键找到新行时如何执行更新。mode包含的updateonly默认值（默认）和allowinsert。
--input-null-string <null-string>：字符串列被解释为空的字符串。
--input-null-non-string <null-string>：要对非字符串列解释为空的字符串。
--staging-table <staging-table-name>：数据在插入目标表之前将在其中展开的表格。
--clear-staging-table：表示可以删除登台表中的任何数据。
--batch：使用批处理模式执行基础语句
```
### 6月4号实操：<br>
1.今天完成的任务:<br>
初步实现Spark SQL查询分析器的html代码<br>

### 6月3号实操：<br>
1.今天完成的任务:<br>
1)安装maven--包管理工具，可类比为conda或pip，配置环境变量,接着更换maven镜像源：https://blog.csdn.net/zzcchunter/article/details/84795105<br>
2)使用IDEA创建springboot项目,安装插件spring-assistant插件自动下载springboot初始项目,https://blog.csdn.net/walykyy/article/details/82776277<br>
3)参考博客https://blog.csdn.net/baidu_39298625/article/details/98102453 实现springboot项目的demo<br>
2.遇到的问题及解决方法：<br>
1)端口8080被占用：在application.properties文件中添加：
```java  
server.port=${port:8088}
```
2)SpringBoot 项目启动后无法打开html页面问题:<br>
参考博客https://www.jianshu.com/p/8ce9d1f6d212 <br>

### 6月2号实操：<br>
1.今天完成的任务：<br>
1)学习了Flink的窗口：<br>
参考博客https://www.cnblogs.com/bjwu/p/10393146.html<br>
2)运用Flink窗口函数统计最近一分钟出现字符“b”的次数：<br>
```java  
object Main {

  val target = "b"

  def main(args: Array[String]) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //Linux or Mac:nc -l 9999
    //Windows:nc -l -p 9999
    val text = env.socketTextStream("localhost", 9999)
    val stream = text.flatMap {
      _.toLowerCase.split("\\W+") filter {
        _.contains(target)
      }
    }.map (
      new MapFunction[String,(String,Int)](){
        override def map(t:String):(String,Int)={
          val chars=t.toLowerCase.split("")
          val cnt:Int = chars.count(
            p=new Function[String,Boolean]{
              override def apply(v1:String):Boolean={
                if(v1.equals(target)){
                  return true
                }
                return false
              }
            })
          return (target,cnt)
        }
      }
    ).keyBy(0).timeWindow(Time.seconds(60)).sum(1)
    stream.print()
    env.execute("Window Stream WordCount")

    
```

### 6月1号实操：<br>
1.今天完成的任务：<br>
1)了解了SQL关系数据库与JAVA开发语言之间的关系
   - SQL是结构化的查询语言，将SQL和关系数据库联合到一起，形成SQL关系数据库，可为网络编程提供更加便利的查询条件，目前SQL关系数据库主要有三种模式结构，一种是用户模式;一种是逻辑模式，另一种是物理模式。
   - JAVA开发语言属于一种设计性语言，是目前网络编程中应用最为广泛的语言，可实现跨平台的程序设计，并且具有很强的通用性，比如：目前应用最广泛的手机软件、Web应用系统都是通过JAVA开发语言设计出来的。
   - JAVA开发语言主要是通过JDBC来实现对SQL关系数据库的访问，因此，JDBC可以看作连接器，是执行SQL关系数据库语句时JAVA的接口，主要由JAVA开发语言编写而成，通过调运对应的API接口，实现对SQL关系数据库的访问。<br>
2）JDBC的作用:<br>
众所周知，JAVA开发语言具有坚固、安全、可移植等特性，是目前程序编写中应用最为主流的语言。一个相对独立的数据管理系统的用户界面是实现数据管理系统应用的主要方式，而JAVA开发语言正是基于这一观念设计了一种通用的SQL关系数据库访问和存储结构，也就是JDBC，可有效支持基本的SQL功能，是一种通用于低层的API。可以为SQL关系数据库的功能模块提供一个统一的用户界面，通过对界面操作就可以实现对SQL关系数据库所有功能的使用，促使独立于数据管理系统的JAVA开发语言的应用成为可能。总而言之，通过JDBC可以更加轻松的向关系数据库发送SQL语句，通过API编写一个程序，就可以向数据库发展SQL语句，JAVA开发语言无需考虑不同平台，将JAVA和JDBC联合到一起，通过编写一道程序，就可以在任何平台上顺利运行。<br>
3）SQL关系数据库与JAVA开发语言的融合路径
在具体开发过程中，应用程序可以直接对数据进行连接，需要JDBC作为二者连接的通讯设备，当用户把SQL语句发送到数据库中时，相应的结果会立即返回给用户，因此通过JDBC可以和不同的程序进行通讯。在实现SQL关系数据库与JAVA开发语言的融合过程中主要涉及到以下几个步骤：<br>
- 第一步，注册JDBC驱动程序。这一点也是实现SQL关系数据库与JAVA开发语言的融合的关键，应用程序通过代码驱动程序来实现，驱动程序被连接到数据库中时，数据不同，驱动程序的种类也不相同，就SQL关系数据库而言，在运行过程中可以同时装载多个驱动程序，以满足和JAVA开发语言的融合需求。
- 第二步，建立和数据库的连接。打开连接和数据建立连接的标准方式为调用Drive Manager.get Connection，并且还能提供驱动程序从而和数据库相互连接，并向数据库中输送相关信息。并获得一个登陆数据库的用户名和密码。如果找到了和第一步驱动程序相互对应的URL，并且提供的登陆密码相同，就可以实现和数据库的全面连接，如果用户名或者密码错误发出SQL Excepiton异常。
- 第三步，创建一个Statement对象来执行SQL语句和存储过程中，并且该对象提供的接口是JDBC中最为重要的一个接口，此接口通过Connection对象建立，就可以完成对执行标准的SQL语句和整个存储过程。
- 第四步，处理结果集。所谓结果集其实就ResultSet接口，此接口主要的作用是处理数据库中查询到的结果，可用作对数据库记录的定位，如果返回一个游标，则可以从此游标中得到不同列的信息。
- 第五步，关闭JDBC对象。这一步也是实现SQL关系数据库与JAVA开发语言相互融合的重中之重，在整个融合过中，数据库的连接是有限制的，在具体融合过程中，为最大限度上提升应用程序的执行效率，当使用完成后，就必须关闭JDBC对象，因此，在具体融合过程中，要严格遵循“随时建立，随时关闭”的原则。
<br>
4)实现对身份证号码脱敏处理：<br>

```java  
try{

      result.foreach(x => {
      //把处理之前的图片输出到input文件夹中
     println(Utils.idEncrypt(x(0).toString))
    })


    //输出结果到控制台
    result.map(t => " idCard:" + t(0)).collect().foreach(println)
    sc.stop()

    
```

```java  


       public static String idEncrypt(String id) {
        if (StringUtils.isEmpty(id)) {
            return id;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String str="****";
        return stringBuilder.append(str).append(id.substring(4,id.length() - 4)).append(str).toString();

    }
    
```
### 5月29号实操：<br>
1.今天完成的任务<br>
1)了解了通过SparkSQL JDBC连接数据库的好处<br>
JDBC(Java DataBase Connectivity)：是一套统一访问各类数据库的标准Java接口，为各个数据库厂商提供了标准的实现。通过JDBC技术，开发人员可以用纯Java语言和标准的SQL语句编写完整的数据库应用程序，并且真正地实现了软件的跨平台性。<br>
2)实现了使用JDBC的方式，从Greenplum中选取一个表并读出数据<br>
```java  
try{

      println("表数据：")
      val resultSet = statement.executeQuery("select * from t_rk_jbxx limit 10")
      while (resultSet.next) {
        val asjbh = resultSet.getString(4)
        val ajmc = resultSet.getString(1)
        val bamjbh = resultSet.getString(8)
        //输出表数据

        println(s"$asjbh    ｜$ajmc    ｜$bamjbh")
      }
      resultSet.close()

    
```

### 5月28号实操：<br>
1.今天完成的任务<br>
1)了解了分布式计算框架:<br>
Apache Hadoop<br>

批处理模式<br>
Hadoop的处理功能来自MapReduce引擎，采用了hadoop分布式文件系统HDFS。可以在普通的PC集群上提供可靠的文件存储，通过数据块进行多个副本备份来解决服务器宕机或者硬盘损坏的问题。<br>

MapReduce，把并发、分布式（如机器间通信）和故障恢复等计算细节隐藏起来，在有足够量计算机集群的前提下，一般每台机器构成一个Maper或者Reducer。
MapReduce的处理技术符合使用键值对的map、shuffle、reduce算法要求。由Map和Reduce两个部分组成一个job，再由job组成DAG，其中把非常重要的Shuffle过程隐藏起来。基本处理过程包括：<br>

- 从HDFS文件系统读取数据集

- 将数据集拆分成小块并分配给所有可用节点

- 针对每个节点上的数据子集进行计算

- 中间结果暂时保存在内存中，达到阈值会写到磁盘上

- 重新分配中间态结果并按照键进行分组

- 通过对每个节点计算的结果进行汇总和组合对每个键的值进行“Reducing”

- 将计算而来的最终结果重新写入 HDFS
<br>
Apache Spark<br>
Apache Spark是一种包含流处理能力的下一代批处理框架。特色是提供了一个集群的分布式内存抽象RDD（Resilient Distributed DataSet），即弹性分布式数据集。Spark上有RDD的两种操作，actor和traformation。transformation包括map、flatMap等操作，actor是返回结果，如Reduce、count、collect等<br>

与MapReduce不同，Spark的数据处理工作全部在内存中进行，只在一开始将数据读入内存，以及将最终结果持久存储时需要与存储层交互。所有中间态的处理结果均存储在内存中。<br>
2)了解了数据库与数据仓库的区别：
1. 数据库存储的多为实时的业务数据，而数据仓库存储的多为历史数据。<br>
2. 数据库是面向事务设计的，而数据仓库是面向主题设计的。<br>
3. 开发人员都知道，数据库的设计都会尽量的避免冗余，针对于某一业务进行设计。而数据仓库的设计则是在有意的引入冗余，依照各种分析需求、维度、指标等进行设计。<br>
4. 数据库是为了业务的数据读写，而数据仓库是为了分析大量数据。<br>
数据仓库中一个最重要的组成部分就是元数据管理，元数据管理简单来说就是关于数据仓库中数据的数据。类似于字典和黄页。保存了整个数据仓库逻辑数据结构、地址、索引、文件等等信息。元数据管理会记录数据仓库中模型的定义、各层级间的映射关系、监控数据仓库的数据状态及 相关ETL 的任务运行状态等，使数据仓库的设计、部署、操作和管理能达成协同和一致。元数据的存储常见的方式有两种，一种是以数据集为基础，每一个数据集有对应的元数据文件，每一个元数据文件包含对应数据集的元数据内容；另一种存储方式是以数据库为基础，即元数据库。比如Hive我们常常使用mysql作为其元数据库（metastore）的存储。<br>


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
- 对内部表的修改会将修改直接同步给元数据，而对外部表的表结构和分区进行修改，则需要修复（MSCK REPAIR TABLE table_name;）<br>
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
   <br>
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
