package com.buckbuild.scenario.lib1.subpackage1;


import android.content.Context;

import com.buckbuild.scenario.lib1.R;

public class LibOneHelper {
    public String getLibTwoResStringDirectly(Context context) {
        // NOTES
        // - this will cause failure to build. But works when using gradle.
    	// - this will build in buck if "resource_union_package = com.buckbuild.scenario.lib1.subpackage1"
        //     - however, although it build the eventual android_binary built using the lib will encounter a runtime exception (the resource won't be found)
        return context.getString(R.string.lib_two_value);

        // NOTES
        // - this will work if "resource_union_package" is not set (Reference full path to resource..)
        // - this will however break the gradle build at runtime, and is not how our libraries are organized in general (heavy use of resource merging)
        // return context.getString(com.buckbuild.scenario.lib2.R.string.lib_two_value);
    }
}
