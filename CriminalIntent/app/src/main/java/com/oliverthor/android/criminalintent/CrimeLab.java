package com.oliverthor.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Oliver on 3/12/2016.
 *
 * CrimeLab is a singleton class that functions as the owner of all Crimes.
 */
public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;

    public static CrimeLab get(Context context)
    {
        if (sCrimeLab == null)
        {
            sCrimeLab = new CrimeLab(context);
        }

        return sCrimeLab;
    }

    private CrimeLab(Context context)
    {

        mCrimes = new ArrayList<>();
//        for (int i = 0; i < 100; i++)
//        {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0); // Every other one
//            mCrimes.add(crime);
//        }
    }
    
    public List<Crime> getCrimes()
    {
        return mCrimes;
    }

    public Crime getCrime(UUID crimeID)
    {
        for (Crime c : mCrimes)
        {
            if (c.getId().equals(crimeID))
            {
                return c;
            }
        }

        return null;
    }

    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    public void removeCrime(Crime c) {
        mCrimes.remove(c);
    }
}
