package com.oliverthor.android.nerdlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by olivernelson on 10/1/16.
 */
public class NerdLauncherFragment extends Fragment {
    private static final String TAG = "NerdLauncherFragment";

    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_nerd_launcher_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();
        return v;
    }

    private void setupAdapter() {

        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo a, ResolveInfo b) {
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(
                        a.loadLabel(pm).toString(),
                        b.loadLabel(pm).toString());
            }
        });

        Log.i(TAG, "Found " + activities.size() + " activities.");
        mRecyclerView.setAdapter(new ActivityAdapter(activities));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;
        private ImageView mAppImage;
        private TextView mAppName;

        public ActivityHolder (View itemView) {
            super(itemView);
//            mNameTextView = (TextView) itemView;
            View row = itemView;
            mAppImage = (ImageView) row
                    .findViewById(R.id.image_app_icon_activity);
            mAppName = (TextView) row
                    .findViewById(R.id.text_app_name);
            mAppName.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = resolveInfo.loadLabel(pm).toString();
//            mNameTextView.setText(appName);
            mAppName.setText(appName);
            mAppImage.setImageDrawable(resolveInfo.activityInfo.loadIcon(pm));
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;

            Intent i = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName,
                            activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {

        private final List<ResolveInfo> mActivities;

        public ActivityAdapter(List<ResolveInfo> activities) {
            mActivities = activities;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View row = layoutInflater
                    .inflate(R.layout.list_row_activity_holder, parent, false);

//            mAppImage = (ImageView)
//                    row.findViewById(R.id.image_app_icon_activity);
//            mAppName = (TextView)
//                    row.findViewById(R.id.text_app_name);
//            View view = layoutInflater
//                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(row);
        }

        @Override
        public void onBindViewHolder(ActivityHolder activityHolder, int position) {
            ResolveInfo resolveInfo = mActivities.get(position);
//            mAppName.setText(resolveInfo.activityInfo.name);

            activityHolder.bindActivity(resolveInfo);

        }

        @Override
        public int getItemCount() {
            return mActivities.size();
        }
    }
}
