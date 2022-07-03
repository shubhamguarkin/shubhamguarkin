echo "For detailed documentation, look at https://github.com/jvm-profiling-tools/async-profiler"
pid=$1
out_file=$2
async_profiler_home="/tmp/async-profiler-2.8-linux-x64";
$async_profiler_home/profiler.sh stop -f $out_file -t -i 1000000 $pid
sudo sysctl kernel.perf_event_paranoid=$(cat $async_profiler_home/perf_event_paranoid.txt);
sudo sysctl kernel.kptr_restrict=$(cat $async_profiler_home/kptr_restrict.txt);
rm $async_profiler_home/perf_event_paranoid.txt;
rm $async_profiler_home/kptr_restrict.txt;
unset -v pid out_file
