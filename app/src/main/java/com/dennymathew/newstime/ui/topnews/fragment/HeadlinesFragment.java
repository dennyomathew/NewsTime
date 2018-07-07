package com.dennymathew.newstime.ui.topnews.fragment;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennymathew.newstime.R;
import com.dennymathew.newstime.binding.FragmentDataBindingComponent;
import com.dennymathew.newstime.data.model.NewsArticle;
import com.dennymathew.newstime.data.network.Resource;
import com.dennymathew.newstime.data.network.Status;
import com.dennymathew.newstime.databinding.HeadlinesFragmentBinding;
import com.dennymathew.newstime.ui.topnews.adapter.NewsListAdapter;
import com.dennymathew.newstime.util.AutoClearedValue;
import com.dennymathew.newstime.ui.topnews.viewmodel.HeadlinesViewModel;
import com.dennymathew.newstime.viewmodel.NewsViewModelFactory;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import timber.log.Timber;

public class HeadlinesFragment extends DaggerFragment {

    @Inject
    NewsViewModelFactory viewModelFactory;

    HeadlinesViewModel headlinesViewModel;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<HeadlinesFragmentBinding> binding;
    AutoClearedValue<NewsListAdapter> adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        headlinesViewModel = ViewModelProviders.of(this, viewModelFactory).get(HeadlinesViewModel.class);

        LiveData<Resource<List<NewsArticle>>> resourceLiveData = headlinesViewModel.getNewsArticles();

//        resourceLiveData.observe(this, newsResource -> {
//            //data = newsResource.data;
//            try {
//                if (newsResource != null && newsResource.data != null ) {
//                    List<NewsArticle> articles = newsResource.data;
//
//                    for (NewsArticle article : articles) {
//                        Timber.d(article.getDescription());
//                    }
//                }
//            } catch (Exception e) {
//                Timber.e(e);
//            }
//        });

        NewsListAdapter newsListAdapter = new NewsListAdapter(dataBindingComponent, null);
        this.adapter = new AutoClearedValue<>(this, newsListAdapter);
        binding.get().newsList.setAdapter(newsListAdapter);

        resourceLiveData.observe(this, newsResource -> {

            if (newsResource != null && newsResource.data != null && newsResource.status == Status.SUCCESS) {
                adapter.get().replace(newsResource.data);
            } else {
                //noinspection ConstantConditions
                adapter.get().replace(Collections.emptyList());
            }
        });

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        HeadlinesFragmentBinding dataBinding = DataBindingUtil.inflate(inflater,
                R.layout.headlines_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        return dataBinding.getRoot();
    }


    public static HeadlinesFragment newInstance() {
        Bundle args = new Bundle();
        HeadlinesFragment fragment = new HeadlinesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}
