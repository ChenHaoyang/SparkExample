package org.sparkexample;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WordCount {
  private static final FlatMapFunction<String, String> WORDS_EXTRACTOR =
      new FlatMapFunction<String, String>() {
        @Override
        public Iterable<String> call(String s) throws Exception {
          return Arrays.asList(s.split(" "));
        }
      };

  private static final PairFunction<String, String, Integer> WORDS_MAPPER =
      new PairFunction<String, String, Integer>() {
        @Override
        public Tuple2<String, Integer> call(String s) throws Exception {
          return new Tuple2<String, Integer>(s, 1);
        }
      };

  private static final Function2<Integer, Integer, Integer> WORDS_REDUCER =
      new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer call(Integer a, Integer b) throws Exception {
          return a + b;
        }
      };
  public static void Test(){
	  System.out.println("Test");
  }

  /**
 * @param args
 * args[0]: path of input file(e.g. words.txt)
 * args[1]: output path(e.g. output)
 */
public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Please provide the input file full path as argument");
      System.exit(0);
    }

    SparkConf conf = new SparkConf()
    		.setAppName("org.sparkexample.WordCount")
    		.setMaster("local[*]")
    		.set("spark.driver.maxResultSize", "10g");
    JavaSparkContext context = new JavaSparkContext(conf);
    
    //Load text file from local system
    JavaRDD<String> file = context.textFile(args[0]);
    //RDD transform(flatMap)
    JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
    //RDD transform(map operation to create a pair:[key, value])
    JavaPairRDD<String, Integer> pairs = words.mapToPair(WORDS_MAPPER);
    //RDD transform(reduce operation by key)
    JavaPairRDD<String, Integer> counter = pairs.reduceByKey(WORDS_REDUCER);
    //save the RDD to text file
    counter.saveAsTextFile(args[1]);
    context.stop();
    
   
/********************An example of using Apache hbase-spark package********************/
    /*
    //create the hbaseconfig object
    Configuration hbaseConfig = HBaseConfiguration.create();
    //add the corresponding setting files into the hbaseconfig
    hbaseConfig.addResource(new Path("/usr/local/hadoop-2.5.0-cdh5.3.9/etc/hadoop/core-site.xml"));
    hbaseConfig.addResource(new Path("/usr/local/hadoop-2.5.0-cdh5.3.9/etc/hadoop/hdfs-site.xml"));
    hbaseConfig.addResource(new Path("/usr/local/hadoop-2.5.0-cdh5.3.9/etc/hadoop/hbase-site.xml"));
    //set the maximal buffer(128M) for batch put operations
    hbaseConfig.set("hbase.client.write.buffer","134217728");
    //disable the check of keyvalue maxsize(because the html content may very large)
    hbaseConfig.set("hbase.client.keyvalue.maxsize","0");
    //set the connect user name to hdfs
    System.setProperty("HADOOP_USER_NAME", "hdfs");
    //create JavaHBaseContext
    JavaHBaseContext hbaseContext = new JavaHBaseContext(context, hbaseConfig);
    
    try {
    	  List<byte[]> list = new ArrayList<>();
    	  list.add(Bytes.toBytes("row_id01"));
    	  list.add(Bytes.toBytes("row_id02"));
    	  list.add(Bytes.toBytes("row_id03"));
    	  list.add(Bytes.toBytes("row_id04"));
    	  list.add(Bytes.toBytes("row_id05"));

    	  JavaRDD<byte[]> rdd = context.parallelize(list);
    	  //Applies a function to each partition of the RDD
    	  hbaseContext.foreachPartition(rdd,
    	      new VoidFunction<Tuple2<Iterator<byte[]>, Connection>>() {
    	   public void call(Tuple2<Iterator<byte[]>, Connection> t)
    	        throws Exception {
    		//get table object from the connection object
    	    Table table = t._2().getTable(TableName.valueOf("tableName"));
    	    //get bufferedmutator from the connection object(for batch input operations)
    	    BufferedMutator mutator = t._2().getBufferedMutator(TableName.valueOf("tableName"));
    	    while (t._1().hasNext()) {
    	      byte[] b = t._1().next();
    	      Result r = table.get(new Get(b));
    	      if (r.getExists()) {
    	       mutator.mutate(new Put(b));
    	      }
    	    }
    	    mutator.flush();
    	    mutator.close();
    	    table.close();
    	   }
    	  });
    	} finally {
    		context.stop();
    	}
    */
  }
}
