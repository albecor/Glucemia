package magnusdroid.com.glucup_2date.Controler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import magnusdroid.com.glucup_2date.Model.MDetailPacient;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;

/**
 * Activity to search patient with no relationship. Use ThemDialog style for show as
 * a Dialog and appear on top from the UI. If a patient is found, send the response to {@link PacienteDetailActivity}
 */
public class SearchActivity extends AppCompatActivity {

    //UI References
    private EditText txt_search;
    private Button btn_search;
    private SearchPacientTask mSearchTask;
    // Model
    private MDetailPacient mDetail;
    // JsonObject response from server
    private JSONObject jObject;
    // Utilities
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Set up toolbar and action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txt_search = (EditText) findViewById(R.id.txt_search);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PacienteListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search() {
        txt_search.setError(null);
        boolean cancel;
        View focusView = null;
        String doc = txt_search.getText().toString();
        // Check for empty field
        if(TextUtils.isEmpty(doc)){
            error(getString(R.string.field_empties));
            focusView = txt_search;
            cancel = true;
        }else {
            cancel = false;
        }

        if (cancel) {
            // There was an error; don't attempt send and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Kick off a background task to perform the send attempt.
            progress = new ProgressDialog(SearchActivity.this);
            progress.setMessage("Buscando (:");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
            mSearchTask = new SearchPacientTask(doc);
            mSearchTask.execute();
        }
    }

    public class SearchPacientTask extends AsyncTask<Void, Void, Integer> {

        private final String mDoc;

        SearchPacientTask(String doc) {
            mDoc = doc;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(c.getTime());
            mDetail = new MDetailPacient();
            int mInt = 0;
            try {
                jObject = mDetail.getDetail(mDoc, date);
                mInt = jObject.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mInt;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progress.dismiss();
            if(integer == 0 || integer == 1){
                mSearchTask = null;
                Intent intent = new Intent(getApplicationContext(), PacienteDetailActivity.class);
                intent.putExtra(PacienteDetailFragment.PACIENT_ID, mDoc);
                startActivity(intent);
                finish();
            }else if (integer == 2){
                dialog();
            }
        }
    }


    /**
     *
     * @param msj String msj to show in the error dialog caused by an issued in the entries of
     *            data
     */
    private  void error(String msj){
        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
        alertDialog.setTitle("Alerta");
        alertDialog.setMessage(msj);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    /**
     * Class to show dialog: Pacient no found !!
     */
    private void dialog() {
        String alert= "Paciente no encontrado";
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                .setIcon(R.mipmap.alert)
                .setMessage(alert)
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        builder1.show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
    }
}
