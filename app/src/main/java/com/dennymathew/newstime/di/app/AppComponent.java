package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.App;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Denny on 3/30/2018.
 */

@Singleton
@Component(
        modules = {
                AppModule.class
        }
)
public interface AppComponent {

    @Component.Builder
    interface Builder {

        Builder application(App application);
        AppComponent build();
    }

    void inject(App app);
}
