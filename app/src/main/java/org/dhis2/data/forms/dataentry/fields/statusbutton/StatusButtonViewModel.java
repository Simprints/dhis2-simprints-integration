package org.dhis2.data.forms.dataentry.fields.statusbutton;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

/**
 * @Author Ankit Bansal (ankit.bansal@autodesk.com)
 */
@AutoValue
public  abstract class StatusButtonViewModel extends FieldViewModel {

    public static StatusButtonViewModel create(String id, String label, Boolean mandatory, String value, String section, String description, ObjectStyle objectStyle) {
        return new AutoValue_StatusButtonViewModel(id, label, mandatory, value, section, null,
                true, null, null, null,description,objectStyle, null);
    }

    @Override
    public StatusButtonViewModel setMandatory() {
        return new AutoValue_StatusButtonViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public StatusButtonViewModel withError(@NonNull String error) {
        return new AutoValue_StatusButtonViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error,description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public StatusButtonViewModel withWarning(@NonNull String warning) {
        return new AutoValue_StatusButtonViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(),description(), objectStyle(), null);
    }

    @Nonnull
    @Override
    public StatusButtonViewModel withValue(String data) {
        return new AutoValue_StatusButtonViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(),description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public StatusButtonViewModel withEditMode(boolean isEditable) {
        return new AutoValue_StatusButtonViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(),description(), objectStyle(), null);
    }
}
