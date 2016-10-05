package magnusdroid.com.glucup_2date.Controler;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;

/**
 * Activity to search record by Date, Week, Value or All of them. Use ThemDialog style for show as
 * a Dialog and appear on top from the UI. Send the filter to {@link ListGlucoseActivity} or
 * {@link WeekFilterGlucose}.
 */
public class FilterActivity extends AppCompatActivity
        implements CalendarDatePickerDialogFragment.OnDateSetListener {

    // UI references.
    RelativeLayout Day, Week, Value;
    Spinner filter_unit_spinner;
    Button button_day, button_week, btn_show;
    EditText txtDate, txtMin, txtMax, txtFix;
    RadioGroup rg;
    RadioButton rbAll, rbValue;
    TextView txtSWeek, txtWeek;
    Switch switch_filter;
    //Utilites
    String[] days;
    String plan, tDate, tWeek, tMin, tMax, tFix;
    private int mFlag;
    private String mPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mFlag = extras.getInt("flag");

        Day = (RelativeLayout) findViewById(R.id.dia_layout);
        Week = (RelativeLayout) findViewById(R.id.semana_layout);
        Value = (RelativeLayout) findViewById(R.id.value_layout);
        button_day = (Button) findViewById(R.id.btn_date);
        button_week = (Button) findViewById(R.id.btn_week);
        btn_show = (Button) findViewById(R.id.btn_show);
        txtDate = (EditText) findViewById(R.id.in_date);
        txtWeek = (TextView) findViewById(R.id.in_week);
        txtMin = (EditText) findViewById(R.id.min_value);
        txtMax = (EditText) findViewById(R.id.max_value);
        txtFix = (EditText) findViewById(R.id.unique_value);
        txtSWeek = (TextView) findViewById(R.id.week_set);
        filter_unit_spinner = (Spinner) findViewById(R.id.filter_unit_spinner);
        rbAll = (RadioButton) findViewById(R.id.show_all);
        rbValue = (RadioButton) findViewById(R.id.show_by_value);
        switch_filter = (Switch) findViewById(R.id.switch_filter);
        switch_filter.setTextOn("Lista");
        switch_filter.setTextOff("Gráfica");

        //0 = Show "All" option, 1 = Hide "All" option
        if (mFlag == 0){
            assert rbAll != null;
            switch_filter.setVisibility(View.GONE);
            rbAll.setVisibility(View.VISIBLE);
            rbValue.setVisibility(View.VISIBLE);
        }else if (mFlag == 1){
            assert rbAll != null;
            switch_filter.setVisibility(View.GONE);
            rbAll.setVisibility(View.GONE);
            rbValue.setVisibility(View.GONE);
        }else if (mFlag == 2){
            assert rbAll != null;
            switch_filter.setVisibility(View.VISIBLE);
            switch_filter.setChecked(true);
            rbAll.setVisibility(View.VISIBLE);
            rbValue.setVisibility(View.VISIBLE);
            mPatient = extras.getString("pacient");
        }

        //attach a listener to check for changes in state
        switch_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked){
                    switch_filter.setText("Mostrar lista");
                }else{
                    switch_filter.setText("Mostrar gráfica");
                }

            }
        });

        rg = (RadioGroup) findViewById(R.id.radioGroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) findViewById(checkedId);
                if (rb != null) {
                    plan = (String) rb.getText();
                }
                //Handle the RadioGroup and show the option to entry filter
                if (plan.equalsIgnoreCase("Día") || plan.equalsIgnoreCase("Day")) {
                    switch_filter.setEnabled(true);
                    switch_filter.setClickable(true);
                    btn_show.setVisibility(View.VISIBLE);
                    Value.setVisibility(View.GONE);
                    Week.setVisibility(View.GONE);
                    Day.setVisibility(View.VISIBLE);
                    button_day.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                                    .setOnDateSetListener(FilterActivity.this);
                            cdp.show(getSupportFragmentManager(), AddGlucActivity.FRAG_TAG_DATE_PICKER);
                        }
                    });
                } else if (plan.equalsIgnoreCase("Semana") || plan.equalsIgnoreCase("Week")) {
                    switch_filter.setEnabled(true);
                    switch_filter.setClickable(true);
                    Value.setVisibility(View.GONE);
                    btn_show.setVisibility(View.VISIBLE);
                    Day.setVisibility(View.GONE);
                    Week.setVisibility(View.VISIBLE);
                    button_week.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                                    .setOnDateSetListener(FilterActivity.this);
                            cdp.show(getSupportFragmentManager(), AddGlucActivity.FRAG_TAG_DATE_PICKER);
                        }
                    });
                } else if (plan.equalsIgnoreCase("Valor") || plan.equalsIgnoreCase("Value")) {
                    switch_filter.setClickable(false);
                    switch_filter.setEnabled(false);
                    Value.setVisibility(View.VISIBLE);
                    btn_show.setVisibility(View.VISIBLE);
                    Day.setVisibility(View.GONE);
                    Week.setVisibility(View.GONE);
                    txtFix.addTextChangedListener(new TextWatcher() {
                        //Enable TextView range of value only when FixValue is empty
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            enableEditText();
                        }
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                } else if (plan.equalsIgnoreCase("Todos") || plan.equalsIgnoreCase("All")) {
                    switch_filter.setEnabled(false);
                    switch_filter.setClickable(false);
                    Value.setVisibility(View.GONE);
                    btn_show.setVisibility(View.VISIBLE);
                    Day.setVisibility(View.GONE);
                    Week.setVisibility(View.GONE);
                }
            }
        });

        //Get the data from the Widget and pass it to Activity to show the records
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_filter.getVisibility() == View.VISIBLE) {
                    if (switch_filter.isChecked()) {
                        mFlag = 3;
                    } else {
                        mFlag = 4;
                    }
                }
                Intent i = new Intent(FilterActivity.this, ListGlucoseActivity.class);
                tDate = txtDate.getText().toString();
                tWeek = txtWeek.getText().toString();
                tMin = txtMin.getText().toString();
                tMax = txtMax.getText().toString();
                tFix = txtFix.getText().toString();
                if (!tDate.isEmpty() && tWeek.isEmpty() && tMin.isEmpty() && tMax.isEmpty() && tFix.isEmpty()){ //Show by Date
                    i.putExtra("date", tDate);
                    i.putExtra("flag", mFlag);
                    i.putExtra("patient", mPatient);
                    startActivity(i);
                    finish();
                } else if (!tWeek.isEmpty() && tDate.isEmpty() && tMin.isEmpty() && tMax.isEmpty() && tFix.isEmpty()){ //Show by Week
                    Intent j = new Intent(FilterActivity.this, WeekFilterGlucose.class);
                    j.putExtra("week", days);
                    j.putExtra("flag", mFlag);
                    j.putExtra("patient", mPatient);
                    startActivity(j);
                    finish();
                } else if ((!tFix.isEmpty() || !tMin.isEmpty() || !tMax.isEmpty()) && tDate.isEmpty() && tWeek.isEmpty()){ //Show by Value
                    String unit = filter_unit_spinner.getSelectedItem().toString();
                    i.putExtra("date", "valor");
                    i.putExtra("flag", mFlag);
                    i.putExtra("Vfix", tFix);
                    i.putExtra("Vmin", tMin);
                    i.putExtra("Vmax", tMax);
                    i.putExtra("Unit", unit);
                    i.putExtra("patient", mPatient);
                    startActivity(i);
                    finish();
                }else if (tDate.isEmpty() && tWeek.isEmpty() && tMin.isEmpty() && tMax.isEmpty()){ //Show All
                    i.putExtra("date", "todos");
                    i.putExtra("flag", mFlag);
                    i.putExtra("patient", mPatient);
                    startActivity(i);
                    finish();
                }else{
                    dialog();
                }
            }
        });

    }

    private void enableEditText() {
        boolean isRdy = !(txtFix.getText().toString().length()>0);
        txtMin.setFocusable(isRdy);
        txtMax.setFocusable(isRdy);
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
     * {@link AddGlucActivity#onDateSet(CalendarDatePickerDialogFragment, int, int, int)}
     * @param dialog
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        if(Day.getVisibility() == View.VISIBLE) {
            String monthConverted = converted(monthOfYear+1);
            String dayConverted = converted(dayOfMonth);
            txtDate.setText(getString(R.string.calendar_date_picker_value, year, monthConverted, dayConverted));
        }else{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            try {
                Date date = format.parse(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                cal.setTime(date);
                int delta = -cal.get(Calendar.DAY_OF_WEEK) + 1; //add 2 if your week start on monday
                cal.add(Calendar.DAY_OF_MONTH, delta );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            days = new String[7];
            for (int i = 0; i < 7; i++)
            {
                days[i] = format.format(cal.getTime());

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            txtWeek.setText(Arrays.toString(days));
            txtSWeek.setText(getString(R.string.calendar_week_picker_value, days[0], days[6]));
        }

    }

    /**
     * Class to show dialog when a wrong value is set in texview. The dialog has sound and vibration
     * to perfom the notification
     */
    private void dialog() {
        String alert= getString(R.string.check_value);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.field_empties))
                .setIcon(R.mipmap.alert)
                .setMessage(alert)
                .setNeutralButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        builder1.show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
        MediaPlayer player = MediaPlayer.create(this, R.raw.alert_value);
        player.start();
    }

    /**
     *
     * @param date Int date to rounded with 0 when is 1digit
     * @return ei 01, 02,.. 09
     */
    private String converted(int date){
        String s = ""+date;
        if(date < 10){
            s = "0"+s;
        }
        return s;
    }

}
