package com.dennymathew.newstime.ui.controller;

import android.support.v4.app.FragmentManager;

import com.dennymathew.newstime.R;
import com.dennymathew.newstime.ui.activity.MainActivity;
import com.dennymathew.newstime.ui.topnews.fragment.HeadlinesFragment;

import javax.inject.Inject;

public class NavigationController {

    private final int containerId;
    private final FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void showHeadlines() {
        HeadlinesFragment headlinesFragment = new HeadlinesFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, headlinesFragment)
                .commitAllowingStateLoss();
    }
}
