package magnusdroid.com.glucup_2date.Controler;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import magnusdroid.com.glucup_2date.Model.ListGluc;
import magnusdroid.com.glucup_2date.Model.MAllGlucose;
import magnusdroid.com.glucup_2date.Model.MDateGlucose;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;

/**
 * A Fragment to list the most recent glucose record. Called by item in the NavigationDrawer on
 * {@link PacientActivity}. This fragment has a FAB list to access {@link AddGlucActivity} or change
 * the unit of the records
 */
public class PacientListFragment extends Fragment implements View.OnClickListener {

    // Keep track of the gluc list task to ensure we can cancel it if requested.
    private DownloadList mAuthTask = null;
    // UI references.
    private FloatingActionButton mFab, mFab1, mFab2;
    private RecyclerView recycler, recycler1;
    private RelativeLayout rLayout, relvalue, rNewpatient;
    private TextView txtlayout, ldate_text;
    // Get shared preferences
    private PrefManager prefManager;
    // Utilities
    private ProgressDialog progress;
    private String lDate;
    private List<ListGluc> glucList = new ArrayList<>();
    private List<ListGluc> glucList1 = new ArrayList<>();
    private Boolean isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;

    public PacientListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pacient_list, container, false);

        prefManager = new PrefManager(getContext());

        mFab = (FloatingActionButton) view.findViewById(R.id.fab_pacient);
        mFab1 = (FloatingActionButton) view.findViewById(R.id.fab_pacient1);
        mFab2 = (FloatingActionButton) view.findViewById(R.id.fab_pacient2);
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_backward);
        rLayout = (RelativeLayout) view.findViewById(R.id.nonvalue);
        relvalue = (RelativeLayout) view.findViewById(R.id.relvalue);
        rNewpatient = (RelativeLayout) view.findViewById(R.id.newpatient);
        txtlayout = (TextView) view.findViewById(R.id.nonvalue_text);
        ldate_text = (TextView) view.findViewById(R.id.ldate_text);
        mFab.setOnClickListener(this);
        mFab1.setOnClickListener(this);
        mFab2.setOnClickListener(this);

        recycler = (RecyclerView) view.findViewById(R.id.reciclador);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);
        recycler.setItemAnimator(new DefaultItemAnimator());

        recycler1 = (RecyclerView) view.findViewById(R.id.reciclador1);
        recycler1.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager1 = new LinearLayoutManager(getContext());
        recycler1.setLayoutManager(lManager1);
        recycler1.setItemAnimator(new DefaultItemAnimator());

        progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.fetching_data));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        Task();

        return view;
    }

    /**
     *
     * See: {@link ChartFragment#Task(int)}
     */
    private void Task(){
        progress.show();
        mAuthTask = new DownloadList(prefManager.getDoc());
        mAuthTask.execute();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d("ID FAB"," "+id);
        switch (id){
            case R.id.fab_pacient:
                animateFAB();
                break;
            case R.id.fab_pacient1:
                animateFAB();
                newGluc();
                break;
            case R.id.fab_pacient2:
                if (recycler.getVisibility() == View.VISIBLE) {
                    recycler.setVisibility(View.GONE);
                    recycler1.setVisibility(View.VISIBLE);
                } else{
                    recycler.setVisibility(View.VISIBLE);
                    recycler1.setVisibility(View.GONE);
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
        i.putExtra("flag", 0);
        startActivity(i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pacient_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.parse_value:
                Intent i = new Intent(getContext(), FilterActivity.class);
                i.putExtra("flag", 0);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * AsyncTask to handle the connection with the server. Recieve the response and send the data
     * using {@link MDateGlucose} class.
     * Data send: <i>Pacient DNI, current datetime</i>
     * Response: JSON with the most recent record
     */
    private class DownloadList extends AsyncTask<Void, Void, Integer>{

        private final String mDoc;

        DownloadList(String doc) {
            mDoc = doc;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            Calendar c = Calendar.getInstance();
            glucList.clear();
            glucList1.clear();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(c.getTime());
            MDateGlucose mList = new MDateGlucose();
            int mInteger;
            try {
                JSONObject jObject = mList.getDay(mDoc, date, "0");
                mInteger = jObject.getInt("status");
                Log.d("LIST"," "+jObject);
                if(mInteger == 1) {
                    lDate = jObject.getString("date");
                    JSONArray jArray = jObject.getJSONArray("obs_glucose");
                    for (int i = 0; i < jArray.length(); i++) {
                        String mUnit2 = "mg/dl";
                        String mUnit1 = "mmol/l";
                        jObject = jArray.getJSONObject(i);
                        //ListGluc for mmol/l
                        ListGluc listGluc = new ListGluc();
                        listGluc.setIssued(jObject.getString("issued"));
                        listGluc.setCode(jObject.getString("code"));
                        listGluc.setState(jObject.getString("state"));
                        if (jObject.has("performer")) {
                            listGluc.setPerformer(jObject.getString("performer"));
                        } else {
                            listGluc.setPerformer(prefManager.getUser());
                        }
                        listGluc.setUnit(mUnit1);
                        listGluc.setValue(jObject.getString("value"));
                        //ListGluc for mg/dl
                        ListGluc listGluc1 = new ListGluc();
                        listGluc1.setIssued(jObject.getString("issued"));
                        listGluc1.setCode(jObject.getString("code"));
                        listGluc1.setState(jObject.getString("state"));
                        if (jObject.has("performer")) {
                            listGluc1.setPerformer(jObject.getString("performer"));
                        } else {
                            listGluc1.setPerformer(prefManager.getUser());
                        }
                        Double aDouble = Double.parseDouble(jObject.getString("value"));
                        String value = String.valueOf(df.format(aDouble * 18));
                        listGluc1.setUnit(mUnit2);
                        listGluc1.setValue(value);
                        /*if(mMed == 1){ //mmol/l -> mg/dl
                            Double aDouble = Double.parseDouble(jObject.getString("value"));
                            value = String.valueOf(df.format(aDouble*18));
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }*/
                        glucList.add(listGluc);
                        glucList1.add(listGluc1);
                    }
                }else {
                    mInteger = 2;
                }
            } catch (JSONException e) {e.printStackTrace();mInteger = 3;}
            return mInteger;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            progress.dismiss();
            if(aVoid == 1) {
                mAuthTask = null;
                relvalue.setVisibility(View.VISIBLE);
                RecyclerView.Adapter adapter = new RecyclerViewAdapter(glucList);
                RecyclerView.Adapter adapter1 = new RecyclerViewAdapter(glucList1);
                recycler.setAdapter(adapter);
                recycler.setVisibility(View.VISIBLE);
                recycler1.setAdapter(adapter1);
                recycler1.setVisibility(View.GONE);
                ldate_text.setText(getString(R.string.last_issued, lDate));
                rLayout.setVisibility(View.GONE);
            }else if(aVoid == 2){
                mAuthTask = null;
                mFab2.setVisibility(View.GONE);
                relvalue.setVisibility(View.GONE);
                rNewpatient.setVisibility(View.VISIBLE);
                rLayout.setVisibility(View.GONE);
            }
            else {
                recycler.setVisibility(View.GONE);
                recycler1.setVisibility(View.GONE);
                relvalue.setVisibility(View.GONE);
                rLayout.setVisibility(View.VISIBLE);
                txtlayout.setText(getString(R.string.login_issued_server));
                mFab.setClickable(false);
            }
        }
    }

}
