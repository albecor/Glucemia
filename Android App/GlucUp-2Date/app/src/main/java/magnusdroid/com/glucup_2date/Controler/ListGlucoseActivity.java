package magnusdroid.com.glucup_2date.Controler;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import magnusdroid.com.glucup_2date.Model.MAllGlucose;
import magnusdroid.com.glucup_2date.Model.MDateGlucose;
import magnusdroid.com.glucup_2date.Model.MValueGlucose;
import magnusdroid.com.glucup_2date.Model.MyBarDataSet;
import magnusdroid.com.glucup_2date.Model.MyMarkerView;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;

/**
 * Activity to show List or Chart according with the Filter selected by {@link FilterActivity}
 * Use <i>{@link MDateGlucose}, {@link MAllGlucose}, {@link MValueGlucose}</i> classes to connect with server
 * and get the data
 */
public class ListGlucoseActivity extends AppCompatActivity implements View.OnClickListener {


    // Keep track of the filter task to ensure we can cancel it if requested.
    private DownloadDate downloadDate = null;
    private DownloadAll downloadAll = null;
    private DownloadValue downloadValue = null;
    // UI references.
    private FloatingActionButton mFab, mFab1, mFab2;
    private RecyclerView recycler, recycler1;
    private RecyclerView.Adapter adapter, adapter1;
    private RelativeLayout rlayout;
    private ScrollView scrollView, scrollView1;
    private TextView txtv;
    private LineChart mLineChart, mLineChart1;
    protected BarChart mBarChart, mBarChart1;
    private CombinedChart mCombiChart, mCombiChart1;
    // Get shared preferences
    private PrefManager prefManager;
    // Utilities
    private DecimalFormat df = new DecimalFormat("#.##");
    private Boolean isFabOpen = false;
    private Boolean mBoolean;
    private int mInt, med, mFlag;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private String date;
    private String mDoc;
    private String minvalue;
    private String maxvalue;
    private String fixvalue;
    private String unit;
    private YAxis leftBarAxis, leftLineAxis, leftCombiAxis, leftBarAxis1, leftLineAxis1, leftCombiAxis1;
    private ArrayList<Entry> yLineValues, yLineValues1;
    private ArrayList<BarEntry> yBarValues, yBarValues1;
    private ArrayList<String> xAxes, xAxes1;
    private MyMarkerView mv;
    // Model
    private MDateGlucose mDateGlucose;
    private MAllGlucose mAllGlucose;
    private MValueGlucose mValueGlucose;
    // JsonObject response from server
    private JSONObject jObject;
    private JSONArray jArray;
    private List<ListGluc> glucList = new ArrayList<>();
    private List<ListGluc> glucList1 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_filter_glucose);
        // Set up shared preferences.
        prefManager = new PrefManager(this);
        // Set up bundle data.
        Bundle extras = getIntent().getExtras();
        date = extras.getString("date");
        mDoc = extras.getString("patient");
        minvalue = extras.getString("Vmin");
        maxvalue = extras.getString("Vmax");
        fixvalue = extras.getString("Vfix");
        mFlag = extras.getInt("flag");
        unit = extras.getString("Unit");
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Set up the title according filter
        if(date.equalsIgnoreCase("todos")){
            getSupportActionBar().setTitle(R.string.filter_all_data);
        } else if(date.equalsIgnoreCase("valor")){
            getSupportActionBar().setTitle(R.string.filter_result);
        } else{
            getSupportActionBar().setTitle(getString(R.string.filter_by_day, date));
        }
        // Set up widget and utilites
        xAxes = new ArrayList<>();
        xAxes1 = new ArrayList<>();
        yLineValues = new ArrayList<>();
        yBarValues = new ArrayList<>();
        yLineValues1 = new ArrayList<>();
        yBarValues1 = new ArrayList<>();
        txtv = (TextView) findViewById(R.id.section_label);
        rlayout = (RelativeLayout) findViewById(R.id.novlauefilter);
        mFab = (FloatingActionButton) findViewById(R.id.fab_date_gluc);
        mFab1 = (FloatingActionButton) findViewById(R.id.fab_date_gluc1);
        mFab2 = (FloatingActionButton) findViewById(R.id.fab_date_gluc2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        scrollView = (ScrollView) findViewById(R.id.scroll_filter);
        Button mShow = (Button) findViewById(R.id.show_linebar_filter);
        mLineChart = (LineChart) findViewById(R.id.line_chart_filter);
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
        mBarChart = (BarChart) findViewById(R.id.bar_chart_filter);
        // no description text
        mBarChart.setDescription("");
        mBarChart.setNoDataTextDescription("You need to provide data for the chart.");
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setDragEnabled(false);
        mBarChart.getLegend().setEnabled(false);
        mCombiChart = (CombinedChart) findViewById(R.id.combi_chart_filter);
        mCombiChart.setDescription("");
        mCombiChart.setDrawGridBackground(false);
        mCombiChart.setDrawBarShadow(false);
        mCombiChart.setPinchZoom(false);
        mCombiChart.setDragEnabled(false);
        mCombiChart.getLegend().setEnabled(false);
        // draw bars behind lines
        mCombiChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });
        scrollView1 = (ScrollView) findViewById(R.id.scroll_filter1);
        mLineChart1 = (LineChart) findViewById(R.id.line_chart_filter1);
        // no description text
        mLineChart1.setDescription("");
        mLineChart1.setNoDataTextDescription("You need to provide data for the chart.");
        // enable touch gestures
        mLineChart1.setTouchEnabled(true);
        mLineChart1.setDrawGridBackground(false);
        // enable scaling and dragging
        mLineChart1.setDragEnabled(false);
        mLineChart1.setScaleEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart1.setPinchZoom(false);
        mLineChart1.setDrawGridBackground(false);
        mLineChart1.getLegend().setEnabled(false);
        mBarChart1 = (BarChart) findViewById(R.id.bar_chart_filter1);
        // no description text
        mBarChart1.setDescription("");
        mBarChart1.setNoDataTextDescription("You need to provide data for the chart.");
        mBarChart1.setDrawBarShadow(false);
        mBarChart1.setDrawGridBackground(false);
        mBarChart1.setScaleEnabled(false);
        mBarChart1.setPinchZoom(false);
        mBarChart1.setDragEnabled(false);
        mBarChart1.getLegend().setEnabled(false);
        mCombiChart1 = (CombinedChart) findViewById(R.id.combi_chart_filter1);
        mCombiChart1.setDescription("");
        mCombiChart1.setDrawGridBackground(false);
        mCombiChart1.setDrawBarShadow(false);
        mCombiChart1.setPinchZoom(false);
        mCombiChart1.setDragEnabled(false);
        mCombiChart1.getLegend().setEnabled(false);
        // draw bars behind lines
        mCombiChart1.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });
        XAxis xLineAxis = mLineChart.getXAxis();
        xLineAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLineAxis.setTextSize(10f);
        xLineAxis.setDrawGridLines(false);
        xLineAxis.setDrawAxisLine(true);
        xLineAxis.setTextColor(R.color.primary_text);
        XAxis xLineAxis1 = mLineChart1.getXAxis();
        xLineAxis1.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLineAxis1.setTextSize(10f);
        xLineAxis1.setDrawGridLines(false);
        xLineAxis1.setDrawAxisLine(true);
        xLineAxis1.setTextColor(R.color.primary_text);
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
        leftBarAxis = leftBarAxis1 = mBarChart.getAxisLeft();
        leftBarAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftBarAxis.setDrawAxisLine(true);
        leftBarAxis.setDrawGridLines(false);
        leftBarAxis.setYOffset(-9f);
        leftBarAxis.setTextColor(R.color.primary_text);
        YAxis rightAxis = mBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);
        YAxis rightAxis1 = mBarChart1.getAxisRight();
        rightAxis1.setDrawGridLines(false);
        rightAxis1.setEnabled(false);
        leftLineAxis = leftLineAxis1 = mLineChart.getAxisLeft();
        leftLineAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftLineAxis.setDrawAxisLine(true);
        leftLineAxis.setDrawGridLines(false);
        leftLineAxis.setYOffset(-9f);
        leftLineAxis.setTextColor(R.color.primary_text);
        YAxis rightLineAxis = mLineChart.getAxisRight();
        rightLineAxis.setDrawGridLines(false);
        rightLineAxis.setEnabled(false);
        YAxis rightLineAxis1 = mLineChart1.getAxisRight();
        rightLineAxis1.setDrawGridLines(false);
        rightLineAxis1.setEnabled(false);
        leftCombiAxis = leftCombiAxis1 = mCombiChart.getAxisLeft();
        leftCombiAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftCombiAxis.setDrawAxisLine(true);
        leftCombiAxis.setDrawGridLines(false);
        leftCombiAxis.setYOffset(-9f);
        leftCombiAxis.setTextColor(R.color.primary_text);
        YAxis rightCombiAxis = mCombiChart.getAxisRight();
        rightCombiAxis.setDrawGridLines(false);
        rightCombiAxis.setEnabled(false);
        YAxis rightCombiAxis1 = mCombiChart1.getAxisRight();
        rightCombiAxis1.setDrawGridLines(false);
        rightCombiAxis1.setEnabled(false);
        mShow.setOnClickListener(this);
        mFab.setOnClickListener(this);
        mFab1.setOnClickListener(this);
        mFab2.setOnClickListener(this);
        recycler = (RecyclerView) findViewById(R.id.date_recycler);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(lManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler1 = (RecyclerView) findViewById(R.id.date_recycler1);
        recycler1.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager1 = new LinearLayoutManager(getApplicationContext());
        recycler1.setLayoutManager(lManager1);
        recycler1.setItemAnimator(new DefaultItemAnimator());
        med = 0;
        unit = "mmol/l";
        Task(med);
    }

    /**
     * See: {@link ChartFragment#Task(int)}
     * @param Mmed
     * This class is used to hide/show List or Chart and start specific AsyncTask
     */
    private void Task(int Mmed){
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
            document = mDoc;
        }else if(mFlag == 4){
            recycler.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            document = mDoc;
        }
        Log.w("Doc"," "+document);
        if(date.equalsIgnoreCase("todos")){
            downloadAll = new DownloadAll(document, Mmed);
            downloadAll.execute();
        } else if(date.equalsIgnoreCase("valor")){
            downloadValue = new DownloadValue(document, fixvalue, minvalue, maxvalue, unit, Mmed);
            downloadValue.execute();
        } else{
            downloadDate = new DownloadDate(document, date);
            downloadDate.execute();
        }
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
                if (recycler.getVisibility() == View.VISIBLE && scrollView.getVisibility() == View.GONE
                        && scrollView1.getVisibility() == View.GONE) {
                    scrollView.setVisibility(View.GONE);
                    scrollView1.setVisibility(View.GONE);
                    recycler.setVisibility(View.GONE);
                    recycler1.setVisibility(View.VISIBLE);
                    unit = "mmol/l";
                } else if (recycler.getVisibility() == View.GONE && scrollView.getVisibility() == View.GONE
                        && scrollView1.getVisibility() == View.GONE) {
                    scrollView.setVisibility(View.GONE);
                    scrollView1.setVisibility(View.GONE);
                    recycler.setVisibility(View.VISIBLE);
                    recycler1.setVisibility(View.GONE);
                    unit = "mg/dl";
                } else if (scrollView.getVisibility() == View.VISIBLE && recycler.getVisibility() == View.GONE
                        && recycler1.getVisibility() == View.GONE) {
                    recycler.setVisibility(View.GONE);
                    recycler1.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                    scrollView1.setVisibility(View.VISIBLE);
                    unit = "mmol/l";
                }else if (scrollView.getVisibility() == View.GONE && recycler.getVisibility() == View.GONE
                        && recycler1.getVisibility() == View.GONE) {
                    recycler.setVisibility(View.GONE);
                    recycler1.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    scrollView1.setVisibility(View.GONE);
                    unit = "mg/dl";

                }
                /*if(med ==0){
                    Task(med);
                    med = 1;
                }else{
                    Task(med);
                    med = 0;
                }*/
                isFabOpen = true;
                animateFAB();
                break;
            case R.id.show_linebar_filter:
                if (scrollView.getVisibility() == View.VISIBLE){
                    mBarChart.setVisibility(View.VISIBLE);
                    mLineChart.setVisibility(View.VISIBLE);
                    mBarChart1.setVisibility(View.GONE);
                    mLineChart1.setVisibility(View.GONE);
                }else if (scrollView1.getVisibility() == View.VISIBLE){
                    mBarChart1.setVisibility(View.VISIBLE);
                    mLineChart1.setVisibility(View.VISIBLE);
                    mBarChart.setVisibility(View.GONE);
                    mLineChart.setVisibility(View.GONE);
                }
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
        Intent i = new Intent(getApplicationContext(), AddGlucActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AsyncTask to handle the connection with the server. Recieve the response and send the data
     * using {@link MAllGlucose} class.
     * Data send: <i>Pacient DNI</i>
     * Response: JSON with data for the pacient relationed
     */
    private class DownloadAll extends AsyncTask<Void, Void, Boolean> {

        private final String mDoc;
        private final int mMed;

        DownloadAll(String doc, int med) {
            mDoc = doc;
            mMed = med;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean mBoolean = null;
            mAllGlucose = new MAllGlucose();
            try {
                jObject = mAllGlucose.getAll(mDoc);
                mInt = jObject.getInt("status");
                glucList.clear();
                if(mInt == 0){
                    mBoolean = true;
                    jArray = jObject.getJSONArray("obs_glucose");
                    for (int i = 0; i < jArray.length(); i++) {
                        String value;
                        String unit;
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
                            value = df.format(aDouble*18);
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }
                        listGluc.setUnit(unit);
                        listGluc.setValue(value);
                        glucList.add(listGluc);
                    }
                }
                else if (mInt == 1){
                    mBoolean = false;
                }

            } catch (JSONException e) {e.printStackTrace();}
            return mBoolean;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            if(aVoid){
                downloadAll = null;
                recycler.setVisibility(View.VISIBLE);
                rlayout.setVisibility(View.GONE);
                adapter = new RecyclerViewAdapter(glucList);
                recycler.setAdapter(adapter);
            }else{
                recycler.setVisibility(View.GONE);
                rlayout.setVisibility(View.VISIBLE);
                txtv.setText("Sin registros");

            }
        }
    }

    /**
     * See: {@link magnusdroid.com.glucup_2date.Controler.DayOfWeekFragment.DownloadDate}
     */
    private class DownloadDate extends AsyncTask<Void, Void, Boolean> {

        private final String mDoc;
        private final String mDate;

        DownloadDate(String doc, String date) {
            mDoc = doc;
            mDate = date;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mDateGlucose = new MDateGlucose();
            glucList.clear();
            glucList1.clear();
            yLineValues.clear();
            yBarValues.clear();
            yLineValues1.clear();
            yBarValues1.clear();
            xAxes.clear();
            xAxes1.clear();
            try {
                jObject = mDateGlucose.getDay(mDoc, mDate, "0");
                mInt = jObject.getInt("status");
                if(mInt == 1){
                    mBoolean = true;
                    jArray = jObject.getJSONArray("obs_glucose");
                    for (int i = 0; i < jArray.length(); i++) {
                        String mUnit1 = "mg/dl";
                        String mUnit2 = "mmol/l";
                        String value;
                        jObject = jArray.getJSONObject(i);
                        /*ListGluc listGluc = new ListGluc();
                        listGluc.setIssued(jObject.getString("issued"));
                        listGluc.setCode(jObject.getString("code"));
                        listGluc.setState(jObject.getString("state"));
                        if(jObject.has("performer")){
                            listGluc.setPerformer(jObject.getString("performer"));
                        }else {
                            listGluc.setPerformer(prefManager.getUser());
                        }
                        String value;
                        if(mMed == 1){ //mmol/l -> mg/dl
                            //df.setRoundingMode(RoundingMode.CEILING);
                            Double aDouble = Double.parseDouble(jObject.getString("value"));
                            value = df.format(aDouble*18);
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }
                        listGluc.setUnit(unit);
                        listGluc.setValue(value);*/
                        //ListGluc for mmol/l
                        value = jObject.getString("value");
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
                        listGluc.setUnit(mUnit1);
                        listGluc.setValue(value);
                        yLineValues.add(new Entry(Float.parseFloat(value), i));
                        yBarValues.add(new BarEntry(Float.parseFloat(value), i));
                        //ListGluc for mg/dl
                        ListGluc listGluc1 = new ListGluc();
                        listGluc1.setIssued(jObject.getString("issued"));
                        listGluc1.setCode(jObject.getString("code"));
                        listGluc1.setState(jObject.getString("state"));
                        if(jObject.has("performer")){
                            listGluc1.setPerformer(jObject.getString("performer"));
                        }else {
                            listGluc1.setPerformer(prefManager.getUser());
                        }
                        Double aDouble = Double.parseDouble(jObject.getString("value"));
                        value = String.valueOf(df.format(aDouble*18));
                        listGluc1.setUnit(mUnit2);
                        listGluc1.setValue(value);
                        glucList.add(listGluc);
                        glucList1.add(listGluc1);
                        yLineValues1.add(new Entry(Float.parseFloat(value), i));
                        yBarValues1.add(new BarEntry(Float.parseFloat(value), i));
                        xAxes.add(i, jObject.getString("issued"));
                        xAxes1.add(i, jObject.getString("issued"));
                    }
                }else if(mInt == 0){
                    mBoolean = false;
                }
            } catch (JSONException e) {e.printStackTrace();}
            return mBoolean;
        }

        @Override
        protected void onPostExecute(Boolean bb) {
            Log.d("Boolean"," "+bb);
            if(bb){
                downloadDate = null;
                rlayout.setVisibility(View.GONE);
                adapter = new RecyclerViewAdapter(glucList);
                adapter1 = new RecyclerViewAdapter(glucList1);
                recycler.setAdapter(adapter);
                recycler1.setAdapter(adapter1);
                mLineChart.animateXY(3000, 3000);
                mBarChart.animateY(2500);
                mCombiChart.animateXY(3000, 2500);
                mLineChart1.animateXY(3000, 3000);
                mBarChart1.animateY(2500);
                mCombiChart1.animateXY(3000, 2500);
                if(unit.equalsIgnoreCase("mmol/l")){
                    leftBarAxis.setAxisMinValue(0f);
                    leftBarAxis.setAxisMaxValue(10f);
                    leftLineAxis.setAxisMinValue(0f);
                    leftLineAxis.setAxisMaxValue(10f);
                    leftCombiAxis.setAxisMinValue(0f);
                    leftCombiAxis.setAxisMaxValue(10f);
                }else if(unit.equalsIgnoreCase("mg/dl")){
                    leftBarAxis1.setAxisMinValue(60f);
                    leftBarAxis1.setAxisMaxValue(190f);
                    leftLineAxis1.setAxisMinValue(60f);
                    leftLineAxis1.setAxisMaxValue(190f);
                    leftCombiAxis1.setAxisMinValue(60f);
                    leftCombiAxis1.setAxisMaxValue(190f);
                }
                //Fetch the data and populate linechart
                LineDataSet set1 = new LineDataSet(yLineValues, "Muestras");
                set1.setAxisDependency(YAxis.AxisDependency.LEFT);
                set1.setValueTextSize(15f);
                set1.setCircleColor(Color.WHITE);
                set1.setLineWidth(2f);
                MyBarDataSet set2 = new MyBarDataSet(yBarValues, "");
                set2.setValueTextSize(15f);
                set2.setColors(new int[]{ContextCompat.getColor(getApplicationContext(), R.color.normal_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.low_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.high_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.primary_text)});
                LineDataSet set3 = new LineDataSet(yLineValues1, "Muestras");
                set3.setAxisDependency(YAxis.AxisDependency.LEFT);
                set3.setValueTextSize(15f);
                set3.setCircleColor(Color.WHITE);
                set3.setLineWidth(2f);
                MyBarDataSet set4 = new MyBarDataSet(yBarValues1, "");
                set4.setValueTextSize(15f);
                set4.setColors(new int[]{ContextCompat.getColor(getApplicationContext(), R.color.normal_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.low_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.high_value),
                        ContextCompat.getColor(getApplicationContext(), R.color.primary_text)});
                // use the interface LineDataSet - BarDataSet
                ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
                lineDataSets.add(set1);
                ArrayList<BarDataSet> barDataSets = new ArrayList<>();
                barDataSets.add(set2);
                LineData lineData = new LineData(xAxes, lineDataSets);
                BarData barData = new BarData(xAxes, barDataSets);
                ArrayList<LineDataSet> lineDataSets1 = new ArrayList<>();
                lineDataSets1.add(set3);
                ArrayList<BarDataSet> barDataSets1 = new ArrayList<>();
                barDataSets1.add(set4);
                LineData lineData1 = new LineData(xAxes1, lineDataSets1);
                BarData barData1 = new BarData(xAxes1, barDataSets1);
                //Fetch the data and populate combinedchart
                LineData d = new LineData(xAxes, set1);
                d.addDataSet(set1);
                BarData db = new BarData(xAxes, set2);
                CombinedData combinedData = new CombinedData(xAxes);
                combinedData.setData(d);
                combinedData.setData(db);
                LineData d1 = new LineData(xAxes1, set3);
                d1.addDataSet(set3);
                BarData db1 = new BarData(xAxes1, set4);
                CombinedData combinedData1 = new CombinedData(xAxes1);
                combinedData1.setData(d1);
                combinedData1.setData(db1);
                // create a custom MarkerView (extend MarkerView) and specify the layout
                // to use for it
                mv = new MyMarkerView(getApplicationContext(), R.layout.custom_marker_view);
                // set the marker to the chart
                mLineChart.setMarkerView(mv);
                mLineChart1.setMarkerView(mv);
                // create a data object with the datasets
                mLineChart.setData(lineData);
                mLineChart.invalidate();
                mLineChart1.setData(lineData1);
                mLineChart1.invalidate();
                mBarChart.setData(barData);
                mBarChart.invalidate();
                mBarChart1.setData(barData1);
                mBarChart1.invalidate();
                mCombiChart.setData(combinedData);
                mCombiChart.invalidate();
                mCombiChart1.setData(combinedData1);
                mCombiChart1.invalidate();
            }else{
                recycler.setVisibility(View.GONE);
                recycler1.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                scrollView1.setVisibility(View.GONE);
                rlayout.setVisibility(View.VISIBLE);
                txtv.setText("No hay registro para este día");
                dialog();
            }
        }
    }

    /**
     * AsyncTask to handle the connection with the server. Recieve the response and send the data
     * using {@link MValueGlucose} class.
     * Data send: <i>Pacient DNI, Value fix-min-max of bloodglucose, Unit</i>
     * Response: JSON with data for the pacient relationed
     */
    private class DownloadValue extends AsyncTask<Void, Void, Boolean> {

        private final String mDoc, mFix, mMin, mMax, mUnit;
        private final int mMed;

        DownloadValue(String doc, String fix, String min, String max, String unit, int med) {
            mDoc = doc;
            mFix = fix;
            mMin = min;
            mMax = max;
            mUnit = unit;
            mMed = med;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mValueGlucose = new MValueGlucose();
            glucList.clear();
            try {
                jObject = mValueGlucose.getValue(mDoc, mFix, mMin, mMax, mUnit);
                mBoolean = jObject.getBoolean("status");
                if(mBoolean){
                    jArray = jObject.getJSONArray("obs_glucose");
                    for (int i = 0; i < jArray.length(); i++) {
                        String value;
                        String unit;
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
                            value = df.format(aDouble*18);
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }
                        listGluc.setUnit(unit);
                        listGluc.setValue(value);
                        glucList.add(listGluc);
                    }
                }else{
                    mBoolean = false;
                }
            } catch (JSONException e) {e.printStackTrace();}
            return mBoolean;
        }

        @Override
        protected void onPostExecute(Boolean bb) {
            if(bb){
                downloadValue = null;
                recycler.setVisibility(View.VISIBLE);
                rlayout.setVisibility(View.GONE);
                adapter = new RecyclerViewAdapter(glucList);
                recycler.setAdapter(adapter);
            }else{
                recycler.setVisibility(View.GONE);
                rlayout.setVisibility(View.VISIBLE);
                txtv.setText("No hay registro con el valor ingresado");
                dialog();
            }
        }
    }

    /**
     * Class to show dialog: Records no found !!
     */
    public void dialog() {
        String alert= getString(R.string.filter_record_not_found);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.filter_record_title))
                .setIcon(R.mipmap.alert)
                .setMessage(alert)
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(ListGlucoseActivity.this, PacientActivity.class);
                                startActivity(i);
                                finish();
                                dialog.cancel();
                            }
                        });
        builder1.show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
    }

}
