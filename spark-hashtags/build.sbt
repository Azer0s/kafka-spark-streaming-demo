name := "spark-hashtags"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.3"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.2.3"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.3"
libraryDependencies += "org.apache.kafka" %% "kafka" % "0.10.2.2"
libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "4.0.0"
