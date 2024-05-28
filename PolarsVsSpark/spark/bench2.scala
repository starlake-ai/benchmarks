import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object bench2 {
  def read_csvs(spark: SparkSession, path: String): DataFrame =
    spark.read
      .option("recursiveFileLookup", "true")
      .option("header", "true")
      .csv(path)

  def write_parquets(df: DataFrame, path: String): Unit =
    df.write.format("parquet").mode("overwrite").save(path)

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

    val read_path = "root/input/*.csv"
    val write_path = "root/parquets/parquets"
    val spark = SparkSession.builder.getOrCreate()

    val raw_data = read_csvs(spark, read_path)
    write_parquets(raw_data, write_path)

    val t2 = System.nanoTime()
    println(
      "Time to run Apache Spark Load csv and save parquet : " + (t2 - t1) / 1e9 + "s"
    )
    spark.stop()
  }
}
