package org.dhis2.data.forms.dataentry.fields.statusbutton;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.StatusButtonBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import io.reactivex.processors.FlowableProcessor;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
public class StatusButtonRow implements Row<StatusButtonHolder, StatusButtonViewModel> {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public StatusButtonRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }


    @NonNull
    @Override
    public StatusButtonHolder onCreate(@NonNull ViewGroup viewGroup) {
        StatusButtonBinding binding = DataBindingUtil.inflate(inflater, R.layout.status_button, viewGroup, false);
        return new StatusButtonHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull StatusButtonHolder viewHolder, @NonNull StatusButtonViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
