package org.dhis2.data.forms.dataentry.fields.status;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.Toast;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.StatusBinding;
import org.dhis2.utils.simprints.SimprintsHelper;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.dhis2.utils.Constants.SIMPRINTS_ENROLL_REQUEST;
import static org.dhis2.utils.Constants.SIMPRINTS_VERIFY_REQUEST;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
public class StatusHolder extends FormViewHolder {

    public enum ValueStatus {
        NOT_DONE,
        SUCCESS,
        FAILURE
    }

    private final FlowableProcessor<RowAction> processor;

    private String guid = null;

    private StatusBinding binding;

    public StatusHolder(StatusBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.binding = binding;
        this.processor = processor;

        binding.tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSimprintsForVerification();
            }
        });
    }

    public void update(@NonNull FieldViewModel model) {
        fieldUid = model.uid();
        guid = model.value();


        if(((StatusViewModel)model).status().equals(ValueStatus.SUCCESS)){
            binding.setVerifyStatus(true);
            binding.tryAgainButton.setVisibility(GONE);
        }else if(((StatusViewModel)model).status().equals(ValueStatus.FAILURE)){
            binding.setVerifyStatus(false);
            binding.tryAgainButton.setVisibility(VISIBLE);
        }else{
            binding.setVerifyStatus(false);
            binding.tryAgainButton.setVisibility(GONE);
        }
    }



    private void launchSimprintsForVerification() {
        //Launch Simprints App Intent - ProjectId, UserId, ModuleId.
        if(guid ==  null){
            Timber.i("Simprints Verification - Guid is Null - Please check again!");
            return;
        }
        Intent simIntent = SimprintsHelper.getInstance().simHelper.verify("MODULE ID", guid);

        try {
            ((Activity)binding.rootView.getContext()).startActivityForResult(simIntent, SIMPRINTS_VERIFY_REQUEST);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(binding.rootView.getContext(), "Please download simprints app!", Toast.LENGTH_SHORT).show();
        }
    }
}
