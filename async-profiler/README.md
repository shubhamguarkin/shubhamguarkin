## Sample Output
![image](https://user-images.githubusercontent.com/50382330/177061710-bb8678e0-2a6e-4264-a5e3-d1e5e6e3fbc9.png)

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
