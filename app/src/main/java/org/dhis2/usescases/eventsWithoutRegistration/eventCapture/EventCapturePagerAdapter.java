package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePagerAdapter extends FragmentStatePagerAdapter {

    private final Context context;
    private final String programUid;
    private final String eventUid;
    private final String biometricsGuid;
    private final int biometricsVerificationStatus;

    public EventCapturePagerAdapter(FragmentManager fm, Context context,String programUid, String eventUid) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
        this.programUid = programUid;
        this.eventUid = eventUid;
        biometricsGuid = null;
        biometricsVerificationStatus = -1;
    }

    public EventCapturePagerAdapter(FragmentManager fm, Context context,String programUid, String eventUid, String guid, int status) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
        this.programUid = programUid;
        this.eventUid = eventUid;
        this.biometricsGuid = guid;
        this.biometricsVerificationStatus = status;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return EventCaptureFormFragment.newInstance(eventUid, biometricsGuid, biometricsVerificationStatus);
            case 1:
                return NotesFragment.newEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getCount() {
        return 2; //TODO: ADD OVERVIEW, INDICATORS
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return context.getString(R.string.event_overview);
            case 1:
                return context.getString(R.string.event_notes);
        }
    }
}
