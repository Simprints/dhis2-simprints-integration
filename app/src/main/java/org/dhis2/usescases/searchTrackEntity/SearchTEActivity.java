package org.dhis2.usescases.searchTrackEntity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.RefusalForm;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeActivity;
import org.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.RelationshipLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.FileResourcesUtil;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.customviews.CoordinatesView;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.customviews.ScanTextView;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.maps.MapLayerDialog;
import org.dhis2.utils.maps.MapLayerManager;
import org.dhis2.utils.maps.MapboxExtensionKt;
import org.dhis2.utils.maps.MarkerUtils;
import org.dhis2.utils.simprints.SimprintsHelper;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.simprints.libsimprints.Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK;
import static com.simprints.libsimprints.Constants.SIMPRINTS_IDENTIFICATIONS;
import static com.simprints.libsimprints.Constants.SIMPRINTS_REFUSAL_FORM;
import static com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;
import static org.dhis2.utils.Constants.SIMPRINTS_IDENTIFY_REQUEST;
import static org.dhis2.utils.Constants.SIMPRINTS_VERIFY_REQUEST;
import static org.dhis2.utils.analytics.AnalyticsConstants.CHANGE_PROGRAM;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017 .
 */
@BindingMethods({
        @BindingMethod(type = FloatingActionButton.class, attribute = "app:srcCompat", method = "setImageDrawable")
})
public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View,
        MapboxMap.OnMapClickListener {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean backDropActive;
    /**
     * 0 - it is general filter
     * 1 - it is search filter
     * 2 - it was closed
     */
    private int switchOpenClose = 2;
    private FiltersAdapter filtersAdapter;

    ObservableBoolean needsSearch = new ObservableBoolean(true);

    private SearchTeiLiveAdapter liveAdapter;
    private RelationshipLiveAdapter relationshipLiveAdapter;
    private FeatureType featureType;
    private MapboxMap map;
    private MarkerViewManager markerViewManager;
    private SymbolManager symbolManager;

    private boolean initSearchNeeded = true;
    private Snackbar downloadingSnackbar;
    private String currentStyle = Style.MAPBOX_STREETS;
    private boolean changingStyle;
    private CustomDialog biometricsErrorDialog;
    private String biometricUid;
    //---------------------------------------------------------------------------------------------

    //region LIFECYCLE
    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule(this, tEType, initialProgram)).inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        binding.setNeedsSearch(needsSearch);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.setTotalFiltersSearch(presenter.getQueryData().size());

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        if (fromRelationship) {
            relationshipLiveAdapter = new RelationshipLiveAdapter(presenter);
            binding.scrollView.setAdapter(relationshipLiveAdapter);
        } else {
            liveAdapter = new SearchTeiLiveAdapter(presenter);
            binding.scrollView.setAdapter(liveAdapter);
        }

        binding.scrollView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), this, presenter));

        binding.enrollmentButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.requestFocus();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                v.performClick();
            }
            return true;
        });

        filtersAdapter = new FiltersAdapter(FiltersAdapter.ProgramType.TRACKER);
        filtersAdapter.addEventStatus();
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }

        binding.mapLayerButton.setOnClickListener(view ->
                new MapLayerDialog(map.getStyle().getImage("ICON_ID"),
                        map.getStyle().getImage("ICON_ENROLLMENT_ID"),
                        isSatelliteStyle -> {
                            if (isSatelliteStyle) {
                                currentStyle = Style.SATELLITE_STREETS;
                            } else {
                                currentStyle = Style.MAPBOX_STREETS;
                            }
                            changingStyle = true;
                            presenter.getMapData();
                            return null;
                        }).show(getSupportFragmentManager(), MapLayerDialog.class.getName()));
        binding.executePendingBindings();
        showHideFilter();

        updateFiltersSearch(presenter.getQueryData().size());
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        filtersAdapter.notifyDataSetChanged();

        binding.biometricSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //simprints - Identification Workflow
                launchSimprintsApp();
            }
        });
    }

    private void launchSimprintsApp() {
        Intent intent = SimprintsHelper.getInstance().simHelper.identify("Module ID");

        try {
            startActivityForResult(intent, SIMPRINTS_IDENTIFY_REQUEST);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(getContext(), "Please download simprints app!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void sendSimprintsAppData(String sessionId, String guid) {
        if(sessionId != null && guid != null) {
            SimprintsHelper.getInstance().simHelper.confirmIdentity(getContext(), sessionId, guid);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        if (isMapVisible()) {
            binding.progressLayout.setVisibility(View.GONE);
        }
        if (initSearchNeeded) {
            presenter.init(tEType);
        } else {
            initSearchNeeded = true;
        }
    }

    @Override
    protected void onPause() {
        if (initSearchNeeded) {
            presenter.onDestroy();
        }
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapView.onDestroy();
        if (markerViewManager != null)
            markerViewManager.onDestroy();
        if (symbolManager != null)
            symbolManager.onDestroy();
        MapLayerManager.Companion.onDestroy();
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case FilterManager.OU_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    filtersAdapter.notifyDataSetChanged();
                    updateFilters(FilterManager.getInstance().getTotalFilters());
                }
                break;
            case Constants.RQ_QR_SCANNER:
                if (resultCode == RESULT_OK) {
                    scanTextView.updateScanResult(data.getStringExtra(Constants.EXTRA_DATA));
                }
                break;
            case SIMPRINTS_IDENTIFY_REQUEST:
                if (resultCode == RESULT_OK) {
                    boolean check = data.getBooleanExtra(SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, false);
                    if (check) {

                        ArrayList<Identification> identifications = data.getParcelableArrayListExtra(SIMPRINTS_IDENTIFICATIONS);
                        RefusalForm refusalForm = data.getParcelableExtra(SIMPRINTS_REFUSAL_FORM);

                          //For Testing And Debugging
//                        bioMetricsGuidList.add("995b1909-2a0c-4204-b0ce-499a3f8111ea");//995b1909-2a0c-4204-b0ce-499a3f8111ea
//                        bioMetricsGuidList.add("!@#$%^&*()BIOMETRICS_DECLINED"); //!@#$%^&*()BIOMETRICS_DECLINED

                        if(identifications == null && refusalForm != null){
                            Toast.makeText(getContext(), "Biometrics declined", Toast.LENGTH_SHORT).show();
                            return;
                        }else if(identifications == null){
                            Toast.makeText(getContext(), "User can not be identified!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArrayList<String>bioMetricsGuidList = new ArrayList<>();

                        for (int i = 0; i < identifications.size(); i++) {
                            identifications.get(i).getGuid();
                            identifications.get(i).getConfidence();
                            identifications.get(i).getTier();
                            bioMetricsGuidList.add(identifications.get(i).getGuid());
                        }

                        String sessionId = data.getStringExtra(SIMPRINTS_SESSION_ID);
                        presenter.storeBiometricsSessionID(sessionId);
                        presenter.setBiometricsSearchGuidData(bioMetricsGuidList.get(0));
                        presenter.setBiometricsSearchStatus(true);
                        presenter.searchOnBiometrics(biometricUid, bioMetricsGuidList.get(0));
                    }else{
                        showBiometricsErrorDialog();
                    }
                }else {
                    Toast.makeText(getContext(), "Biometrics declined", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showBiometricsErrorDialog() {
        String title = getString(R.string.biometrics_error_dialog_title);
        String desc = getString(R.string.biometrics_error_dialog_desc);
        String posButton = getString(R.string.try_again);
        String negButton = getString(R.string.cancel);
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositive() {
                  launchSimprintsApp();
            }

            @Override
            public void onNegative() {
               if(null != biometricsErrorDialog){
                   biometricsErrorDialog.dismiss();
               }
            }
        };
        biometricsErrorDialog = new CustomDialog(getContext(), title, desc, posButton, negButton, 0, dialogClickListener);
        biometricsErrorDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_PERMISSION_REQUEST) {
            initSearchNeeded = false;
        }
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        initSearchNeeded = false;
        super.onMapPositionClick(coordinatesView);
    }

    @Override
    public void onsScanClicked(Intent intent, @NotNull ScanTextView scanTextView) {
        initSearchNeeded = false;
        super.onsScanClicked(intent, scanTextView);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void updateFiltersSearch(int totalFilters) {
        binding.setTotalFiltersSearch(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public Consumer<FeatureType> featureType() {
        return featureType -> this.featureType = featureType;
    }

    @Override
    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        popupMenu.getMenuInflater().inflate(R.menu.search_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.showHelp:
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(false);
                    break;
                case R.id.menu_list:
                    showMap(false);
                    break;
                case R.id.menu_map:
                    showMap(true);
                    break;
                default:
                    break;
            }
            return false;
        });

        boolean messageIsVisible = binding.messageContainer.getVisibility() == View.VISIBLE;
        boolean progressIsVisible = binding.progressLayout.getVisibility() == View.VISIBLE;
        boolean mapIsVisible = binding.mapView.getVisibility() == View.VISIBLE;
        boolean teiListIsVisible = binding.scrollView.getVisibility() == View.VISIBLE;


        popupMenu.getMenu().getItem(0).setVisible(!messageIsVisible && !mapIsVisible && featureType != FeatureType.NONE);
        popupMenu.getMenu().getItem(1).setVisible(!messageIsVisible && !teiListIsVisible && featureType != FeatureType.NONE);
        if (!progressIsVisible)
            popupMenu.show();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    private void showMap(boolean showMap) {
        binding.scrollView.setVisibility(showMap ? View.GONE : View.VISIBLE);
        binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.mapLayerButton.setVisibility(showMap ? View.VISIBLE : View.GONE);

        if (showMap)
            presenter.getMapData();
    }

    @Override
    public void setForm(List<TrackedEntityAttribute> trackedEntityAttributes, @Nullable Program program, HashMap<String, String> queryData,
                        List<ValueTypeDeviceRendering> renderingTypes) {
        //Form has been set.
        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();


        findBiometricUid(trackedEntityAttributes);

        formAdapter.setList(trackedEntityAttributes, program, queryData, renderingTypes);
        updateFiltersSearch(queryData.size());
    }

    private void findBiometricUid(List<TrackedEntityAttribute> trackedEntityAttributes) {
        for(int i=trackedEntityAttributes.size()-1; i>=0; i--){
            String code = trackedEntityAttributes.get(i).code();
            if(code != null && code.equalsIgnoreCase("biometrics")){
                biometricUid = trackedEntityAttributes.get(i).uid();
                break;
            }
        }
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public void clearData() {
        if (!isMapVisible()) {
            binding.progressLayout.setVisibility(View.VISIBLE);
        }
        binding.scrollView.setVisibility(View.GONE);
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() ->
                        HelpManager.getInstance().show(getActivity(),
                                HelpManager.TutorialName.TEI_SEARCH,
                                null),
                500);
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    @Override
    public void setLiveData(LiveData<PagedList<SearchTeiModel>> liveData) {
        if (!fromRelationship) {
            liveData.observe(this, searchTeiModels -> {


                if(presenter.getBiometricsSearchStatus()){
                    presenter.clearQueryData();
                    for(int i = 0; i < searchTeiModels.size(); i++){
                        searchTeiModels.get(i).setBiometricsSearchStatus(true);
                    }
                }else{
                    presenter.setBiometricsSearchStatus(false);

                    for(int i = 0; i < searchTeiModels.size(); i++){
                        searchTeiModels.get(i).setBiometricsSearchStatus(false);
                    }
                }

                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);

                if (data.val1().isEmpty()) {
                    binding.filterCounter.setVisibility(View.VISIBLE);
                    //simprints - search_filter_general should not be visible.
                    //binding.searchFilterGeneral.setVisibility(View.VISIBLE);
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    liveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                } else {
                    binding.filterCounter.setVisibility(View.GONE);
                    binding.searchFilterGeneral.setVisibility(View.GONE);

                    showMap(false);
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                    binding.scrollView.setVisibility(View.GONE);
                }
            });
        } else {
            liveData.observeForever(searchTeiModels -> {
                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    relationshipLiveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                } else {
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                    binding.scrollView.setVisibility(View.GONE);
                }
                if (!presenter.getQueryData().isEmpty() && data.val2())
                    setFabIcon(false);
            });
        }
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        if (uid == null)
            binding.programSpinner.setSelection(0);
    }
    //endregion

    @Override
    public void setPrograms(List<Program> programs) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programs, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programs);
        else
            binding.programSpinner.setSelection(0);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(binding.programSpinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    analyticsHelper().setEvent(CHANGE_PROGRAM, CLICK, CHANGE_PROGRAM);
                    Program selectedProgram = (Program) adapterView.getItemAtPosition(pos - 1);
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()));
                    presenter.setProgram((Program) adapterView.getItemAtPosition(pos - 1));
                } else if (programs.size() == 1 && pos != 0) {
                    presenter.setProgram(programs.get(0));
                } else {
                    presenter.setProgram(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setInitialProgram(List<Program> programs) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }

    @Override
    public void showAssignmentFilter(){
        filtersAdapter.addAssignedToMe();
    }

    @Override
    public void hideAssignmentFilter(){
        filtersAdapter.removeAssignedToMe();
    }

    @Override
    public void setProgramColor(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color, ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(programColor));
            binding.mainToolbar.setBackgroundColor(programColor);
            binding.backdropLayout.setBackgroundColor(programColor);
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
            int colorPrimary;
            switch (prefs.getInt(Constants.THEME, R.style.AppTheme)) {
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorPrimary)));
            binding.mainToolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            binding.backdropLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
        }

        binding.executePendingBindings();
        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int colorToReturn = a.getColor(0, 0);
            a.recycle();
            window.setStatusBarColor(colorToReturn);
        }
    }

    @Override
    public String fromRelationshipTEI() {
        return fromRelationshipTeiUid;
    }

    @Override
    public void setFabIcon(boolean needsSearch) {
        this.needsSearch.set(needsSearch);
        animSearchFab(needsSearch);
    }

    private void animSearchFab(boolean hasQuery) {
        if (hasQuery) {
            binding.enrollmentButton.startAnimation(
                    AnimationUtils.loadAnimation(binding.enrollmentButton.getContext(), R.anim.bounce_animation));
        } else {
            binding.enrollmentButton.clearAnimation();
            hideKeyboard();
        }
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(View.GONE);
        binding.formRecycler.setVisibility(View.VISIBLE);

        swipeFilters(false);
    }

    @Override
    public void showHideFilterGeneral() {
        binding.filterLayout.setVisibility(View.VISIBLE);
        binding.formRecycler.setVisibility(View.GONE);

        swipeFilters(true);
    }

    private void swipeFilters(boolean general) {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        if (backDropActive && !general && switchOpenClose == 0)
            switchOpenClose = 1;
        else if (backDropActive && general && switchOpenClose == 1)
            switchOpenClose = 0;
        else {
            switchOpenClose = general ? 0 : 1;
            backDropActive = !backDropActive;
        }
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            activeFilter(general);
    }

    private void activeFilter(boolean general) {
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);

        if (backDropActive) {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, general ? R.id.filterLayout : R.id.form_recycler, ConstraintSet.BOTTOM, 50);
        } else {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void closeFilters() {
        if (switchOpenClose == 0)
            showHideFilterGeneral();
        else
            showHideFilter();
    }

    @Override
    public void clearFilters() {
        if (switchOpenClose == 0) {
            FilterManager.getInstance().clearAllFilters();
            filtersAdapter.notifyDataSetChanged();
        } else
            presenter.onClearClick();

    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.Companion.getBundle(initialProgram);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this, FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        FilterManager.getInstance().addPeriod(datePeriods);
                    },
                    true);
        }
    }

    @Override
    public void openDashboard(String teiUid, String programUid, String enrollmentUid) {
        if (downloadingSnackbar != null && downloadingSnackbar.isShown()) {
            downloadingSnackbar.dismiss();
        }
        startActivity(TeiDashboardMobileActivity.intent(this, teiUid, programUid, enrollmentUid));
    }

    @Override
    public void goToEnrollment(String enrollmentUid, String programUid) {
        Intent intent = EnrollmentActivity.Companion.getIntent(this,
                enrollmentUid,
                programUid,
                EnrollmentActivity.EnrollmentMode.NEW,
                fromRelationshipTEI() != null);
        startActivity(intent);
        finish();
    }

    /*region MAP*/
    @Override
    public void setMap(HashMap<String, FeatureCollection> teiFeatureCollections, BoundingBox boundingBox) {
        binding.progressLayout.setVisibility(View.GONE);

        if (map == null) {
            binding.mapView.getMapAsync(mapboxMap -> {
                map = mapboxMap;
                if (map.getStyle() == null) {
                    map.setStyle(Style.MAPBOX_STREETS, style -> loadDataForStyle(style, teiFeatureCollections, boundingBox));

                    binding.mapView.addOnStyleImageMissingListener(filePath -> {
                        File file = new File(filePath);
                        if (file.exists()) {
                            Style style = mapboxMap.getStyle();
                            if (style != null) {
                                style.addImageAsync(filePath, MarkerUtils.INSTANCE.getMarker(this, FileResourcesUtil.getSmallImage(this, filePath), presenter.getTEIColor()));
                            }
                        } else {
                            Style style = mapboxMap.getStyle();
                            if (style != null) {
                                style.addImageAsync(filePath, MarkerUtils.INSTANCE.getMarker(this, presenter.getSymbolIcon(), presenter.getTEIColor()));
                            }
                        }
                    });

                } else {
                    binding.mapLayerButton.setVisibility(View.VISIBLE);
                    ((GeoJsonSource) mapboxMap.getStyle().getSource("teis")).setGeoJson(teiFeatureCollections.get("TEI"));
                    ((GeoJsonSource) mapboxMap.getStyle().getSource("enrollments")).setGeoJson(teiFeatureCollections.get("ENROLLMENT"));
                    LatLngBounds bounds = LatLngBounds.from(boundingBox.north(),
                            boundingBox.east(),
                            boundingBox.south(),
                            boundingBox.west());

                    MapboxExtensionKt.initDefaultCamera(map, this, bounds);
                }
            });
        } else if (changingStyle) {
            map.setStyle(currentStyle, style -> {
                loadDataForStyle(style, teiFeatureCollections, boundingBox);
                changingStyle = false;
            });
        } else {
            ((GeoJsonSource) map.getStyle().getSource("teis")).setGeoJson(teiFeatureCollections.get("TEI"));
            ((GeoJsonSource) map.getStyle().getSource("enrollments")).setGeoJson(teiFeatureCollections.get("ENROLLMENT"));
            if (boundingBox.north() != 0.0 && boundingBox.east() != 0.0 && boundingBox.south() != 0.0 && boundingBox.west() != 0.0) {
                LatLngBounds bounds = LatLngBounds.from(boundingBox.north(),
                        boundingBox.east(),
                        boundingBox.south(),
                        boundingBox.west());

                MapboxExtensionKt.initDefaultCamera(map, this, bounds);
            } else {
                map.easeCamera(CameraUpdateFactory.zoomTo(map.getMinZoomLevel()));
            }
        }
    }

    private void loadDataForStyle(Style style, HashMap<String, FeatureCollection> teiFeatureCollection, BoundingBox bbox) {
        binding.mapLayerButton.setVisibility(View.VISIBLE);
        if (!changingStyle) {
            MapLayerManager.Companion.init(style, "teis", featureType);
            MapLayerManager.Companion.instance().setEnrollmentLayerData(
                    presenter.getProgram() != null ?
                            ColorUtils.getColorFrom(presenter.getProgram().style() != null ? presenter.getProgram().style().color() : null, ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY)) :
                            ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY),
                    ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY_DARK),
                    presenter.getProgram() != null ? presenter.getProgram().featureType() != null ? presenter.getProgram().featureType() : FeatureType.NONE : FeatureType.NONE
            );
            MapLayerManager.Companion.instance().showEnrollmentLayer().observe(this, show -> {
                if (show)
                    presenter.getEnrollmentMapData();
            });
        } else {
            MapLayerManager.Companion.instance().updateStyle(style);
        }
        map.addOnMapClickListener(this);

        style.addImage("ICON_ID", MarkerUtils.INSTANCE.getMarker(this, presenter.getSymbolIcon(), presenter.getTEIColor()));
        style.addImage("ICON_ENROLLMENT_ID", MarkerUtils.INSTANCE.getMarker(this, presenter.getEnrollmentSymbolIcon(), presenter.getEnrollmentColor()));

        setSource(style, teiFeatureCollection);

        setLayer(style);

        LatLngBounds bounds = LatLngBounds.from(bbox.north(),
                bbox.east(),
                bbox.south(),
                bbox.west());

        MapboxExtensionKt.initDefaultCamera(map, this, bounds);

        if (markerViewManager == null) {
            markerViewManager = new MarkerViewManager(binding.mapView, map);
        }

        if (symbolManager == null) {
            symbolManager = new SymbolManager(binding.mapView, map, style, null,
                    new GeoJsonOptions().withTolerance(0.4f));

            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            symbolManager.setIconIgnorePlacement(true);
            symbolManager.setTextIgnorePlacement(true);
            symbolManager.setSymbolPlacement("line-center");
            symbolManager.create(teiFeatureCollection.get("TEI"));
        }
    }

    @Override
    public Consumer<D2Progress> downloadProgress() {
        return progress -> Snackbar.make(binding.getRoot(), String.format("Downloading %s", String.valueOf(progress.percentage())) + "%", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean isMapVisible() {
        return binding.mapView.getVisibility() == View.VISIBLE;
    }

    private void setSource(Style style, HashMap<String, FeatureCollection> featCollectionMap) {
        style.addSource(new GeoJsonSource("teis", featCollectionMap.get("TEI")));
        style.addSource(new GeoJsonSource("enrollments", featCollectionMap.get("ENROLLMENT")));
    }

    private void setLayer(Style style) {

        SymbolLayer symbolLayer = new SymbolLayer("POINT_LAYER", "teis").withProperties(
                PropertyFactory.iconImage(get("teiImage")),
                iconOffset(new Float[]{0f, -25f}),
                iconAllowOverlap(true),
                textAllowOverlap(true)
        );

        symbolLayer.setFilter(eq(literal("$type"), literal("Point")));

        style.addLayer(symbolLayer);

        if (featureType != FeatureType.POINT) {
            style.addLayerBelow(new FillLayer("POLYGON_LAYER", "teis")
                            .withProperties(
                                    fillColor(
                                            ColorUtils.getPrimaryColorWithAlpha(this, ColorUtils.ColorType.PRIMARY_LIGHT, 150f)
                                    ))
                            .withFilter(eq(literal("$type"), literal("Polygon"))),
                    "POINT_LAYER"
            );
            style.addLayerAbove(new LineLayer("POLYGON_BORDER_LAYER", "teis")
                            .withProperties(
                                    lineColor(
                                            ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY_DARK)
                                    ),
                                    lineWidth(2f))
                            .withFilter(eq(literal("$type"), literal("Polygon"))),
                    "POLYGON_LAYER"

            );
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = map.getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
        List<Feature> features = map.queryRenderedFeatures(rectF, featureType == FeatureType.POINT ? "POINT_LAYER" : "POLYGON_LAYER");
        if (!features.isEmpty()) {
            presenter.onTEIClick(features.get(0).getStringProperty("teiUid"),
                    features.get(0).getStringProperty("enrollmentUid"),
                    false);
            return true;
        }

        return false;
    }

    /*endregion*/
}
