package com.dennymathew.newstime;

import android.app.Activity;
import android.app.Application;

import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by Denny on 3/30/2018.
 */

public class App extends Application implements HasActivityInjector {


    @Override
    public AndroidInjector<Activity> activityInjector() {
        return null;
    }
}
