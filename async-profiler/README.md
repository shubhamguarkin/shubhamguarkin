## Download async profiler
```
wget https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/async-profiler/download.sh -O- -q | bash -s
```

## Start profiling
```
wget https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/async-profiler/start-async-profiler.sh -O- -q | bash -s <pid> <output file>
```

## Stop profiling
```
wget https://raw.githubusercontent.com/shubhamguarkin/shubhamguarkin/master/async-profiler/stop-async-profiler.sh -O- -q | bash -s <pid> <output file>
```

## Check status of profiler
```
sudo $async_profiler_home/profiler.sh status <pid>
```

## Take intermediate dump without terminating the profiler
```
sudo $async_profiler_home/profiler.sh dump -f <out_file> -t -i 1000000 <pid>
```
  
## Source
https://github.com/jvm-profiling-tools/async-profiler
