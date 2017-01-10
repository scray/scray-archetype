#!/bin/bash

MY_PATH_REL="`dirname \"$0\"`"
MY_FULL_PATH="`( cd \"$MY_PATH_REL\" && pwd )`"

ORIGDIR=$(pwd)

BASEDIR=$(dirname $(readlink -f $0))
cd $BASEDIR/..

function usage {
  echo -e "Usage: $0 <Options> <Job arguments>\n\
Options:\n\
  --spark-1.6.0-bin-hadoop2.6      Spark verson to download\n\
"
}

if [ ! -z $SPARK_HOME ]; then
 echo "WARN: SPARK_HOME variable ist set to $SPARK_HOME. This version will be used for spark-submit" 
fi

if [ ! -z $SPARK_SUBMIT ]; then
  echo "WARN: SPARK_SUBMIT variable is set to $SPARK_SUBMIT. This script will be used"
  usage
  exit 1
fi

ARGUMENTS=()
# find spark master argument
while [[ $# > 0 ]]; do
  if [[ $1 == "--spark-1.6.0-bin-hadoop2.6" ]]; then
    SPARK_BINARY_URL="http://d3kbcqa49mib13.cloudfront.net/spark-1.6.0-bin-hadoop2.6.tgz"
    shift 1 
  elif [[ $1 == "--help" ]]; then
    usage
    exit 0
  else
    ARGUMENTS+=("$1")
    shift 1
    exit 0
  fi
done


EXTRACTED_SPARK_FOLDER_NAME="spark-1.6.0-bin-hadoop2.6.tgz"

echo "SPARK_HOME="$MY_FULL_PATH/$EXTRACTED_SPARK_FOLDER_NAME
export SPARK_HOME=$MY_FULL_PATH/$EXTRACTED_SPARK_FOLDER_NAME

echo "YARN_CONF_DIR="$MY_FULL_PATH/../conf
export YARN_CONF_DIR=$MY_FULL_PATH/../conf


function downloadSparkBinaries {
	echo $SPARK_BINARY_URL
	wget $SPARK_BINARY_URL -O $MY_FULL_PATH/tmp_spark.tgz && mv $MY_FULL_PATH/tmp_spark.tgz $MY_FULL_PATH/spark.tgz
	tar -xvzf $MY_FULL_PATH/spark.tgz
}

if [ ! -e "$MY_FULL_PATH/spark.tgz" ]
then
	downloadSparkBinaries  
else 
	echo "Spark already exists. No download"
fi
