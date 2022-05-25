### Fixes JIRA:

[CYG-78416](https://jira.eng.vmware.com/browse/CYG-78416)


### Summary


- On all nodes:
```
cd /tmp/
wget https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/flink-samza-handoff.sh -O /tmp/flink-samza-handoff.sh
sudo chown ubuntu:ubuntu /tmp/flink-samza-handoff.sh 
sudo chmod 770 /tmp/flink-samza-handoff.sh
bash /tmp/flink-samza-handoff.sh 
```





### Areas of Impact:

* `Smaza` offset lag just after upgrade should be less
### Testing


* Setup tested on `10.79.199.192`
   * handoff happening

