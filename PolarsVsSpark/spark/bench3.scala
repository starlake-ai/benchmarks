import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object bench3 {

  def read_parquet(spark: SparkSession, path: String): DataFrame =
    spark.read.parquet(path)

  def main(args: Array[String]): Unit = {
    val t1 = System.nanoTime()

    val read_path = "root/parquets/parquets"
    val spark = SparkSession.builder.getOrCreate()

    val raw_data = read_parquet(spark, read_path)
    val records_count = raw_data.filter("1 == 1").count()
    val t2 = System.nanoTime()
    println(
      "Time to run Apache Spark Load parquet : " + (t2 - t1) / 1e9 + "s"
    )
    println(records_count)
    spark.stop()
  }
}
