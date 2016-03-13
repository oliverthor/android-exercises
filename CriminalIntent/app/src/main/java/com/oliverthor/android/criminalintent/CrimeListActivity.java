package com.oliverthor.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by Oliver on 3/12/2016.
 */
public class CrimeListActivity extends SingleFragmentActivity {

    protected Fragment createFragment()
    {
        return new CrimeListFragment();
    }
}
