package magnusdroid.com.glucup_2date.Controler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import magnusdroid.com.glucup_2date.Model.ListGluc;
import magnusdroid.com.glucup_2date.Model.MDateGlucose;
import magnusdroid.com.glucup_2date.Model.MyBarDataSet;
import magnusdroid.com.glucup_2date.Model.MyMarkerView;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;


/**
 * Fragment to show Records in a week. Called by {@link WeekFilterGlucose}
 * Use PageAdapter to show the records of different day: List and Chart
 */
public class DayOfWeekFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String FLAG = "flag";
    private static final String PATIENT = "patient";
    // UI references.
    private FloatingActionButton mFab, mFab1, mFab2;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private RelativeLayout rlayout;
    private ScrollView scrollView;
    private TextView txtv;
    private LineChart mLineChart;
    protected BarChart mBarChart;
    private CombinedChart mCombiChart;
    // Get shared preferences
    private PrefManager prefManager;
    // Utilities
    private Boolean isFabOpen = false;
    private Boolean mBoolean;
    private int mInt, mFlag;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private int med;
    private String date, unit, value, mPatient;
    private YAxis leftBarAxis, leftLineAxis, leftCombiAxis;
    private ArrayList<Entry> yLineValues;
    private ArrayList<BarEntry> yBarValues;
    private ArrayList<String> xAxes;
    private MyMarkerView mv;
    private ProgressDialog progress;
    // JsonObject response from server
    private JSONObject jObject;
    private JSONArray jArray;
    private List<ListGluc> glucList = new ArrayList<>();
    // Keep track of the filter task to ensure we can cancel it if requested.
    private DownloadDate downloadDate = null;

    public DayOfWeekFragment() {
        // Required empty public constructor
    }

    /**
     * Used to handle the data sended by {@link WeekFilterGlucose}
     * @param sectionNumber String day to show the title and the records of the day
     * @param mFlag int Flag to show List or Chart
     * @param mPatient
     * @return UI according to the data
     */
    public static DayOfWeekFragment newInstance(String sectionNumber, int mFlag, String mPatient) {
        DayOfWeekFragment fragment = new DayOfWeekFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(FLAG, mFlag);
        args.putString(PATIENT, mPatient);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Allow change of orientation to see the chart in landscape or portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.week_day_filter, container, false);
        // Set up shared preferences.
        prefManager = new PrefManager(getContext());
        // Set up widget and utilities
        txtv = (TextView) view.findViewById(R.id.txtweekday);
        rlayout = (RelativeLayout) view.findViewById(R.id.nonweekday);
        date = getArguments().getString(ARG_SECTION_NUMBER);
        mFlag = getArguments().getInt(FLAG);
        mPatient = getArguments().getString(PATIENT);
        xAxes = new ArrayList<>();
        yLineValues = new ArrayList<>();
        yBarValues = new ArrayList<>();
        mFab = (FloatingActionButton) view.findViewById(R.id.fab_date_gluc);
        mFab1 = (FloatingActionButton) view.findViewById(R.id.fab_date_gluc1);
        mFab2 = (FloatingActionButton) view.findViewById(R.id.fab_date_gluc2);
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_backward);
        scrollView = (ScrollView) view.findViewById(R.id.scroll_weekday);
        Button mShow = (Button) view.findViewById(R.id.show_linebar_weekday);
        mLineChart = (LineChart) view.findViewById(R.id.line_chart_weekday);
        // no description text
        mLineChart.setDescription("");
        mLineChart.setNoDataTextDescription("You need to provide data for the chart.");
        // enable touch gestures
        mLineChart.setTouchEnabled(true);
        mLineChart.setDrawGridBackground(false);
        // enable scaling and dragging
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.getLegend().setEnabled(false);
        mBarChart = (BarChart) view.findViewById(R.id.bar_chart_weekday);
        // no description text
        mBarChart.setDescription("");
        mBarChart.setNoDataTextDescription("You need to provide data for the chart.");
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setDragEnabled(false);
        mBarChart.getLegend().setEnabled(false);
        mCombiChart = (CombinedChart) view.findViewById(R.id.combi_chart_weekday);
        mCombiChart.setDescription("");
        mCombiChart.setDrawGridBackground(false);
        mCombiChart.setDrawBarShadow(false);
        mCombiChart.setPinchZoom(false);
        mCombiChart.setDragEnabled(false);
        mCombiChart.getLegend().setEnabled(false);
        // draw bars behind lines
        mCombiChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE});
        XAxis xLineAxis = mLineChart.getXAxis();
        xLineAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLineAxis.setTextSize(10f);
        xLineAxis.setDrawGridLines(false);
        xLineAxis.setDrawAxisLine(true);
        xLineAxis.setTextColor(R.color.primary_text);
        XAxis xBarAxis = mBarChart.getXAxis();
        xBarAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xBarAxis.setTextSize(10f);
        xBarAxis.setDrawGridLines(false);
        xBarAxis.setDrawAxisLine(true);
        xBarAxis.setTextColor(R.color.primary_text);
        XAxis xCombiAxis = mCombiChart.getXAxis();
        xCombiAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xCombiAxis.setTextSize(10f);
        xCombiAxis.setDrawGridLines(false);
        xCombiAxis.setDrawAxisLine(true);
        xCombiAxis.setTextColor(R.color.primary_text);
        leftBarAxis = mBarChart.getAxisLeft();
        leftBarAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftBarAxis.setDrawAxisLine(true);
        leftBarAxis.setDrawGridLines(false);
        leftBarAxis.setYOffset(-9f);
        leftBarAxis.setTextColor(R.color.primary_text);
        YAxis rightAxis = mBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);
        leftLineAxis = mLineChart.getAxisLeft();
        leftLineAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftLineAxis.setDrawAxisLine(true);
        leftLineAxis.setDrawGridLines(false);
        leftLineAxis.setYOffset(-9f);
        leftLineAxis.setTextColor(R.color.primary_text);
        YAxis rightLineAxis = mLineChart.getAxisRight();
        rightLineAxis.setDrawGridLines(false);
        rightLineAxis.setEnabled(false);
        leftCombiAxis = mCombiChart.getAxisLeft();
        leftCombiAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftCombiAxis.setDrawAxisLine(true);
        leftCombiAxis.setDrawGridLines(false);
        leftCombiAxis.setYOffset(-9f);
        leftCombiAxis.setTextColor(R.color.primary_text);
        YAxis rightCombiAxis = mCombiChart.getAxisRight();
        rightCombiAxis.setDrawGridLines(false);
        rightCombiAxis.setEnabled(false);
        mShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarChart.setVisibility(View.VISIBLE);
                mLineChart.setVisibility(View.VISIBLE);
            }
        });
        mFab.setOnClickListener(this);
        mFab1.setOnClickListener(this);
        mFab2.setOnClickListener(this);
        recycler = (RecyclerView) view.findViewById(R.id.weekday_recycler);
        recycler.setHasFixedSize(true);
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.fetching_data));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        med = 0;
        Task(med);
        return view;
    }

    /**
     * Hide/Show List or Chart according to the Flag
     * @param Mmed int Unit to show mmol/l or mg/dl
     */
    private void Task(int Mmed){
        progress.show();
        String document = null;
        if(mFlag == 0){
            document = prefManager.getDoc();
            recycler.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }else if(mFlag == 1){
            document = prefManager.getDoc();
            recycler.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }else if(mFlag == 3 || mFlag == 2){
            recycler.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            document = mPatient;
        }else if(mFlag == 4){
            recycler.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            document = mPatient;
        }
            downloadDate = new DownloadDate(document, date, Mmed);
            downloadDate.execute();
        med = 0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab_date_gluc:
                animateFAB();
                break;
            case R.id.fab_date_gluc1:
                animateFAB();
                newGluc();
                break;
            case R.id.fab_date_gluc2:
                if(med ==0){
                    Task(med);
                    med = 1;
                }else{
                    Task(med);
                    med = 0;
                }
                isFabOpen = true;
                animateFAB();
                break;
        }
    }

    public void animateFAB(){
        if(isFabOpen){
            mFab.startAnimation(rotate_backward);
            mFab1.startAnimation(fab_close);
            mFab2.startAnimation(fab_close);
            mFab1.setClickable(false);
            mFab2.setClickable(false);
            isFabOpen = false;
        } else {
            mFab.startAnimation(rotate_forward);
            mFab1.startAnimation(fab_open);
            mFab2.startAnimation(fab_open);
            mFab1.setClickable(true);
            mFab2.setClickable(true);
            isFabOpen = true;
        }
    }

    /**
     * Start {@link AddGlucActivity}
     */
    public void newGluc(){
        Intent i = new Intent(getContext(), AddGlucActivity.class);
        startActivity(i);
    }

    /**
     * AsyncTask to handle the connection with the server. Recieve the response and send the data
     * using {@link MDateGlucose} class.
     * Data send: <i>Pacient DNI, Datetime, Unit and Context to use {@link PrefManager}</i>
     * Response: JSON with fetched data, parse used {@link ListGluc} class to build List or Chart
     */
    private class DownloadDate extends AsyncTask<Void, Void, Boolean> {

        private final String mDoc;
        private final String mDate;
        private final int mMed;


        DownloadDate(String doc, String date, int med) {
            mDoc = doc;
            mDate = date;
            mMed = med;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //Model
            MDateGlucose mDateGlucose = new MDateGlucose();
            DecimalFormat df = new DecimalFormat("#.##");
            glucList.clear();
            yLineValues.clear();
            yBarValues.clear();
            xAxes.clear();
            try {
                jObject = mDateGlucose.getDay(mDoc, mDate, "1");
                mInt = jObject.getInt("status");
                if(mInt == 1){
                    mBoolean = true;
                    jArray = jObject.getJSONArray("obs_glucose");
                    for (int i = 0; i < jArray.length(); i++) {
                        jObject = jArray.getJSONObject(i);
                        ListGluc listGluc = new ListGluc();
                        listGluc.setIssued(jObject.getString("issued"));
                        listGluc.setCode(jObject.getString("code"));
                        listGluc.setState(jObject.getString("state"));
                        if(jObject.has("performer")){
                            listGluc.setPerformer(jObject.getString("performer"));
                        }else {
                            //listGluc.setPerformer(prefManager.getUser());
                            listGluc.setPerformer(prefManager.getPatientName());
                        }
                        if(mMed == 1){ //mmol/l -> mg/dl
                            Double aDouble = Double.parseDouble(jObject.getString("value"));
                            df.setRoundingMode(RoundingMode.CEILING);
                            value = String.valueOf(df.format(aDouble*18));
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }
                        listGluc.setUnit(unit);
                        listGluc.setValue(value);
                        glucList.add(listGluc);
                        yLineValues.add(new Entry(Float.parseFloat(value), i));
                        yBarValues.add(new BarEntry(Float.parseFloat(value), i));
                        xAxes.add(i, jObject.getString("issued"));
                    }
                }else if(mInt == 0){
                    mBoolean = false;
                }

            } catch (JSONException e) {e.printStackTrace();}
            return mBoolean;
        }

        @Override
        protected void onPostExecute(Boolean bb) {
            progress.dismiss();
            if(bb){
                downloadDate = null;
                rlayout.setVisibility(View.GONE);
                adapter = new RecyclerViewAdapter(glucList);
                recycler.setAdapter(adapter);
                adapter = new RecyclerViewAdapter(glucList);
                recycler.setAdapter(adapter);
                mLineChart.animateXY(3000, 3000);
                mBarChart.animateY(2500);
                mCombiChart.animateXY(3000, 2500);
                if(unit.equalsIgnoreCase("mmol/l")){
                    leftBarAxis.setAxisMinValue(0f);
                    leftBarAxis.setAxisMaxValue(10f);
                    leftLineAxis.setAxisMinValue(0f);
                    leftLineAxis.setAxisMaxValue(10f);
                    leftCombiAxis.setAxisMinValue(0f);
                    leftCombiAxis.setAxisMaxValue(10f);
                }else if(unit.equalsIgnoreCase("mg/dl")){
                    leftBarAxis.setAxisMinValue(60f);
                    leftBarAxis.setAxisMaxValue(190f);
                    leftLineAxis.setAxisMinValue(60f);
                    leftLineAxis.setAxisMaxValue(190f);
                    leftCombiAxis.setAxisMinValue(60f);
                    leftCombiAxis.setAxisMaxValue(190f);
                }
                //Fetch the data and populate linechart
                LineDataSet set1 = new LineDataSet(yLineValues, "Muestras");
                set1.setAxisDependency(YAxis.AxisDependency.LEFT);
                set1.setValueTextSize(15f);
                set1.setCircleColor(Color.WHITE);
                set1.setLineWidth(2f);
                MyBarDataSet set2 = new MyBarDataSet(yBarValues, "");
                set2.setValueTextSize(15f);
                set2.setColors(new int[]{ContextCompat.getColor(getContext(), R.color.normal_value),
                        ContextCompat.getColor(getContext(), R.color.low_value),
                        ContextCompat.getColor(getContext(), R.color.high_value),
                        ContextCompat.getColor(getContext(), R.color.primary_text)});
                // use the interface LineDataSet - BarDataSet
                ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
                lineDataSets.add(set1);
                ArrayList<BarDataSet> barDataSets = new ArrayList<>();
                barDataSets.add(set2);
                LineData lineData = new LineData(xAxes, lineDataSets);
                BarData barData = new BarData(xAxes, barDataSets);
                //Fetch the data and populate combinedchart
                LineData d = new LineData(xAxes, set1);
                d.addDataSet(set1);
                BarData db = new BarData(xAxes, set2);
                CombinedData combinedData = new CombinedData(xAxes);
                combinedData.setData(d);
                combinedData.setData(db);
                // create a custom MarkerView (extend MarkerView) and specify the layout
                // to use for it
                mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
                // set the marker to the chart
                mLineChart.setMarkerView(mv);
                // create a data object with the datasets
                mLineChart.setData(lineData);
                mLineChart.invalidate();
                mBarChart.setData(barData);
                mBarChart.invalidate();
                mCombiChart.setData(combinedData);
                mCombiChart.invalidate();
            }else{
                recycler.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                rlayout.setVisibility(View.VISIBLE);
                txtv.setText("No hay registro para este día");
                mFab.setClickable(false);
                mFab1.setClickable(false);
                mFab2.setClickable(false);
            }
        }
    }


}
