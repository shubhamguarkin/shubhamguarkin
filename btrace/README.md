# What is it?
A debugging tool for java processes.

# Setup
## Download Btrace 
```
mkdir -p /tmp/btrace;
cd /tmp/btrace;
wget https://github.com/btraceio/btrace/releases/download/v2.2.2/btrace-v2.2.2-bin.tar.gz;
# Reference: https://github.com/btraceio/btrace#using-btrace
```

## Run btrace script
```
BTRACE_HOME="/tmp/btrace/btrace_dep"
sudo chmod -R 777 /tmp/btrace
pid=$(sudo -u yarn jps | grep Task | cut -d' ' -f1) # For flink task manager
cp="/home/ubuntu/build-target/flinkjobs/engine-0.001-SNAPSHOT.jar" # For flink task manager
java_file="/tmp/btrace/src/main/java/btrace/TsdbSinkLatencies.java" # Point to your custom btrace script
sudo -E -u yarn $BTRACE_HOME/bin/btrace -u  -p 2022 -classpath $cp $pi $java_file > btrace.out &
```

# Acknowledgement

 - https://github.com/btraceio/btrace/wiki
 - https://gist.github.com/yulewei/53339ccced8837686895e3c9f45557cc#btrace-annotations
 - https://stackoverflow.com/questions/tagged/btrace
 - https://www.piotrnowicki.com/2012/05/btrace-a-simple-way-to-instrument-running-java-applications/
 - https://stackoverflow.com/questions/10638826/java-reflection-impact-of-setaccessibletrue?answertab=trending#tab-top
 - https://stackoverflow.com/questions/14520344/using-btrace-to-find-when-a-class-is-created-for-the-first-time
 - `FlinkWindow#log2` by [@dhayanithisarkin](https://github.com/dhayanithisarkin) shows how to access return value by an intermediate call.
 - Thanks to [@dhayanithisarkin](https://github.com/dhayanithisarkin) for introducing me to Btrace.

# Notes

## Class.getMethod() v/s Class.getDeclaredMethod()

- `Class.getMethod()` looks into superclasses, returns only `public` methods.
- `Class.getDeclaredMethod()` looks into only the current class. But it can return non-public methods as well.
- Btrace uses `getDeclaredField()` instead of `getField()` method internally, and then calls `Field.setAccessible(true)`. 
- Analogously, `getDeclaredMethod()` followed by `Method.setAccessible(true)` should be used.

## Others

- `btrace` command takes only one `.class`\\`.java` file without inner classes.
- It might be useful to test the btrace script locally. This can be done by running a unit test locally simulating the java process to which btrace script is intended to be attached.
- `-v` is the verbose option. But it prints some logs in production code aas well