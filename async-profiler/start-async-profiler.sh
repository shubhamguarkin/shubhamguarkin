echo "For detailed documentation, look at https://github.com/jvm-profiling-tools/async-profiler"
pid=$1
out_file=$2
async_profiler_home="/tmp/async-profiler-2.8-linux-x64";
echo $(sysctl kernel.kptr_restrict | cut -d' ' -f3) > $async_profiler_home/kptr_restrict.txt;
echo $(sysctl kernel.perf_event_paranoid | cut -d' ' -f3) > $async_profiler_home/perf_event_paranoid.txt;
sudo sysctl kernel.perf_event_paranoid=1;
sudo sysctl kernel.kptr_restrict=0;
sudo $async_profiler_home/profiler.sh start -f $out_file -t -i 1000000 $pid;
echo Use "sudo $async_profiler_home/profiler.sh status $pid" to check status
echo Use "sudo $async_profiler_home/profiler.sh dump -f $out_file -t -i 1000000 $pid" to get partial output
unset -v pid out_file;
