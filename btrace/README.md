# Acknowledgement

 - https://github.com/btraceio/btrace/wiki
 - https://gist.github.com/yulewei/53339ccced8837686895e3c9f45557cc#btrace-annotations
 - https://stackoverflow.com/questions/tagged/btrace
 - https://www.piotrnowicki.com/2012/05/btrace-a-simple-way-to-instrument-running-java-applications/
 - https://stackoverflow.com/questions/10638826/java-reflection-impact-of-setaccessibletrue?answertab=trending#tab-top
 - https://stackoverflow.com/questions/14520344/using-btrace-to-find-when-a-class-is-created-for-the-first-time
 - `FlinkWindow.log2` by [@dhayanithisarkin](https://github.com/dhayanithisarkin) shows how to access return value by an intermediate call.
 - Thanks to [@dhayanithisarkin](https://github.com/dhayanithisarkin) for introducing me to Btrace.


# Class.getMethod() v/s Class.getDeclaredMethod()

- `Class.getMethod()` looks into superclasses, returns only `public` methods.
- `Class.getDeclaredMethod()` looks into only the current class. But it can return non-public methods as well.
- Btrace uses `getDeclaredField()` instead of `getField()` method internally, and then calls `Field.setAccessible(true)`. 
- Analogously, `getDeclaredMethod()` followed by `Method.setAccessible(true)` should be used.