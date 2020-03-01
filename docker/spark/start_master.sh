#!/bin/sh
./spark/bin/spark-class org.apache.spark.deploy.master.Master --ip $SPARK_LOCAL_IP --port $SPARK_MASTER_PORT --webui-port $SPARK_MASTER_WEBUI_PORT
