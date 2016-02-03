# Library resource dependency: "cannot find symbol"

In this scenario we have two libraries in the `libs` folder:

* `lib1`
* `lib2`

`lib1` depends on `lib2` for a resource, which it accesses directly via R. For example ([excerpt from this file](https://github.com/polycoder/buck-scenario-libraryresourcemerging/blob/master/libs/lib1/src/main/java/com/buckbuild/scenario/lib1/subpackage1/LibOneHelper.java)):

```
// lib_two_value comes from lib2 but is available via R in lib1 when using gradle
context.getString(R.string.lib_two_value)
```

When accessing a `lib2` resource like this and running `buck build //libs/lib1:lib`, we get the following output:

```
> buck build //libs/lib1:lib
[-] PROCESSING BUCK FILES...FINISHED 0.2s [100%]
[+] DOWNLOADING... (0.00 B/S, TOTAL: 0.00 B, 0 Artifacts)
[+] BUILDING...1.0s [50%] (4/5 JOBS, 0 UPDATED, 0.0% CACHE MISS)
 |=> //libs/lib1:lib...  0.5s (running javac[0.4s])
 |=> IDLE
 |=> IDLE
 |=> IDLE
 |=> IDLE
/Users/seank/repo/buck-tests/BuckScenarioLibraryResourceMerging/libs/lib1/src/main/java/com/buckbuild/scenario/lib1/subpackage1/LibOneHelper.java:14: error: cannot find symbol
        return context.getString(R.string.lib_two_value);
                                         ^
  symbol:   variable lib_two_value
  location: class com.buckbuild.scenario.lib1.R.string
Errors: 1. Warnings: 0.
```
However we did specify `lib2` as a dep of `lib1`:

```
android_library(
  name = "lib",
  srcs = glob(['src/main/java/**/*.java']),
  deps = [':res', '//libs/lib2:res', '//libs/lib2:lib'],
  visibility = ['PUBLIC']
)
```

# One way around this...

We have found an [undocumented flag](https://github.com/facebook/buck/commit/8852bda6c1700b62bc99b9a4d18b273380d605b6) in the buck source code that skirts this issue but leads to another one.
By setting the flag:

 ```
 resource_union_package = "com.buckbuild.scenario.lib1"
 ```

The previous error is not encountered when performing `buck build //libs/lib1:lib`

```
buck build //libs/lib1:lib
Not using buckd because watchman isn't installed.
[-] PROCESSING BUCK FILES...FINISHED 0.2s [100%]
[-] DOWNLOADING... (0.00 B/S AVG, TOTAL: 0.00 B, 0 Artifacts)
[-] BUILDING...FINISHED 0.5s [100%] (1/9 JOBS, 0 UPDATED, 0.0% CACHE MISS)
```

However this will still lead to a runtime exception when packaging `lib1` as a deps of the main app:

```
android_binary(
  name = 'app',
  manifest = 'src/main/AndroidManifest.xml',
  keystore = ':debug_keystore',
  deps =  [':lib'],
)

android_library(
  name = "lib",
  srcs = glob(['src/main/java/**/*.java']),
  deps = [':res', '//libs/lib1:lib'],
  visibility = ['PUBLIC']
)
```

Running `buck install //app:app` will work, but when the app starts it will throw an execption trying to find
the `lib2` resource when executing code from `lib1`:

```
E/AndroidRuntime( 2011): FATAL EXCEPTION: main
E/AndroidRuntime( 2011): Process: com.buckbuild.scenario.buckscenariolibraryresourcemerging, PID: 2011
E/AndroidRuntime( 2011): java.lang.NoSuchFieldError: No static field lib_two_value of type I in class Lcom/buckbuild/scenario/lib1/R$string; or its superclasses (declaration of 'com.buckbuild.scenario.lib1.R$string' appears in /data/app/com.buckbuild.scenario.buckscenariolibraryresourcemerging-1/base.apk)
E/AndroidRuntime( 2011): 	at com.buckbuild.scenario.lib1.subpackage1.LibOneHelper.getLibTwoString(LibOneHelper.java:10)
E/AndroidRuntime( 2011): 	at com.buckbuild.scenario.buckscenariolibraryresourcemerging.MainActivity.onCreate(MainActivity.java:18)
E/AndroidRuntime( 2011): 	at android.app.Activity.performCreate(Activity.java:5990)
E/AndroidRuntime( 2011): 	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1106)
E/AndroidRuntime( 2011): 	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2278)
E/AndroidRuntime( 2011): 	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2387)
E/AndroidRuntime( 2011): 	at android.app.ActivityThread.access$800(ActivityThread.java:151)
E/AndroidRuntime( 2011): 	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1303)
E/AndroidRuntime( 2011): 	at android.os.Handler.dispatchMessage(Handler.java:102)
E/AndroidRuntime( 2011): 	at android.os.Looper.loop(Looper.java:135)
E/AndroidRuntime( 2011): 	at android.app.ActivityThread.main(ActivityThread.java:5254)
E/AndroidRuntime( 2011): 	at java.lang.reflect.Method.invoke(Native Method)
E/AndroidRuntime( 2011): 	at java.lang.reflect.Method.invoke(Method.java:372)
E/AndroidRuntime( 2011): 	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:903)
E/AndroidRuntime( 2011): 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:698)
```

# Questions

Base on this experimental scenario, we have the following questions:

* Is the buck configuration not correct?
  * Is there another way to organize a lib1 -> lib2 res dependency like this?
* Does buck not provide the same lib1 -> lib2 resource merging as Gradle? Our current code relies heavily on this...