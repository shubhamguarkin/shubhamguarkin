### Fixes JIRA:

[CYG-78416](https://jira.eng.vmware.com/browse/CYG-78416)


### Summary


- On all nodes:
```
wget  https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/flink-samza-handoff.sh -O- -q | bash -s
```





### Areas of Impact:

* `Smaza` offset lag just after upgrade should be less
### Testing


* Setup tested on `10.79.199.192`
   * handoff happening

