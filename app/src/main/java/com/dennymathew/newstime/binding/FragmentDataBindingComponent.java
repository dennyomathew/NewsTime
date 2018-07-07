package com.dennymathew.newstime.binding;

import android.databinding.DataBindingComponent;
import android.support.v4.app.Fragment;

/**
 * A Data Binding Component implementation for fragments.
 */
public class FragmentDataBindingComponent implements android.databinding.DataBindingComponent {
    private final FragmentBindingAdapters adapter;

    public FragmentDataBindingComponent(Fragment fragment) {
        this.adapter = new FragmentBindingAdapters(fragment);
    }

    @Override
    public FragmentBindingAdapters getFragmentBindingAdapters() {
        return adapter;
    }
}
