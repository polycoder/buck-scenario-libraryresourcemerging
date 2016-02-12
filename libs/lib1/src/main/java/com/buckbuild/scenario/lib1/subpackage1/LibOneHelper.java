package com.buckbuild.scenario.lib1.subpackage1;


import android.content.Context;

import com.buckbuild.scenario.lib1.R;

public class LibOneHelper {
    public String getLibTwoResStringDirectly(Context context) {
        return context.getString(R.string.lib_two_value);
    }
}
