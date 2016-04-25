package com.oliverthor.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.oliverthor.android.criminalintent.CustomAction.DoneOnEditorActionListener;

//import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by olivernelson on 12/1/15.
 */
public class CrimeFragment extends Fragment {

    // widgets
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mRemoveButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;

    // consts
    //private static final String CRIME_DATE_FORMAT = "EEEE, MMM d yyyy";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;

    private static final String TAG = CrimeFragment.class.getSimpleName();

    private String mContactId = null;
    private boolean mAvailableContactsApp = true; // should be a better way to do this

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });
        mTitleField.setOnEditorActionListener(new DoneOnEditorActionListener());

        mDateButton = (Button) v.findViewById(R.id.crime_date);


        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }

        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mRemoveButton = (Button) v.findViewById(R.id.crime_remove);
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();

            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder ib = ShareCompat.IntentBuilder.from(getActivity());
                ib.setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .setChooserTitle(R.string.send_report);

//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT,
//                        getString(R.string.crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));

                //startActivity(ib.getIntent());
                ib.startChooser();
            }
        });

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkContactsPermission();
            }
        });

        mCallSuspectButton = (Button) v.findViewById(R.id.crime_suspect_call);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSuspect();
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());

            if (mCrime.getSuspectNumber() != null) {
                mCallSuspectButton.setText(mCrime.getSuspectNumber());
            }
        }

        Intent pickContact = new Intent(Intent.ACTION_PICK);
        if (!isAppAvailableForIntent(pickContact)) {
            mSuspectButton.setEnabled(false);

            Intent callContact = new Intent(Intent.ACTION_DIAL);
            if (!isAppAvailableForIntent(callContact)) {
                mCallSuspectButton.setEnabled(false);
            }
        }

        return v;

    }

    private void callSuspect() {

        if (mCrime.getSuspectNumber() != null) {

            Uri number = Uri.parse("tel:" + mCrime.getSuspectNumber());
            Intent callSuspect = new Intent(Intent.ACTION_DIAL);
            callSuspect.setData(number);
            startActivity(callSuspect);
        } else { // no available suspect number found

            Toast.makeText(getContext(), "No suspect number found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onContactsPermission() {

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        if (isAppAvailableForIntent(pickContact)) {
            startActivityForResult(pickContact, REQUEST_CONTACT);
        }
    }

    private boolean isAppAvailableForIntent(Intent intentAction) {
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(intentAction,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);

            updateDate();

        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            retrieveContactBasic(contactUri);

            Log.d(TAG, "Contacts Permission Check");


        }
    }

    private void checkContactsPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.

                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        } else { // user has permission


            onContactsPermission();
        }
    }

    private void retrieveContactBasic(Uri contactUri) {

        String[] queryFields = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID
        };
        // Perform your query - the contactUri is like a "where"
        // clause here.
        Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, null, null, null);

        try {
            // Double-check that you actually got results
            if (c.getCount() == 0) {
                return;
            }

            // Pull out the first column of the first row of data -
            // that is your suspect's name.
            c.moveToFirst();
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);

            mContactId = c.getString(1);
            Log.d(TAG, "Contact ID: " + mContactId);

            retrieveContactNumber();

        } finally {
            c.close();
        }
    }

    private void retrieveContactNumber() {

        String[] queryNumber = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cNumber = getActivity().getContentResolver()
                .query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // uri
                        queryNumber, // projection

                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                        new String[]{mContactId}, // selectionArgs
                        null  // sortOrder
                );

        try {

            // Pull out the first column of the first row of data -
            // that should be out suspect's phone number
            String suspectNumber = null;
            if (cNumber.moveToFirst()) {

                suspectNumber = cNumber.getString(cNumber.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
            }

            if (suspectNumber != null) {
                mCrime.setSuspectNumber(suspectNumber);
                mCallSuspectButton.setText(suspectNumber);
            }

            Log.d(TAG, "Contact Phone Number: " + suspectNumber);

        } finally {
            cNumber.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "Contacts Permission Granted");
                    onContactsPermission();


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void updateDate() {
        //DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
        //String df = DateFormat.getBestDateTimePattern(Locale.US, "EEE, MMM d, yyyy h:mm a");
        String df = "EEE, MMM d, yyyy h:mm a";
        mDateButton.setText(DateFormat.format(df, mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

}
