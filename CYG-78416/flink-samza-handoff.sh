#!/usr/bin/env bash

# To be run manually
SCRIPT_PATH="/home/ubuntu/build-target/deploymentmanager/cluster/roles/flink_to_samza_pre_upgrade_handoff.py"
wget https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/flink_to_samza_pre_upgrade_handoff.py -O $SCRIPT_PATH
PYTHON__PATH="/home/ubuntu/build-target/deploymentmanager/"
OUT_FILE="/home/ubuntu/logs/handoff.log"
sudo mv /tmp/flink_to_samza_pre_upgrade_handoff.py $SCRIPT_PATH 2>/dev/null
sudo chown ubuntu:ubuntu $SCRIPT_PATH
sudo chmod 770 $SCRIPT_PATH
cd $PYTHON__PATH || exit
rm -rf $OUT_FILE
python3 -m cluster.roles.flink_to_samza_pre_upgrade_handoff "$@" > $OUT_FILE 2>&1
status=$?
[[ $status -eq 0 ]] && echo "handoff successful" || echo "handoff unsuccessful. Please check ${OUT_FILE}"
rm -rf $SCRIPT_PATH
