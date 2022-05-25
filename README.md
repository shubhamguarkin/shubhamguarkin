### Fixes JIRA:

[CYG-78416](https://jira.eng.vmware.com/browse/CYG-78416)


### Summary


- On all nodes:
```
cd /tmp; wget  https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/flink-samza-handoff.sh -O- -q | bash -s
```





### Areas of Impact:

* `Smaza` offset lag just after upgrade should be less
### Testing


* Setup tested on `10.79.199.192`
   * handoff happening
   * Flink is shut down
   * Sample test output:
```
KAFKA_HOME='/home/ubuntu/deploy/kafka'
ZK_HOSTS=$(grep 'zookeeper.connect=' $KAFKA_HOME/config/consumer.properties | cut -d'=' -f2)
BROKER=localhost:9092
bash $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server $BROKER  --topic __samza_checkpoint_ver_1_for_job4_DIZ0QM2 --from-beginning
.......
{"SystemStreamPartition [kafka, Topic3, 10]":{"system":"kafka","partition":"10","offset":"69382","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 4]":{"system":"kafka","partition":"4","offset":"48985","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 17]":{"system":"kafka","partition":"17","offset":"46065","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 1]":{"system":"kafka","partition":"1","offset":"101076","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 5]":{"system":"kafka","partition":"5","offset":"74537","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 2]":{"system":"kafka","partition":"2","offset":"58577","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 8]":{"system":"kafka","partition":"8","offset":"73865","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 15]":{"system":"kafka","partition":"15","offset":"82865","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 6]":{"system":"kafka","partition":"6","offset":"80173","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 16]":{"system":"kafka","partition":"16","offset":"63409","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 3]":{"system":"kafka","partition":"3","offset":"47942","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 12]":{"system":"kafka","partition":"12","offset":"61931","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 13]":{"system":"kafka","partition":"13","offset":"61268","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 0]":{"system":"kafka","partition":"0","offset":"31552","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 7]":{"system":"kafka","partition":"7","offset":"54956","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 11]":{"system":"kafka","partition":"11","offset":"54736","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 14]":{"system":"kafka","partition":"14","offset":"47398","stream":"Topic3"}}
{"SystemStreamPartition [kafka, Topic3, 9]":{"system":"kafka","partition":"9","offset":"38403","stream":"Topic3"}}
^CProcessed a total of 162 messages
```

