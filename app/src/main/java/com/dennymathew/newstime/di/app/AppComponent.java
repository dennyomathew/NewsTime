package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.base.App;
import com.dennymathew.newstime.di.network.NewsServiceModule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Created by Denny on 3/30/2018.
 */

@Singleton
@Component(
    modules = {
        AndroidSupportInjectionModule.class,
            NewsServiceModule.class,
        ActivityBuilderModule.class,
        AppModule.class
    }
)
public interface AppComponent extends AndroidInjector<App> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<App> {}

}
