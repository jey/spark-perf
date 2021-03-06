package spark.perf

import scala.util.Random
import scala.math
import spark.HashPartitioner
import spark.SparkContext
import spark.SparkContext._

object RandomStrings {
  def generateString(len: Int) = {
    //generates a more readable string than Random.nextString
    (0 until len).map (i => Random.nextPrintableChar) mkString("")
  }
  /**
    * Create an rdd of numPairs key-value pairs with approximately numKeys distinct keys
    * @param keyLen length of key
    * @param valueLen length of value
    * @param numPairs approximate total number of pairs
    * @param numKeys approximate number of distinct keys
    */
  def generatePairs(sc: SparkContext, keyLen: Int, valueLen: Int, numPairs: Int, 
    numKeys: Int, numPartitions: Int): spark.RDD[(String, String)] = {


    val keys = (0 until numKeys).map { i => generateString(keyLen) }.toArray
    val broadcastKeys = sc.broadcast(keys)

    val rdd = sc.parallelize(1 to numPairs, numPartitions)
    val pairsRdd = rdd.map { x => 
      (broadcastKeys.value(Random.nextInt(numKeys)), generateString(valueLen))
    }

    return pairsRdd
  }

  def main(args: Array[String]) {
    val sparkHome = System.getenv("SPARK_HOME")
    val jars = List(System.getenv("SPARK_PERF"))
    val sc = new SparkContext(args(0), "Random Strings", sparkHome, jars)
    val numPairs = args(1).toInt
    val numKeys = args(2).toInt
    val numPartitions = args(3).toInt
    val outputDir = args(4)
    generatePairs(sc, 10, 5, numPairs, numKeys, numPartitions).saveAsTextFile(outputDir)
    sc.stop()
  }
}
