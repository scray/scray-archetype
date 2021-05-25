package ${package}

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
/**
 * Class containing all the batch stuff
 */
class BatchJob(@transient val sc: SparkContext) extends LazyLogging with Serializable {

  /**
   * get the intial rdd to load data from
   */
  def getBatchRDD() = {
    // TODO: create rdd here
    // example to load data from Cassandra: sc.cassandraTable("scraykeyspace", "inputcolumnfamily")
    // for now, we use a hard-wired collection
    val data = Array(("bla1", "blubb1", "org", "Tokio"),
                    ("bla1", "blubb2", "com", "Johannisburg"),
                    ("bla2", "blubb1", "net", "Rio"))
    sc.parallelize(data)
  }


  /**
   * do the job
   */
  def batchAggregate() = {
    // TODO: define your own job!
    val dataRDD = getBatchRDD.map(row => ((row._1), 1))
    val reducedRDD = dataRDD.reduceByKey(_ + _)
    .map(xx => println("Number of rows: " + xx))
  }
}
