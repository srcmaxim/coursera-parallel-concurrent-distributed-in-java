1.1 Introduction to MapReduce

Lecture Summary: In this lecture, we learned the MapReduce paradigm, which is a pattern of parallel functional programming that has been very successful in enabling "big data" computations.

The input to a MapReduce style computation is a set of key-value pairs. The keys are similar to keys used in hash tables, and the functional programming approach requires that both the keys and values be immutable. When a user-specified map function, f, is applied on a key-value pair, (kA,vA), it results in a (possibly empty) set of output key-value pairs, {(kA1,vA1), (kA2,vA2),....} This map function can be applied in parallel on all key-value pairs in the input set, to obtain a set of intermediate key-value pairs that is the union of all the outputs.

The next operation performed in the MapReduce workflow is referred to as grouping, which groups together all intermediate key-value pairs with the same key. Grouping is performed automatically by the MapReduce framework, and need not be specified by the programmer. For example, if there are two intermediate key- value pairs, (kA1, vA1) and (kB1, vB1) with the same key, kA1 = kB1 =k, then the output of grouping will associate the set of values {vA1,vB1} with key k.

Finally, when a user-specified reduce function, g, is applied on two or more grouped values (e.g., vA1, vB1,...) associated with the same key k, it folds or reduces all those values to obtain a single output key-value pair, (k, g(vA1, vB1, . . .)), for each key, k. in the intermediate key-value set. If needed, the set of output key-value pairs can then be used as the input for a successive MapReduce computation.

In the example discussed in the lecture, we assumed that the map function, f, mapped a key-value pair like (“WR”,10) to a set of intermediate key-value pairs obtained from factors of 10 to obtain the set, { (“WR”,2), (“WR”,5), (“WR”,10) }, and the reduce function, g, calculated the sum of all the values with the same key to obtain (“WR”,17) as the output key-value pair for key “WR”. The same process can be performed in parallel for all keys to obtain the complete output key-value set.

Optional Reading:
Wikipedia article on the [MapReduce](https://en.wikipedia.org/wiki/MapReduce) framework

MapReduce is a programming model and an associated implementation for processing and generating big data sets with a parallel, distributed algorithm on a cluster.[1][2]

A MapReduce program is composed of a Map() procedure (method) that performs filtering and sorting (such as sorting students by first name into queues, one queue for each name) and a Reduce() method that performs a summary operation (such as counting the number of students in each queue, yielding name frequencies). The "MapReduce System" (also called "infrastructure" or "framework") orchestrates the processing by marshalling the distributed servers, running the various tasks in parallel, managing all communications and data transfers between the various parts of the system, and providing for redundancy and fault tolerance.

- Input reader
The input reader divides the input into appropriate size 'splits' (in practice typically 64 MB to 128 MB) and the framework assigns one split to each Map function. The input reader reads data from stable storage (typically a distributed file system) and generates key/value pairs.
A common example will read a directory full of text files and return each line as a record.
- Map function
The Map function takes a series of key/value pairs, processes each, and generates zero or more output key/value pairs. The input and output types of the map can be (and often are) different from each other.
If the application is doing a word count, the map function would break the line into words and output a key/value pair for each word. Each output pair would contain the word as the key and the number of instances of that word in the line as the value.
- Partition function
Each Map function output is allocated to a particular reducer by the application's partition function for sharding purposes. The partition function is given the key and the number of reducers and returns the index of the desired reducer.
A typical default is to hash the key and use the hash value modulo the number of reducers. It is important to pick a partition function that gives an approximately uniform distribution of data per shard for load-balancing purposes, otherwise the MapReduce operation can be held up waiting for slow reducers to finish (i.e. the reducers assigned the larger shares of the non-uniformly partitioned data).
Between the map and reduce stages, the data are shuffled (parallel-sorted / exchanged between nodes) in order to move the data from the map node that produced them to the shard in which they will be reduced. The shuffle can sometimes take longer than the computation time depending on network bandwidth, CPU speeds, data produced and time taken by map and reduce computations.
- Comparison function
The input for each Reduce is pulled from the machine where the Map ran and sorted using the application's comparison function.
- Reduce function
The framework calls the application's Reduce function once for each unique key in the sorted order. The Reduce can iterate through the values that are associated with that key and produce zero or more outputs.
In the word count example, the Reduce function takes the input values, sums them and generates a single output of the word and the final sum.
- Output writer
The Output Writer writes the output of the Reduce to the stable storage.

1.2 Apache Hadoop Project

Lecture Summary: The Apache Hadoop project is a popular open-source implementation of the Map-Reduce paradigm for distributed computing. A distributed computer can be viewed as a large set of multicore computers connected by a network, such that each computer has multiple processor cores, e.g., P0, P1, P2, P3, ... . Each individual computer also has some persistent storage (e.g., hard disk, flash memory), thereby making it possible to store and operate on large volumes of data when aggregating the storage available across all the computers in a data center. The main motivation for the Hadoop project is to make it easy to write large-scale parallel programs that operate on this “big data”.

The Hadoop framework allows the programmer to specify map and reduce functions in Java, and takes care of all the details of generating a large number of map tasks and reduce tasks to perform the computation as well as scheduling them across a distributed computer. A key property of the Hadoop framework is that it supports automatic fault-tolerance. Since MapReduce is essentially a functional programming model, if a node in the distributed system fails, the Hadoop scheduler can reschedule the tasks that were executing on that node with the same input elsewhere, and continue computation. This is not possible with non-functional parallelism in general, because when a non-functional task modifies some state, re-executing it may result in a different answer. The ability of the Hadoop framework to process massive volumes of data has also made it a popular target for higher-level query languages that implement SQL-like semantics on top of Hadoop.

The lecture discussed the word-count example, which, despite its simplicity, is used very often in practice for document mining and text mining. In this example, we illustrated how a Hadoop map-reduce program can obtain word-counts for the distributed text “To be or not to be”. There are several other applications that have been built on top of Hadoop and other MapReduce frameworks. The main benefit of Hadoop is that it greatly simplifies the job of writing programs to process large volumes of data available in a data center.

Optional Reading:
1. Wikipedia article on the [Apache Hadoop](https://en.wikipedia.org/wiki/Apache_Hadoop) project

Apache Hadoop -- is an open-source software framework used for distributed storage and processing of dataset of big data using the MapReduce programming model. It consists of computer clusters built from commodity hardware. All the modules in Hadoop are designed with a fundamental assumption that hardware failures are common occurrences and should be automatically handled by the framework.

The core of Apache Hadoop consists of a storage part, known as Hadoop Distributed File System (HDFS), and a processing part which is a MapReduce programming model. Hadoop splits files into large blocks and distributes them across nodes in a cluster. It then transfers packaged code into nodes to process the data in parallel. This approach takes advantage of data locality,[3] where nodes manipulate the data they have access to. This allows the dataset to be processed faster and more efficiently than it would be in a more conventional supercomputer architecture that relies on a parallel file system where computation and data are distributed via high-speed networking.

The term Hadoop has come to refer not just to the aforementioned base modules and sub-modules, but also to the ecosystem,[8] or collection of additional software packages that can be installed on top of or alongside Hadoop, such as Apache Pig, Apache Hive, Apache HBase, Apache Phoenix, Apache Spark, Apache ZooKeeper, Cloudera Impala, Apache Flume, Apache Sqoop, Apache Oozie, and Apache Storm.

A small Hadoop cluster includes a single master and multiple worker nodes. The master node consists of a Job Tracker, Task Tracker, NameNode, and DataNode. A slave or worker node acts as both a DataNode and TaskTracker, though it is possible to have data-only and compute-only worker nodes. These are normally used only in nonstandard applications.

1.3 Apache Spark Framework

Lecture Summary: Apache Spark is a similar, but more general, programming model than Hadoop MapReduce. Like Hadoop, Spark also works on distributed systems, but a key difference in Spark is that it makes better use of in- memory computing within distributed nodes compared to Hadoop MapReduce. This difference can have a significant impact on the performance of iterative MapReduce algorithms since the use of memory obviates the need to write intermediate results to external storage after each map/reduce step. However, this also implies that the size of data that can be processed in this manner is limited by the total size of memory across all nodes, which is usually much smaller than the size of external storage. (Spark can spill excess data to external storage if needed, but doing so reduces the performance advantage over Hadoop.)

Another major difference between Spark and Hadoop MapReduce, is that the primary data type in Spark is the Resilient Distributed Dataset (RDD), which can be viewed as a generalization of sets of key-value pairs. RDDs enable Spark to support more general operations than map and reduce. Spark supports intermediate operations called Transformations (e.g., map,filter,join,...) and terminal operations called Actions (e.g., reduce,count,collect,...). As in Java streams, intermediate transformations are performed lazily, i.e., their evaluation is postponed to the point when a terminal action needs to be performed.

In the lecture, we saw how the Word Count example can be implemented in Spark using Java APIs. (The Java APIs use the same underlying implementation as Scala APIs, since both APIs can be invoked in the same Java virtual machine instance.) We used the Spark flatMap() method to combine all the words in all the lines in an input file into a single RDD, followed by a mapToPair() Transform method call to emit pairs of the form, (word, 1), which can then be processed by a reduceByKey() operation to obtain the final word counts.

Optional Reading:
1. [Spark Programming Guide](https://spark.apache.org/docs/latest/rdd-programming-guide.html)
2. Wikipedia article on the [Apache Spark project](https://en.wikipedia.org/wiki/Apache_Spark)

Apache Spark provides programmers with an application programming interface centered on a data structure called the resilient distributed dataset (RDD), a read-only multiset of data items distributed over a cluster of machines, that is maintained in a fault-tolerant way. It was developed in response to limitations in the MapReduce cluster computing paradigm, which forces a particular linear dataflow structure on distributed programs: MapReduce programs read input data from disk, map a function across the data, reduce the results of the map, and store reduction results on disk. Spark's RDDs function as a working set for distributed programs that offers a (deliberately) restricted form of distributed shared memory.

The availability of RDDs facilitates the implementation of both iterative algorithms, that visit their dataset multiple times in a loop, and interactive/exploratory data analysis, i.e., the repeated database-style querying of data. The latency of such applications (compared to a MapReduce implementation, as was common in Apache Hadoop stacks) may be reduced by several orders of magnitude. Among the class of iterative algorithms are the training algorithms for machine learning systems, which formed the initial impetus for developing Apache Spark.

Apache Spark requires a cluster manager and a distributed storage system. For cluster management, Spark supports standalone (native Spark cluster), Hadoop YARN, or Apache Mesos. For distributed storage, Spark can interface with a wide variety, including Hadoop Distributed File System (HDFS), MapR File System (MapR-FS),[8] Cassandra,[9] OpenStack Swift, Amazon S3, Kudu, or a custom solution can be implemented. Spark also supports a pseudo-distributed local mode, usually used only for development or testing purposes, where distributed storage is not required and the local file system can be used instead; in such a scenario, Spark is run on a single machine with one executor per CPU core.

1.4 TF-IDF Example

Lecture Summary: In this lecture, we discussed an important statistic used in information retrieval and document mining, called Term Frequency – Inverse Document Frequency (TF-IDF). The motivation for computing TF-IDF statistics is to efficiently identify documents that are most similar to each other within a large corpus.

Assume that we have a set of N documents D1,D2,…,DN, and a set of terms TERM1,TERM2,… that can appear in these documents. We can then compute total frequencies TFi,j for each term TERMi in each document Dj . We can also compute the document frequencies DF1,DF2,... for each term, indicating how many documents contain that particular term, and the inverse document frequencies (IDF):IDFi=N/DFi. The motivation for computing inverse document frequencies is to determine which terms are common and which ones are rare, and give higher weights to the rarer terms when searching for similar documents. The weights are computed as: Weight(TERMi,Dj)=TFi,j x log(N/DFi).

Using MapReduce, we can compute the TFi,j values by using a MAP operation to find all the occurrences of TERMi in document Dj, followed by a REDUCE operation to add up all the occurrences of TERMi as key-value pairs of the form, ((Dj,TERMi),TFi,j) (as in the Word Count example studied earlier). These key-value pairs can also be used to compute DFi values by using a MAP operation to identify all the documents that contain TERMi and a REDUCE operation to count the number of documents that TERMi appears in. The final weights can then be easily computed from the TFi,j and DFi values. Since the TF−IDF computation uses a fixed (not iterative) number of MAP and REDUCE operations, it is a good candidate for both Hadoop and Spark frameworks.

Optional Reading:
1. Wikipedia article on the [Term Frequency – Inverse Document Frequency (TF-IDF) statistic](https://en.wikipedia.org/wiki/Tf%E2%80%93idf)

1.5 PageRank Example

Lecture Summary: In this lecture, we discussed the PageRank algorithm as an example of an iterative algorithm that is well suited for the Spark framework. The goal of the algorithm is to determine which web pages are more important by examining links from one page to other pages. In this algorithm, the rank of a page, B, is defined as follows,

RANK(B)=∑A∈SRC(B)RANK(A)DEST_COUNT(A)
where SRC(B) is the set of pages that contain a link to B, while DEST_COUNT(A) is the total number of pages that A links to. Intuitively, the PageRank algorithm works by splitting the weight of a page A (i.e., RANK(A)) among all of the pages that A links to (i.e. DEST_COUNT(A)). Each page that A links to has its own rank increased proportional to A's own rank. As a result, pages that are linked to from many highly-ranked pages will also be highly ranked.

The motivation to divide the contribution of A in the sum by DEST_COUNT(A) is that if page A links to multiple pages, each of the successors should get a fraction of the contribution from page A. Conversely, if a page has many outgoing links, then each successor page gets a relatively smaller weightage, compared to pages that have fewer outgoing links. This is a recursive definition in general, since if (say) page X links to page Y, and page Y links to page X, then RANK(X) depends on RANK(Y) and vice versa. Given the recursive nature of the problem, we can use an iterative algorithm to compute all page ranks by repeatedly updating the rank values using the above formula, and stopping when the rank values have converged to some acceptable level of precision. In each iteration, the new value of RANK(B) can be computed by accumulating the contributions from each predecessor page, A. A parallel implementation in Spark can be easily obtained by implementing two steps in an iteration, one for computing the contributions of each page to its successor pages by using the flatMapToPair() method, and the second for computing the current rank of each page by using the reduceByKey() and mapValues() methods.. All the intermediate results between iterations will be kept in main memory, resulting in a much faster execution than a Hadoop version (which would store intermediate results in external storage).

Optional Reading:
1. Wikipedia article on the [PageRank algorithm](https://en.wikipedia.org/wiki/PageRank)
