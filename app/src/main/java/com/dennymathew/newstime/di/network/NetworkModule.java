package com.dennymathew.newstime.di.network;

import android.app.Application;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Denny on 3/31/2018.
 */

@Module
public class NetworkModule {

    private final static String NEWS_API_URL = "https://newsapi.org/v2/top-headlines";

    @Provides
    @Singleton
    Gson providesGson() {
        return new GsonBuilder().
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor providesHttpLogging() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    Cache providesOkHttpCache(Application app) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        return new Cache(app.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    OkHttpClient.Builder providesOkHttpClient(HttpLoggingInterceptor logger, Cache cache) {
        return new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        request = request.newBuilder()
                                .addHeader("Accept-Language", Locale.getDefault().getLanguage())
                                .build();

                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logger);
    }

    @Provides
    @Singleton
    public Retrofit providesRetrofit(Gson gson, OkHttpClient okHttpClient) {

        return new Retrofit.Builder()
                .baseUrl(NEWS_API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

    }

}
