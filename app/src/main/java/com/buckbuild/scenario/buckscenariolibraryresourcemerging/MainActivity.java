package com.buckbuild.scenario.buckscenariolibraryresourcemerging;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.buckbuild.scenario.lib1.subpackage1.LibOneHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LibOneHelper libOneHelper = new LibOneHelper();
        TextView libOneTextView = (TextView) findViewById(R.id.text_view_lib_1);
        libOneTextView.setText(libOneHelper.getLibTwoResStringDirectly(this));
    }
}
