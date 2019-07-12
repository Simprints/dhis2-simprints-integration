package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class DataValueRepositoryImpl implements DataValueRepository {

    private final D2 d2;
    private BriteDatabase briteDatabase;
    private String dataSetUid;

    private String SELECT_APPROVAL = "SELECT * FROM DataApproval WHERE organisationUnit = ? and period = ? " +
            "and attributeOptionCombo = ? and state = 'APPROVED_HERE'";

    public DataValueRepositoryImpl(D2 d2, BriteDatabase briteDatabase, String dataSetUid) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<Period> getPeriod(String periodId) {
        return Flowable.fromCallable(() -> d2.periodModule().periods.byPeriodId().eq(periodId).one().get());
    }

    @Override
    public Flowable<List<DataInputPeriod>> getDataInputPeriod() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.withDataInputPeriods().byUid().eq(dataSetUid).one().get().dataInputPeriods());
    }

    @Override
    public Flowable<Map<String, List<String>>> getCategoryOptionComboCatOption() {

        Map<String, List<String>> map = new HashMap<>();
        return Flowable.fromCallable(() ->{
                List<DataSetElement> override = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().get().dataSetElements();

                for(DataSetElement dataSetElement: override){
                    DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().get();
                    DataElement dElement = transformDataElement(dataElement, override);
                    List<String> catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.withCategoryOptions().byCategoryComboUid().eq(dElement.categoryComboUid()).one().get().categoryOptions());

                    if (map.containsKey(dElement.uid())) {
                        map.get(dElement.uid()).addAll(catOptions);
                    } else {
                        map.put(dElement.uid(), catOptions);
                    }
                }

                return map;
        } );

    }

    @Override
    public Flowable<List<DataElement>> getDataElements(String sectionName) {
        if (!sectionName.equals("NO_SECTION"))
            return Flowable.fromCallable(() -> {
                List<String> dataElementsUid = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get().dataElements());
                List<DataElement> transformedDataElements = new ArrayList<>();
                List<DataElement> dataElements = d2.dataElementModule().dataElements.withAllChildren().byUid().in(dataElementsUid).orderByName(RepositoryScope.OrderByDirection.ASC).get();
                for (DataElement dataElement : dataElements) {
                    transformedDataElements.add(transformDataElement(dataElement, d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements()));
                }
                return transformedDataElements;
            });

        return Flowable.fromCallable(() -> {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements();
            List<DataElement> transformedDataElements = new ArrayList<>();
            List<String> uids = new ArrayList<>();
            for (DataSetElement dataSetElement : dataSetElements)
                uids.add(dataSetElement.dataElement().uid());

            List<DataElement> dataElements = d2.dataElementModule().dataElements.byUid().in(uids).orderByName(RepositoryScope.OrderByDirection.ASC).get();

            for (DataElement dataElement : dataElements) {
                transformedDataElements.add(transformDataElement(dataElement, dataSetElements));
            }
            return transformedDataElements;
        });
    }

    public Flowable<List<CategoryCombo>> getCatCombo(String sectionName) {
        if (!sectionName.equals("NO_SECTION"))
            return Flowable.fromCallable(() -> {
                List<String> dataElementUids = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get().dataElements());
                List<DataElement> dataElements = d2.dataElementModule().dataElements.withAllChildren().byUid().in(dataElementUids).orderByName(RepositoryScope.OrderByDirection.ASC).get();
                List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements();
                List<CategoryCombo> categoryCombos = new ArrayList<>();

                for (DataElement dataElement : dataElements) {
                    for (DataSetElement dataSetElement : dataSetElements) {
                        if (dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
                            categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataSetElement.categoryCombo().uid()).one().get());
                        else
                            categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataElement.categoryCombo().uid()).one().withAllChildren().get());
                    }
                }
                return categoryCombos;
            });

        return Flowable.fromCallable(() -> {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().get().dataSetElements();
            List<CategoryCombo> categoryCombos = new ArrayList<>();
            for (DataSetElement dataSetElement : dataSetElements) {
                if (dataSetElement.categoryCombo() != null)
                    categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataSetElement.categoryCombo().uid()).one().withAllChildren().get());
                else {
                    DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().get();
                    categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataElement.categoryCombo().uid()).one().withAllChildren().get());
                }
            }
            return categoryCombos;
        });
    }

    @Override
    public Flowable<DataSet> getDataSet() {

        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().get());

    }

    @Override
    public Flowable<Long> insertDataValue(DataValueModel dataValue) {
        return Flowable.just(briteDatabase.insert(DataValueModel.TABLE, dataValue.toContentValues()));
    }

    public Flowable<Integer> updateValue(DataValueModel dataValue) {
        String where = DataValueModel.Columns.DATA_ELEMENT + " = '" + dataValue.dataElement() + "' AND " + DataValueModel.Columns.PERIOD + " = '" + dataValue.period() +
                "' AND " + DataValueModel.Columns.ORGANISATION_UNIT + " = '" + dataValue.organisationUnit() +
                "' AND " + DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO + " = '" + dataValue.attributeOptionCombo() +
                "' AND " + DataValueModel.Columns.CATEGORY_OPTION_COMBO + " = '" + dataValue.categoryOptionCombo() + "'";

        if (dataValue.value() != null && !dataValue.value().isEmpty()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataValueModel.Columns.VALUE, dataValue.value());
            contentValues.put(DataValueModel.Columns.STATE, dataValue.state().name());
            contentValues.put(DataValueModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(dataValue.lastUpdated()));

            return Flowable.just(briteDatabase.update(DataValueModel.TABLE, contentValues, where));
        } else
            return Flowable.just(briteDatabase.delete(DataValueModel.TABLE, where));

    }

    @Override
    public Flowable<Map<String, List<CategoryOptionCombo>>> getCatOptionCombo() {

        return Flowable.fromCallable(() -> {
            Map<String, List<CategoryOptionCombo>> map = new HashMap<>();
            List<Section> sectionsList = d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).get();

            for (Section section : sectionsList) {
                List<DataElement> dataElements = section.dataElements();
                List<DataElement> dataElementOverrides = new ArrayList<>();

                List<DataSetElement> overrides = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements();
                for (DataElement dataElement : dataElements)
                    dataElementOverrides.add(transformDataElement(dataElement, overrides));

                if (map.get(section.name()) == null)
                    map.put(section.name(), new ArrayList<>());

                for (DataElement dataElement : dataElementOverrides) {
                    boolean exist = false;
                    List<CategoryOptionCombo> listCatOption = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(dataElement.categoryCombo().uid()).get();
                    for (CategoryOptionCombo catOptionCombo : listCatOption) {
                        for (CategoryOptionCombo catOptionComboMap : map.get(section.name())) {
                            if (catOptionComboMap.uid().equals(catOptionCombo.uid()))
                                exist = true;
                        }

                        if (!exist)
                            map.get(section.name()).add(catOptionCombo);
                    }
                }
            }
            return map;
        });
    }

    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String sectionName) {
        List<String> catCombos = new ArrayList<>();
        if (sectionName.equals("NO_SECTION"))
            return Flowable.fromCallable(() -> {
                List<String> dataElementUids = new ArrayList<>();
                List<DataElement> dataElements;
                for (DataSetElement dataSetElement : d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements())
                    dataElementUids.add(dataSetElement.dataElement().uid());
                dataElements = d2.dataElementModule().dataElements.withAllChildren().byUid().in(dataElementUids).orderByName(RepositoryScope.OrderByDirection.ASC).get();
                return getMap(catCombos, dataElements);
            });
        return Flowable.fromCallable(() -> {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements();
            List<DataElement> dataElements = new ArrayList<>();
            List<String> dataElementsUid = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get().dataElements());

            for (DataElement dataElement : d2.dataElementModule().dataElements.withAllChildren().byUid().in(dataElementsUid).orderByName(RepositoryScope.OrderByDirection.ASC).get()) {
                dataElements.add(transformDataElement(dataElement, dataSetElements));
            }
            return getMap(catCombos, dataElements);
        });
    }


    private Map<String, List<List<Pair<CategoryOption, Category>>>> getMap(List<String> catCombos, List<DataElement> dataElements) {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = new HashMap<>();
        for (DataElement dataElement : dataElements) {
            String catCombo = dataElement.categoryCombo().uid();
            if (!catCombos.contains(dataElement.categoryCombo().uid())) {
                catCombos.add(dataElement.categoryCombo().uid());

                List<Category> categories = d2.categoryModule().categoryCombos.withCategories().withAllChildren().byUid().eq(catCombo).one().get().categories();

                if (map.get(catCombo) == null) {
                    map.put(catCombo, new ArrayList<>());
                }

                for (Category category : categories) {
                    List<CategoryOption> catOptions = d2.categoryModule().categories.withCategoryOptions().byUid().eq(category.uid()).one().get().categoryOptions();
                    for (CategoryOption catOption : catOptions) {
                        if (map.get(catCombo).size() == 0) {
                            List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                            list.add(Pair.create(catOption, category));
                            map.get(catCombo).add(list);
                        } else {

                            if (map.get(catCombo).get(map.get(catCombo).size() - 1).get(0).val1().uid().equals(category.uid())) {
                                map.get(catCombo).get(map.get(catCombo).size() - 1).add(Pair.create(catOption, category));
                            } else {
                                List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                                list.add(Pair.create(catOption, category));
                                map.get(catCombo).add(list);
                            }

                        }
                    }
                }
            }
        }
        return map;
    }


    private DataElement transformDataElement(DataElement dataElement, List<DataSetElement> override) {
        for (DataSetElement dataSetElement : override)
            if (dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
                return DataElement.builder()
                        .uid(dataElement.uid())
                        .code(dataElement.code())
                        .name(dataElement.name())
                        .displayName(dataElement.displayName())
                        .shortName(dataElement.shortName())
                        .displayShortName(dataElement.displayShortName())
                        .description(dataElement.description())
                        .displayDescription(dataElement.displayDescription())
                        .valueType(dataElement.valueType())
                        .zeroIsSignificant(dataElement.zeroIsSignificant())
                        .aggregationType(dataElement.aggregationType())
                        .formName(dataElement.formName())
                        .domainType(dataElement.domainType())
                        .displayFormName(dataElement.displayFormName())
                        .optionSet(dataElement.optionSet())
                        .categoryCombo(dataSetElement.categoryCombo()).build();

        return dataElement;
    }

    @Override
    public Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String sectionName) {

        Map<String, String> mapDataElementCatCombo = new HashMap<>();

        return Flowable.just(d2.dataSetModule().dataSets.withSections().withDataSetElements().byUid().eq(dataSetUid).one().get())
                .flatMapIterable(dataSet -> {
                    List<DataSetElement> dataElements = new ArrayList<>();
                    if (!sectionName.equals("NO_SECTION")) {
                        List<DataElement> dataElementSection = d2.dataSetModule().sections.withDataElements().byName().eq(sectionName).one().get().dataElements();
                        for (DataElement dataElement : dataElementSection) {
                            for (DataSetElement dataSetElement : dataSet.dataSetElements())
                                if (dataSetElement.dataElement().uid().equals(dataElement.uid()))
                                    dataElements.add(dataSetElement);
                        }
                    } else
                        dataElements = dataSet.dataSetElements();

                    return dataElements;
                })
                .flatMapIterable(dataSetElement -> {
                            if (dataSetElement.categoryCombo() != null)
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(), dataSetElement.categoryCombo().uid());
                            else
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(),
                                        d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().get().categoryCombo().uid());

                            return d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid())
                                    .byAttributeOptionComboUid().eq(catOptionComb)
                                    .byPeriod().eq(initPeriodType)
                                    .byOrganisationUnitUid().eq(orgUnitUid)
                                    .get();
                        }
                ).map(dataValue -> {
                    List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptionCombos.withCategoryOptions()
                            .byUid().eq(dataValue.categoryOptionCombo()).one().get().categoryOptions();
                    List<String> uidCatOptions = new ArrayList<>();
                    for (CategoryOption catOption : categoryOptions)
                        uidCatOptions.add(catOption.uid());

                    return DataSetTableModel.create(dataValue.id(), dataValue.dataElement(), dataValue.period(),
                            dataValue.organisationUnit(), dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                            dataValue.value(), dataValue.storedBy(), "",//no used anywhere, remove this field
                            uidCatOptions, mapDataElementCatCombo.get(dataValue.dataElement()));

                }).toList().toFlowable();
    }

    @Override
    public Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombos, String section) {

        Map<String, Map<String, List<String>>> mapData = new HashMap<>();

        return Flowable.fromCallable(() -> {
            List<DataElementOperand> operands;
            if(!section.isEmpty() && !section.equals("NO_SECTION")) {
                operands = d2.dataSetModule().sections.withAllChildren().byDataSetUid().eq(dataSetUid).byName().eq(section).one().get().greyedFields();

                for (DataElementOperand operand : operands) {
                    List<String> catOptions;
                    if (operand.categoryOptionCombo() == null) {
                        List<DataSetElement> override = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().get().dataSetElements();
                        DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(operand.dataElement().uid()).one().get();
                        DataElement dataElementOverride = transformDataElement(dataElement, override);

                        List<CategoryOptionCombo> catOptionCombos = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(dataElementOverride.categoryCombo().uid()).withCategoryOptions().get();
                        HashMap<String, List<String>> mapCatOptions = new HashMap<>();

                        for (CategoryOptionCombo catOptionCombo : catOptionCombos) {
                            mapCatOptions.put(catOptionCombo.uid(), UidsHelper.getUidsList(catOptionCombo.categoryOptions()));
                        }

                        mapData.put(operand.dataElement().uid(), mapCatOptions);
                    } else {

                        if (mapData.containsKey(operand.dataElement().uid())) {
                            catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.byUid().eq(operand.categoryOptionCombo().uid()).withCategoryOptions().one().get().categoryOptions());
                            mapData.get(operand.dataElement().uid()).put(operand.categoryOptionCombo().uid(), catOptions);
                        } else
                            catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.byUid().eq(operand.categoryOptionCombo().uid()).withCategoryOptions().one().get().categoryOptions());
                        HashMap<String, List<String>> mapCatOptions = new HashMap<>();
                        mapCatOptions.put(operand.categoryOptionCombo().uid(), catOptions);
                        mapData.put(operand.dataElement().uid(), mapCatOptions);
                    }
                }
            }

            return mapData;

        });

    }

    @Override
    public Flowable<Map<String, List<String>>> getMandatoryDataElement(List<String> categoryOptionCombo) {
        Map<String, List<String>> mapData = new HashMap<>();
        return Flowable.fromCallable(() -> {
            DataSet dataSet = d2.dataSetModule().dataSets.withCompulsoryDataElementOperands().withDataSetElements().byUid().eq(dataSetUid).one().get();
            for(DataElementOperand operand : dataSet.compulsoryDataElementOperands()){
                List<String> catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.withCategoryOptions().byUid().eq(operand.categoryOptionCombo().uid()).one().get().categoryOptions());
                if (mapData.containsKey(operand.dataElement().uid())) {
                    mapData.get(operand.dataElement().uid()).addAll(catOptions);
                } else {
                    mapData.put(operand.dataElement().uid(), catOptions);
                }
            }

            return  mapData;
        });

    }

    @Override
    public Flowable<Section> getSectionByDataSet(String section) {
        if(!section.isEmpty() && !section.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections.byDataSetUid().eq(dataSetUid).byName().eq(section).one().get());
        else
            return Flowable.just(Section.builder().uid("").build());

    }

    @Override
    public Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        boolean updateOrInserted;
        String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ?";

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataSetCompleteRegistration.Columns.STATE, State.TO_UPDATE.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put("date", completeDate);
        String[] values = {periodInitialDate, dataSetUid, catCombo};

        updateOrInserted = briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0;

        if (!updateOrInserted) {
            DataSetCompleteRegistration dataSetCompleteRegistration =
                    DataSetCompleteRegistration.builder().dataSet(dataSetUid)
                            .period(periodInitialDate)
                            .organisationUnit(orgUnitUid)
                            .attributeOptionCombo(catCombo)
                            .date(DateUtils.getInstance().getToday())
                            .state(State.TO_POST).build();

            updateOrInserted = briteDatabase.insert(DataSetCompleteRegistration.class.getSimpleName(), dataSetCompleteRegistration.toContentValues()) > 0;
        }

        return Flowable.just(updateOrInserted);

    }

    @Override
    public Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ? and organisationUnit = ? ";
        String[] values = {periodInitialDate, dataSetUid, catCombo, orgUnitUid};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataSetCompleteRegistration.Columns.STATE, State.TO_DELETE.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put("date", completeDate);

        return Flowable.just(briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0);
    }

    @Override
    public Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo) {

        return Flowable.fromCallable(() ->{
            DataSetCompleteRegistration completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catCombo)
                    .byPeriod().eq(periodInitialDate)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .one().get();
            if(completeRegistration != null)
                return true;
            else
                return  false;
        });
    }

    @Override
    public Flowable<Boolean> isApproval(String orgUnit, String period, String attributeOptionCombo){
        return briteDatabase.createQuery("DataApproval", SELECT_APPROVAL, orgUnit, period, attributeOptionCombo)
                .mapToOneOrDefault(data -> true, false)
                .toFlowable(BackpressureStrategy.LATEST);
    }
}
