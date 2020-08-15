package org.dhis2.data.forms.dataentry.fields.status;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
@AutoValue
public  abstract class StatusViewModel extends FieldViewModel {

    @NonNull
    public abstract StatusHolder.ValueStatus status();

    public static StatusViewModel create(String id, String label, Boolean mandatory, String value, String section, String description, ObjectStyle objectStyle, StatusHolder.ValueStatus status) {
        return new AutoValue_StatusViewModel(id, label, mandatory, value, section, null,
                true, null, null, null,description,objectStyle, null, status);
    }

    @Override
    public StatusViewModel setMandatory() {
        return new AutoValue_StatusViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(), objectStyle(), null, status());
    }

    @NonNull
    @Override
    public StatusViewModel withError(@NonNull String error) {
        return new AutoValue_StatusViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error,description(), objectStyle(), null, status());
    }

    @NonNull
    @Override
    public StatusViewModel withWarning(@NonNull String warning) {
        return new AutoValue_StatusViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(),description(), objectStyle(), null, status());
    }

    @Nonnull
    @Override
    public StatusViewModel withValue(String data) {
        return new AutoValue_StatusViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(),description(), objectStyle(), null, status());
    }

    @NonNull
    @Override
    public StatusViewModel withEditMode(boolean isEditable) {
        return new AutoValue_StatusViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(),description(), objectStyle(), null, status());
    }
}
