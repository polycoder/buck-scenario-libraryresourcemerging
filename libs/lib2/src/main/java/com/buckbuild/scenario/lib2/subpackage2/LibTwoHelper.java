package com.buckbuild.scenario.lib2.subpackage2;

import android.content.Context;

import com.buckbuild.scenario.lib2.R;

public class LibTwoHelper {
    public String getResString(Context context) {
        return context.getString(R.string.lib_two_value);
    }
}
