package org.dhis2.utils.simprints;

import com.simprints.libsimprints.SimHelper;

public class SimprintsHelper {

    private static final String PROJECT_ID = "";
    private static final String USER_ID = "android";

    private static SimprintsHelper instance;

    public SimHelper simHelper = new SimHelper(PROJECT_ID, USER_ID);

    public static SimprintsHelper getInstance() {
        if (instance == null)
            instance = new SimprintsHelper();

        return instance;
    }
}
