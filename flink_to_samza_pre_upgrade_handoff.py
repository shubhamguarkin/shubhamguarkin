import logging
import sys
import time

import utils

FLINK_JOB_PROPERTIES_PATH = '/home/ubuntu/build-target/flinkjobs/job4.properties'
SAMZA_NEW_OFFSETS = '/home/ubuntu/build-target/deployment/flink-current-offsets.txt'
SYSTEM_CUSTOMER_ID = 0


def handoff_offsets(job_properties_path=FLINK_JOB_PROPERTIES_PATH):
    logging.info('Using job properties path %s', job_properties_path)
    # checkpoint-tool reside under samzajobs tar. So untar samzajobs and then invoke checkpoint tool
    untar = "{0}/untar_samzajobs.sh {0} {0}/job4.properties.backup".format('/home/ubuntu/build-target/samzajobs')
    rc = run_local_cmd(untar)
    if rc:
        raise Exception('Failed to untar samza tar')
    logging.info("samzajobs untar is done")
    utils.delete_file(SAMZA_NEW_OFFSETS)
    # Want to update the offset of 2 topics : Topic3 and FastTopic
    flink_offsets = get_flink_current_offsets(3, ["Topic3", "FastTopic"], "vrniflink")
    result = ""
    # This is the format which is required for the file to be given in --new-offsets flag while calling checkpoint
    # script for overwriting offsets
    for offset_line in flink_offsets.splitlines():
        atoms = offset_line.split()
        result += "tasknames.SystemStreamPartition\ [kafka,\ {0},\ {1}].systems.kafka.streams.{0}.partitions.{1}={2}\n".format(
            atoms[0],  # TOPIC NAME
            atoms[1],  # Partition Id
            atoms[2])  # Current Offset
    utils.write_to_file(SAMZA_NEW_OFFSETS, result)
    cmd = "env base_dir='%s' %s --config %s --config %s --new-offsets %s" % (
        "/home/ubuntu/build-target/samzajobs/samza-jobs/",
        "/home/ubuntu/build-target/samzajobs/samza-jobs/bin/checkpoint-tool.sh",
        "job.config.loader.factory=org.apache.samza.config.loaders.PropertiesConfigLoaderFactory",
        ("job.config.loader.properties.path=%s" % job_properties_path),
        SAMZA_NEW_OFFSETS)
    logging.info("Executing cmd: %s", cmd)
    rc, out, err = utils.run_local_cmd(cmd, False)
    if rc:
        logging.error("stderr: %s" % err) if err else 0
        logging.error("stdout: %s" % out) if out else 0
        raise Exception("Failed to update offsets for Samza")


def get_flink_current_offsets(num_retries, topic, group_id):
    wait_for_retry = 1
    for i in range(0, num_retries):
        # OutPut Format: <topicName> <partitionId> <currentOffset>
        cmd = "%s --bootstrap-server  localhost:9092 --describe --group %s | grep '%s\|%s' | awk '{print $2, $3, $4}'" % (
            "/home/ubuntu/deploy/kafka/bin/kafka-consumer-groups.sh",
            group_id,
            topic[0],
            topic[1])

        logging.info("Executing cmd: %s", cmd)
        rc, out, err = utils.run_local_cmd(cmd, False)
        if rc is not None and rc != 0:
            error_msg = 'Failed to get current offsets from Flink'
            logging.error(error_msg)
            time.sleep(wait_for_retry + wait_for_retry * i)
            continue
        return out
    raise Exception("Failed to get current offsets from Flink")


def shutdown_flink():
    cmd = "sudo systemctl stop flinkjobs.service"
    rc = run_local_cmd(cmd)
    if rc:
        raise Exception('Failed to shutdown flink')
    logging.info("flink shutdown complete")


def unmask_samza():
    cmd = "sudo systemctl unmask samzajobs.service"
    rc = run_local_cmd(cmd)
    if rc:
        raise Exception('Failed to unmask samza')
    logging.info("samza unmasking complete")


def create_samza_dir_in_hdfs():
    cmd = "sudo -u hdfs hdfs dfs -mkdir -p /samza; sudo -u hdfs hdfs dfs -chown yarn /samza"
    rc = run_local_cmd(cmd)
    if rc:
        raise Exception('Failed to create samza dir in hdfs')
    logging.info("creating samza dir in hdfs complete")


def run_local_cmd(cmd):
    logging.info("Running command %s", cmd)
    rc, out, err = utils.run_local_cmd(cmd, False)
    logging.info("RC: %d", rc)
    logging.info("STDOUT: %s", out) if out else 1
    logging.info("STDERR: %s", err) if err else 1
    return rc


if __name__ == '__main__':
    logging.getLogger().setLevel(logging.INFO)
    shutdown_flink()
    if utils.isThisPlatformNode1():
        create_samza_dir_in_hdfs()
        if len(sys.argv) >= 2:
            handoff_offsets(sys.argv[1])
        else:
            handoff_offsets()
    else:
        logging.info('Skipping handoff and samza dir creation as not platform1')
    unmask_samza()

