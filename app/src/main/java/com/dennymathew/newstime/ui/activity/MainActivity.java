package com.dennymathew.newstime.ui.activity;

import android.os.Bundle;

import com.dennymathew.newstime.R;
import com.dennymathew.newstime.base.activity.BaseActivity;
import com.dennymathew.newstime.ui.controller.NavigationController;

import javax.inject.Inject;

public class MainActivity extends BaseActivity {


    @Inject
    NavigationController navigationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getString(R.string.app_title));

        if (savedInstanceState == null) {
            navigationController.showHeadlines();
        }


    }
}
