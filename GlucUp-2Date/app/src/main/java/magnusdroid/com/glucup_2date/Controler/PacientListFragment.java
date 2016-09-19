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
    private RecyclerView recycler;
    private RelativeLayout rLayout, lLayout;
    private TextView txtlayout, ldate_text;
    // Get shared preferences
    private PrefManager prefManager;
    // Utilities
    private ProgressDialog progress;
    private String lDate;
    private List<ListGluc> glucList = new ArrayList<>();
    private Boolean isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private int med;


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
        lLayout = (RelativeLayout) view.findViewById(R.id.relvalue);
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
     * See: {@link ChartFragment#Task(int)}
     * @param Mmed int Unit = mmol/l or mg/dl
     */
    private void Task(int Mmed){
        progress.show();
        mAuthTask = new DownloadList(prefManager.getDoc(), Mmed);
        mAuthTask.execute();
        med = 0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab_pacient:
                animateFAB();
                break;
            case R.id.fab_pacient1:
                animateFAB();
                newGluc();
                break;
            case R.id.fab_pacient2:
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
    private class DownloadList extends AsyncTask<Void, Void, Boolean>{

        private final String mDoc;
        private final int mMed;

        DownloadList(String doc, int med) {
            mDoc = doc;
            mMed = med;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean aBoolean = true;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            Calendar c = Calendar.getInstance();
            glucList.clear();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(c.getTime());
            MDateGlucose mList = new MDateGlucose();
            int mInteger;
            try {
                JSONObject jObject = mList.getDay(mDoc, date, "0");
                mInteger = jObject.getInt("status");
                if (mInteger == 1){
                    aBoolean = true;
                    JSONArray jArray = jObject.getJSONArray("obs_glucose");
                    lDate = jObject.getString("date");
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
                            listGluc.setPerformer(prefManager.getUser());
                        }
                        if(mMed == 1){ //mmol/l -> mg/dl
                            Double aDouble = Double.parseDouble(jObject.getString("value"));
                            value = String.valueOf(df.format(aDouble*18));
                            unit = "mg/dl";
                        }else{
                            value = jObject.getString("value");
                            unit  = "mmol/l";
                        }
                        listGluc.setUnit(unit);
                        listGluc.setValue(value);
                        glucList.add(listGluc);
                    }
                }else{aBoolean = false;}
            } catch (JSONException e) {e.printStackTrace();}
            return aBoolean;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            progress.dismiss();
            if(aVoid) {
                mAuthTask = null;
                RecyclerView.Adapter adapter = new RecyclerViewAdapter(glucList);
                recycler.setAdapter(adapter);
                recycler.setVisibility(View.VISIBLE);
                lLayout.setVisibility(View.VISIBLE);
                ldate_text.setText(getString(R.string.last_issued, lDate));
                rLayout.setVisibility(View.GONE);
            }else{
                recycler.setVisibility(View.GONE);
                rLayout.setVisibility(View.VISIBLE);
                txtlayout.setText(getString(R.string.login_issued_server));
                mFab.setClickable(false);
            }
        }
    }

}
