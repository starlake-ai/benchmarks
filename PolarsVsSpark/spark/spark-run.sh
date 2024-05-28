export SPARK_HOME=$HOME/programs/spark 
PATH=$SPARK_HOME/bin:$PATH $SPARK_HOME/bin/spark-submit --jars bench-spark_2.12-0.1.0-SNAPSHOT.jar --class  bench1 bench1
