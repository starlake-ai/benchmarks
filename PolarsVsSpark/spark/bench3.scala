import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object bench3 {
  def read_csvs(spark: SparkSession, path: String): DataFrame =
    spark.read
      .option("recursiveFileLookup", "true")
      .option("header", "true")
      .csv(path)

  def read_parquet(spark: SparkSession, path: String): DataFrame =
    spark.read.parquet(path)

  def write_parquets(df: DataFrame, path: String): Unit =
    df.write.partitionBy("date").format("parquet").mode("overwrite").save(path)

  def calculate_metrics(df: DataFrame): DataFrame =
    df.groupBy(
      col("date"),
      year(col("date")).alias("year"),
      month(col("date")).alias("month"),
      dayofmonth(col("date")).alias("day"),
      col("model")
    ).agg(sum("failure").alias("failures"))

  def main(args: Array[String]): Unit = {
    val t1 = System.nanoTime()

    val read_path = "root/parquets/parquets"
    val spark = SparkSession.builder.getOrCreate()

    val raw_data = read_parquet(spark, read_path)
    val records_count = raw_data.filter("model == 'value'").count()
    val t2 = System.nanoTime()
    println(
      "Time to run Apache Spark Load parquet : " + (t2 - t1) / 1e9 + "s"
    )
    println(records_count)
    spark.stop()
  }
}
