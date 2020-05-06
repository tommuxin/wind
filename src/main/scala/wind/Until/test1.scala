package wind.Until

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
//import org.apache.spark.sql.SparkSession

object test1 {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    val conf = new SparkConf()
      .setAppName("WordCount1")
      // 设置yarn-client模式提交
      .setMaster("local")
    val spark = new SparkContext(conf)

    val yy = spark.parallelize(Array(1, 2, 3))

    val yy1 = yy.count()
    var yy2=(x:Int) =>{
      x*2
    }

    println(yy1)
    println(yy2(2))
    spark.stop()

  }

}
