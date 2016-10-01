package com.oliverthor.android.criminalintent;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Oliver on 4/24/2016.
 */
public class ZoomedImageFragment extends DialogFragment {

    private TextView mCrimeTitle;
    private ImageView mZoomedImage;
    private File mPhotoFile;

    private static Crime mCrime;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater
                .inflate(R.layout.dialog_zoomed_image, null);

        mCrimeTitle = (TextView) v.findViewById(R.id.crime_zoomed_title);
        mCrimeTitle.setText(mCrime.getTitle());

        mZoomedImage = (ImageView) v.findViewById(R.id.crime_zoomed_image);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        updatePhotoView();

        return v;
    }

    public static ZoomedImageFragment newInstance(Crime crime) {

        Bundle args = new Bundle();

        mCrime = crime;

        ZoomedImageFragment fragment = new ZoomedImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mZoomedImage.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mZoomedImage.setImageBitmap(bitmap);
        }
    }
}
