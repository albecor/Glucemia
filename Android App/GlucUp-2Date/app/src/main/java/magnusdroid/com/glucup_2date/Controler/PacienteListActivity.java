package magnusdroid.com.glucup_2date.Controler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import magnusdroid.com.glucup_2date.Class.Global;
import magnusdroid.com.glucup_2date.Model.MListPacient;
import magnusdroid.com.glucup_2date.Model.Pacient;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.Model.ViewDialog;
import magnusdroid.com.glucup_2date.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Pacientes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PacienteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PacienteListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private View recyclerView;
    private FrameLayout frameLayout, frameLayout1;
    // Keep track of the task to ensure we can cancel it if requested.
    private DownloadList mAuthTask = null;
    // Utilities
    private ProgressDialog progress;
    private List<Pacient> pacientList = new ArrayList<>();
    private Global global = new Global();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_list);
        // Set up shared preferences.
        final PrefManager prefManager = new PrefManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_personal);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_personal);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                i.putExtra("flag", 0);
                startActivity(i);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout1 = (FrameLayout) findViewById(R.id.frameLayout1);

        recyclerView = findViewById(R.id.paciente_list);
        /*assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);*/

        if (findViewById(R.id.paciente_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        progress = new ProgressDialog(PacienteListActivity.this);
        progress.setMessage(getString(R.string.fetching_data));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        mAuthTask = new DownloadList(prefManager.getDoc());
        mAuthTask.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.personal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_personal_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_personal_help){
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_personal_web){
            Intent myWebLink = new Intent(Intent.ACTION_VIEW);
            myWebLink.setData(Uri.parse(global.getUrl()));
            startActivity(myWebLink);
        }else if(id == R.id.action_personal_logout){
            ViewDialog alert = new ViewDialog();
            alert.LogOutDialog(PacienteListActivity.this, getApplicationContext());
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<Pacient> pacientList) {
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext()));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(pacientList));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>{

        private final List<Pacient> mValues;

        public SimpleItemRecyclerViewAdapter(List<Pacient> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.paciente_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).subject);
            holder.mName = mValues.get(position).subject;
            /*holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);*/

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(PacienteDetailFragment.PACIENT_ID, holder.mItem.id);
                        arguments.putString(PacienteDetailFragment.PACIENT_NAME, holder.mName);
                        //arguments.putString(PacienteDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        PacienteDetailFragment fragment = new PacienteDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.paciente_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PacienteDetailActivity.class);
                        intent.putExtra(PacienteDetailFragment.PACIENT_ID, holder.mItem.id);
                        intent.putExtra(PacienteDetailFragment.PACIENT_NAME, holder.mName);
                        //intent.putExtra(PacienteDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mContentView;
            public final ImageView mImageView;
            public Pacient mItem;
            public String mName;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.txt_name_pacient);
                mImageView = (ImageView) view.findViewById(R.id.icon_pacient);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    /**
     * AsyncTask to handle the connection with the server. Recieve the response and send the data
     * using {@link MListPacient} class.
     * Data send: <i>Stafff DNI</i>
     * Response: JSON with list of pacients
     */
    private class DownloadList extends AsyncTask<Object, Object, Integer> {

        private final String mDoc;

        DownloadList(String doc) {
            mDoc = doc;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            pacientList.clear();
            MListPacient mList = new MListPacient();
            int mInteger = 100;
            try {
                JSONObject jObject = mList.getList(mDoc);
                mInteger = jObject.getInt("status");
                if(mInteger == 0){
                    JSONArray jArray = jObject.getJSONArray("pacient");
                    for (int i = 0; i < jArray.length(); i++) {
                        jObject = jArray.getJSONObject(i);
                        Pacient pacient = new Pacient(jObject.getString("id"), jObject.getString("subject"));
                        pacientList.add(pacient);
                    }
                }
            } catch (JSONException e) {e.printStackTrace();}
            return mInteger;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            progress.dismiss();
            if(aVoid == 0 || aVoid == 1) {
                mAuthTask = null;
                frameLayout.setVisibility(View.VISIBLE);
                frameLayout1.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                setupRecyclerView((RecyclerView) recyclerView, pacientList);
            }else {
                recyclerView.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                frameLayout1.setVisibility(View.VISIBLE);
                //txtlayout.setText(getString(R.string.login_issued_server));
            }
        }
    }

    /**
     * Class to draw a divider item to the list of the Pacient. Inflate the layout and add params
     * using canvas class
     */
    public class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public DividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(context,R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

}
