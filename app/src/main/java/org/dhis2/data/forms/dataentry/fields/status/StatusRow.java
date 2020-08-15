package org.dhis2.data.forms.dataentry.fields.status;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.StatusBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import io.reactivex.processors.FlowableProcessor;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
public class StatusRow implements Row<StatusHolder, StatusViewModel> {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public StatusRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }


    @NonNull
    @Override
    public StatusHolder onCreate(@NonNull ViewGroup viewGroup) {
        StatusBinding binding = DataBindingUtil.inflate(inflater, R.layout.status, viewGroup, false);
        return new StatusHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull StatusHolder viewHolder, @NonNull StatusViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
