#!/bin/bash

# List of ODroids"
ODROIDS="192.168.1.100
192.168.1.101
192.168.1.102
192.168.1.103
192.168.1.104
192.168.1.105
192.168.1.106
192.168.1.107
192.168.1.108
192.168.1.109
192.168.1.110
192.168.1.111
192.168.1.112
192.168.1.113
192.168.1.114
192.168.1.115
192.168.1.116"

# Deploy target name
LOCAL_TARGET_DIR="target"
LOCAL_TARGET_NAME="group3-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
DEPLOY_TARGET_DIR="group3"
DEPLOY_TARGET_NAME="p2pfs.jar"
DEPLOY_LOG_FILE="logfile.log"

echo "Starting to deploy $LOCAL_TARGET_DIR/$LOCAL_TARGET_NAME to ~/$DEPLOY_TARGET_DIR/$DEPLOY_TARGET_NAME"
echo "---------------------------------------------------------------------------------------------------"

echo "Creating  directory"
for entry in $ODROIDS; do
  ssh -i ~/.ssh/odroid root@$entry "mkdir -p ~/$DEPLOY_TARGET_DIR"
done
echo "Created $DEPLOY_TARGET_DIR dirs"

echo "Starting to deploy snapshot"
echo "---------------------------"
for entry in $ODROIDS; do
  echo "Killing already running jars"
  ssh -i ~/.ssh/odroid root@$entry "pkill -f 'java -jar'"
  echo "Creating $DEPLOY_TARGET_DIR"
  ssh -i ~/.ssh/odroid root@$entry "mkdir -p ~/$DEPLOY_TARGET_DIR"
  echo "Deploy $LOCAL_TARGET_DIR/$LOCAL_TARGET_NAME to ODroid with IP:  $entry"
  scp -i ~/.ssh/odroid $LOCAL_TARGET_DIR/$LOCAL_TARGET_NAME root@$entry:~/$DEPLOY_TARGET_DIR/$DEPLOY_TARGET_NAME
  echo "Remove Filesystem target dir and lofgile if exists"
  ssh -i ~/.ssh/odroid root@$entry "rm -rf ~/$DEPLOY_TARGET_DIR/P2PFS; rm ~/$DEPLOY_TARGET_DIR/logfile.log"
  echo "Execute jar file and write console output to $DEPLOY_LOG_FILE"
  ssh -i ~/.ssh/odroid root@$entry "cd ~/$DEPLOY_TARGET_DIR; nohup java -jar $DEPLOY_TARGET_NAME >> $DEPLOY_LOG_FILE &"

  sleep 5
done

echo "Deployed to all ODroids"
