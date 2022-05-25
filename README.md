### Target: alpha


### Fixes JIRA:

[CYG-78416](https://jira.eng.vmware.com/browse/CYG-78416)


### Summary


- On all nodes:
    - Download https://github.com/shubhamguarkin/shubhamguarkin/blob/master/flink-samza-handoff.sh in `/tmp/`
    - Download https://github.com/shubhamguarkin/shubhamguarkin/blob/master/flink_to_samza_pre_upgrade_handoff.py in `/tmp/`
    - Run the following on all nodes:
```
sudo chown ubuntu:ubuntu /tmp/flink-samza-handoff.sh 
sudo chmod 770 /tmp/flink-samza-handoff.sh
/tmp/flink-samza-handoff.sh 
```





### Areas of Impact:

* `Smaza` offset lag just after upgrade should be less
### Testing


* Setup tested on `10.79.199.192`
   * handoff happening
   * Applying of `definiton_def` also unaffected by the script, although there is some unrelated issue
* BAT: TBD

