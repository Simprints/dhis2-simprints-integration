package org.dhis2.data.forms.dataentry.fields.statusbutton;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.Toast;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.StatusButtonBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.simprints.SimprintsHelper;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.processors.FlowableProcessor;

import static android.view.View.VISIBLE;
import static org.dhis2.utils.Constants.SIMPRINTS_ENROLL_REQUEST;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
public class StatusButtonHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    private StatusButtonBinding binding;

    public StatusButtonHolder(StatusButtonBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.binding = binding;
        this.processor = processor;

        binding.biometricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSimprints();
            }
        });
    }

    public void update(@NonNull FieldViewModel model) {
        fieldUid = model.uid();
        String value = model.value();

        if(value !=null && value.length() > 0){
            if(value.equalsIgnoreCase(Constants.BIOMETRICS_FAILURE_PATTERN)){
                onFailure();
            }else {
                onSuccess();
            }
        }else {
            onInitial();
        }
    }


    void onInitial(){
        binding.biometricsStatus.setVisibility(View.GONE);

        binding.biometricsButton.setVisibility(VISIBLE);
    }

    void onSuccess(){
        binding.biometricsStatus.setBackgroundColor(binding.rootView.getContext().getResources().getColor(R.color.green_7ed));
        binding.biometricsStatus.setText("BIOMETRICS COMPLETED");
        binding.biometricsStatus.setVisibility(VISIBLE);

        binding.biometricsButton.setVisibility(View.GONE);
    }

    void onFailure(){
        binding.biometricsStatus.setBackgroundColor(binding.rootView.getContext().getResources().getColor(R.color.red_060));
        binding.biometricsStatus.setText("BIOMETRICS DECLINED");
        binding.biometricsStatus.setVisibility(VISIBLE);

        binding.biometricsButton.setText("TRY AGAIN");
        binding.biometricsButton.setBackgroundColor(binding.rootView.getContext().getResources().getColor(R.color.gray_979));
    }


    private void launchSimprints() {
        //Launch Simprints App Intent - ProjectId, UserId, ModuleId.
        Intent intent = SimprintsHelper.getInstance().simHelper.register("Module ID");

        try {
            ((Activity)binding.rootView.getContext()).startActivityForResult(intent, SIMPRINTS_ENROLL_REQUEST);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(binding.rootView.getContext(), "Please download simprints app!", Toast.LENGTH_SHORT).show();
        }
    }
}
