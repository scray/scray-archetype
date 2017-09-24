package ${package}

import java.util.concurrent.locks.ReentrantLock

import scala.collection.convert.decorateAsScala.asScalaIteratorConverter
import scala.collection.convert.decorateAsScala.asScalaBufferConverter
import scala.collection.mutable.HashMap
import scala.reflect.runtime.universe

import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.State
import org.apache.spark.streaming.StateSpec
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.spark.connector.CassandraRow
import com.datastax.spark.connector.rdd.CassandraTableScanRDD
import com.datastax.spark.connector.streaming.toDStreamFunctions
import com.datastax.spark.connector.streaming.toStreamingContextFunctions
import ${package}.data.AggregationKey
import com.typesafe.scalalogging.slf4j.LazyLogging
import scray.querying.sync.JobInfo
import com.datastax.driver.core.querybuilder.Insert
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Statement


/**
 * job that transforms data :)
 */
class StreamingJob(@transient val ssc: StreamingContext, jobInfo: JobInfo[Statement, Insert, ResultSet]) extends LazyLogging with Serializable {
  
  /**
   * main job execution method: filter for wrong keys, perform aggregation with state changes, transform data into
   * output format and save
   */
  def runTuple[T <: DStream[(Option[AggregationKey], Long)]](dstream: T, host: Option[String], keyspace: Option[String], master: String) = {  

    val processedStream = dstream.
      filter(x => x._1.isDefined).
      map(println)
  }
}

object StreamingJob {
  
  /**
   * TODO: setup a key for the aggregation function using the key class defined in data
   */
  def buildAggregationKey(row: (String, String, String, String)): Option[AggregationKey] = {
    Some(AggregationKey(row._1, row._2, row._3, row._4))
  }

  /**
   * TODO: transform data into common tuple format for storage
   */
  def saveDataMap(data: (AggregationKey, Long)): (String, String, String, String, Long) =
    (data._1.access, data._1.typ, data._1.category, data._1.direction, data._2)
}
