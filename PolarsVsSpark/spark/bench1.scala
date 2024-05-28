import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object bench1 {
  def read_csvs(spark: SparkSession, path: String): DataFrame =
    spark.read
      .option("recursiveFileLookup", "true")
      .option("header", "true")
      .csv(path)

  def write_parquets(df: DataFrame, path: String): Unit =
    df.write
      .partitionBy("date")
      .format("parquet")
      .mode("overwrite")
      .save(path)

  def calculate_metrics(df: DataFrame): DataFrame =
    df
      .groupBy(
        col("date"),
        year(col("date")).alias("year"),
        month(col("date")).alias("month"),
        dayofmonth(col("date")).alias("day"),
        col("model")
      )
      .agg(sum("failure").alias("failures"))

  def main(args: Array[String]): Unit = {
    val t1 = System.nanoTime()

    val read_path = "root/input/*.csv"
    val metrics_write_path = "root/parquets/parquets_metrics"
    val spark = SparkSession.builder.getOrCreate()

    val raw_data = read_csvs(spark, read_path)
    val selected_data = raw_data.select("date", "model", "failure")
    val metrics = calculate_metrics(selected_data)
    write_parquets(metrics, metrics_write_path)

    val t2 = System.nanoTime()
    println("Time to run Apache Spark pipeline : " + (t2 - t1) / 1e9 + "s")
    spark.stop()
  }
}
