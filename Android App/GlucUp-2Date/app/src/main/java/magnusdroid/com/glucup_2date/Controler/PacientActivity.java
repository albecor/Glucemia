package magnusdroid.com.glucup_2date.Controler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import magnusdroid.com.glucup_2date.Class.Global;
import magnusdroid.com.glucup_2date.Model.PrefManager;
import magnusdroid.com.glucup_2date.R;

/**
 * This activity is launched when a user is a Pacient. Has a NavigationDrawer menu, each option
 * inflate a different fragment to replace the container and show in the main thread
 */
public class PacientActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // UI references.
    private TextView mNameView;
    // Get shared preferences
    private PrefManager prefManager;
    private Global global = new Global();
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacient);
        // Set up shared preferences.
        prefManager = new PrefManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_patient);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(checkNetwork()) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }else{
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        mNameView = (TextView) header.findViewById(R.id.NameHeader);
        mNameView.setText(prefManager.getUser());

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class clase = PacientListFragment.class;
            try {
                fragment = (Fragment) clase.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int t = 0;
        Fragment fragment = null;
        Class fragmentClass = null;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_list) {
            fragmentClass = PacientListFragment.class;
        } else if (id == R.id.nav_graphic) {
            fragmentClass = ChartFragment.class;
        } else if (id == R.id.nav_reminder) {
            fragmentClass = AlarmFragment.class;
        } else if (id == R.id.nav_help) {
            fragmentClass = HelpFragment.class;
        } else if (id == R.id.nav_info) {
            t = 1;
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_web) {
            t = 1;
            Intent myWebLink = new Intent(Intent.ACTION_VIEW);
            myWebLink.setData(Uri.parse(global.getUrl()+"Glucometrias"));
            startActivity(myWebLink);
        } else if (id == R.id.nav_off) {
            fragmentClass = LogOutFragment.class;
        }

        if( t  == 0) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
            // Highlight the selected item, update the title, and close the drawer
            item.setChecked(true);
            setTitle(item.getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Check network connection
    private boolean checkNetwork(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
