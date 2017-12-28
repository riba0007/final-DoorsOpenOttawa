package ca.edumedia.riba0007.doorsopenottawa;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;
import ca.edumedia.riba0007.doorsopenottawa.services.DOOService;
import ca.edumedia.riba0007.doorsopenottawa.utils.NetworkHelper;

import static ca.edumedia.riba0007.doorsopenottawa.services.DOOService.REQUEST_SERVICE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.ACTIVITY_EDIT_BUILDING;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.ACTIVITY_NEW_BUILDING;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BASE_URL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_ACADEMIC;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_BUSINESS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_COMMUNITY;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_EMBASSIES;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_FAVORITES;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_FUNCTIONAL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_GALLERIES;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_GOVERNMENT;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_MUSEUMS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_MY_BUILDINGS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_OTHER;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_RELIGIOUS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BUILDING_CATEGORY_SPORTS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.NO_SELECTED_CATEGORY_ID;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PREFERENCES_FAVORITES;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PREFERENCES_FILTER;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PREFERENCES_MY_BUILDINGS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PREFERENCES_SORT;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.RESULT_MESSAGE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.RESULT_OBJECT;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.SECURE_URL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.SORT_A_Z;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.SORT_Z_A;

/**
 *  Final Android Assigment - Doors Open App
 *  Show a list of buildings. Add, Edit and delete buildings
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ABOUT_DIALOG_TAG = "About Dialog";

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private ArrayList<BuildingPOJO> mBuildingsList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int selectedFilter;
    private int selectedSort;

    protected List<Integer> mFavorites;
    protected List<Integer> mMyBuildings;

    private SharedPreferences settings;

    BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(DOOService.SERVICE_PAYLOAD)) {
                String message = intent.hasExtra(DOOService.SERVICE_MESSAGE) ? intent.getStringExtra(DOOService.SERVICE_MESSAGE) : "";

                if (message.isEmpty()){
                    BuildingPOJO[] buildingsArray = (BuildingPOJO[]) intent.getParcelableArrayExtra(DOOService.SERVICE_PAYLOAD);
                    Toast.makeText(MainActivity.this,
                            "Received " + buildingsArray.length + " buildings from service",
                            Toast.LENGTH_SHORT).show();
                    //Save the Buildings as a global member field in a List or ArrayList
                    mBuildingsList = new ArrayList<>(Arrays.asList(buildingsArray));
                    displayBuildings();
                } else {
                    BuildingPOJO buildingPOJO = (BuildingPOJO) intent.getParcelableExtra(DOOService.SERVICE_PAYLOAD);

                    if (message.equals(DOOService.SERVICE_DELETE)) {
                        Toast.makeText(getApplicationContext(),"Building " + buildingPOJO.getBuildingId() + " deleted.",Toast.LENGTH_SHORT).show();
                        mMyBuildings.remove(buildingPOJO.getBuildingId());
                        fetchBuildings();
                    }
                }
           } else if (intent.hasExtra(DOOService.SERVICE_EXCEPTION)) {
                String message = intent.getStringExtra(DOOService.SERVICE_EXCEPTION);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //sharedPreferences
        settings = getSharedPreferences( getResources().getString(R.string.app_name), Context.MODE_PRIVATE );

        //filter selections
        selectedFilter = settings.getInt(PREFERENCES_FILTER, NO_SELECTED_CATEGORY_ID);
        selectedSort = settings.getInt(PREFERENCES_SORT, NO_SELECTED_CATEGORY_ID);

        //favorite list
        mFavorites = stringToIntList(settings.getString(PREFERENCES_FAVORITES, ""));

        //my_buildings
        mMyBuildings = stringToIntList(settings.getString(PREFERENCES_MY_BUILDINGS, ""));

        //add swipeRefresh behaviour
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchBuildings();
            }
        });

        //recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.building_list);
        //get the adapter for the recyclerview ready
        displayBuildings();

        //register the service
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_LIST));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_DELETE));

        //try to retrieve the list of buildings
        fetchBuildings();

        //fab button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewBuildingActivity.class);
                startActivityForResult(intent, ACTIVITY_NEW_BUILDING);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        //save preferences
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFERENCES_FILTER, selectedFilter);
        editor.putInt(PREFERENCES_SORT, selectedSort);
        editor.putString(PREFERENCES_FAVORITES, intListToString(mFavorites));
        editor.putString(PREFERENCES_MY_BUILDINGS, intListToString(mMyBuildings));

        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        updateCheckedMenus(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_about:
                DialogFragment newFragment = new AboutDialogFragment();
                newFragment.show(getFragmentManager(), ABOUT_DIALOG_TAG);
                break;
            case R.id.action_filter_none:
                selectedFilter = NO_SELECTED_CATEGORY_ID;
                fetchBuildings();
                break;
            case R.id.action_filter_religious:
                selectedFilter = BUILDING_CATEGORY_RELIGIOUS;
                fetchBuildings();
                break;
            case R.id.action_filter_embassies:
                selectedFilter = BUILDING_CATEGORY_EMBASSIES;
                fetchBuildings();
                break;
            case R.id.action_filter_government:
                selectedFilter = BUILDING_CATEGORY_GOVERNMENT;
                fetchBuildings();
                break;
            case R.id.action_filter_functional:
                selectedFilter = BUILDING_CATEGORY_FUNCTIONAL;
                fetchBuildings();
                break;
            case R.id.action_filter_galleries:
                selectedFilter = BUILDING_CATEGORY_GALLERIES;
                fetchBuildings();
                break;
            case R.id.action_filter_academic:
                selectedFilter = BUILDING_CATEGORY_ACADEMIC;
                fetchBuildings();
                break;
            case R.id.action_filter_sports:
                selectedFilter = BUILDING_CATEGORY_SPORTS;
                fetchBuildings();
                break;
            case R.id.action_filter_community:
                selectedFilter = BUILDING_CATEGORY_COMMUNITY;
                fetchBuildings();
                break;
            case R.id.action_filter_business:
                selectedFilter = BUILDING_CATEGORY_BUSINESS;
                fetchBuildings();
                break;
            case R.id.action_filter_museums:
                selectedFilter = BUILDING_CATEGORY_MUSEUMS;
                fetchBuildings();
                break;
            case R.id.action_filter_other:
                selectedFilter = BUILDING_CATEGORY_OTHER;
                fetchBuildings();
                break;
            case R.id.action_filter_my_buildings:
                selectedFilter = BUILDING_CATEGORY_MY_BUILDINGS;
                fetchBuildings();
                break;
            case R.id.action_filter_favorites:
                selectedFilter = BUILDING_CATEGORY_FAVORITES;
                fetchBuildings();
                break;
        }

        if ( item.isCheckable() ) {
            // remember which sort option the user picked
            item.setChecked( true );

            // which sort menu item did the user pick?
            switch( item.getItemId() ) {
                case R.id.action_sort_name_asc:
                    selectedSort = SORT_A_Z;
                    mAdapter.sortByNameAscending();
                    Log.i( TAG, "Sorting buildings by name (a-z)" );
                    break;

                case R.id.action_sort_name_dsc:
                    selectedSort = SORT_Z_A;
                    mAdapter.sortByNameDescending();
                    Log.i( TAG, "Sorting buildings by name (z-a)" );
                    break;
            }

            return true;
        } // END if item.isChecked()

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        BuildingPOJO buildingPOJO = new BuildingPOJO();

        if (resultCode == RESULT_OK) {
            if (resultIntent.hasExtra(RESULT_OBJECT)) {
                buildingPOJO = resultIntent.getParcelableExtra(RESULT_OBJECT);

                switch (requestCode) {
                    case ACTIVITY_NEW_BUILDING:
                        Toast.makeText(this,"New building Added",Toast.LENGTH_SHORT).show();
                        mMyBuildings.add(buildingPOJO.getBuildingId());
                        break;
                    case ACTIVITY_EDIT_BUILDING:
                        if (resultIntent.hasExtra(RESULT_MESSAGE)) {
                            String resultMessage = resultIntent.getStringExtra(RESULT_MESSAGE);
                            if (resultMessage.equals(DOOService.SERVICE_DELETE)) {
                                Toast.makeText(this,"Building " + buildingPOJO.getBuildingId() + " deleted.",Toast.LENGTH_SHORT).show();
                                mMyBuildings.remove(buildingPOJO.getBuildingId());
                            } else {
                                Toast.makeText(this,"Building " + buildingPOJO.getBuildingId() + " saved.",Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }

                fetchBuildings();

            }
        }

    }

    //convert a List<Integer> to string
    private String intListToString(List<Integer> list){
        String s = "";
        for (Integer i : list) {
            s += i + ",";
        }
        return s;
    }

    //convert a string to List<Integer>
    private List<Integer> stringToIntList(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        ArrayList<Integer> result = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            result.add(Integer.parseInt(st.nextToken()));
        }
        return result;
    }

    //set selected menus and selected filters aligned
    private void updateCheckedMenus(Menu menu){
        //sort
        if (selectedSort == SORT_A_Z){
            menu.findItem(R.id.action_sort_name_asc).setChecked(true);
        } else {
            menu.findItem(R.id.action_sort_name_dsc).setChecked(true);
        }

        //filter
        int id = R.id.action_filter_none;
        switch (selectedFilter){
            case BUILDING_CATEGORY_RELIGIOUS:
                id = R.id.action_filter_religious;
                break;
            case BUILDING_CATEGORY_EMBASSIES:
                id = R.id.action_filter_embassies;
                break;
            case BUILDING_CATEGORY_GOVERNMENT:
                id = R.id.action_filter_government;
                break;
            case BUILDING_CATEGORY_FUNCTIONAL:
                id = R.id.action_filter_functional;
                break;
            case BUILDING_CATEGORY_GALLERIES:
                id = R.id.action_filter_galleries;
                break;
            case BUILDING_CATEGORY_ACADEMIC:
                id = R.id.action_filter_academic;
                break;
            case BUILDING_CATEGORY_SPORTS:
                id = R.id.action_filter_sports;
                break;
            case BUILDING_CATEGORY_COMMUNITY:
                id = R.id.action_filter_community;
                break;
            case BUILDING_CATEGORY_BUSINESS:
                id = R.id.action_filter_business;
                break;
            case BUILDING_CATEGORY_MUSEUMS:
                id = R.id.action_filter_museums;
                break;
            case BUILDING_CATEGORY_OTHER:
                id = R.id.action_filter_other;
                break;
            case BUILDING_CATEGORY_MY_BUILDINGS:
                id = R.id.action_filter_my_buildings;
                break;
            case BUILDING_CATEGORY_FAVORITES:
                id = R.id.action_filter_favorites;
            default:
                id = R.id.action_filter_none;
        }
        menu.findItem(id).setChecked(true);
    }

    public void deleteBuilding(Integer buildingId) {

        Intent intent = new Intent(this, DOOService.class);
        intent.putExtra(DOOService.REQUEST_ENDPOINT, BASE_URL + "/" + buildingId);
        intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_DELETE);
        intent.putExtra(DOOService.REQUEST_METHOD, "DELETE");
        startService(intent);

    }

    public boolean favoriteBuilding(Integer buildingId) {

        if (mFavorites.contains(buildingId)){
            mFavorites.remove(buildingId);
            return false;
        }

        mFavorites.add(buildingId);
        return true;

    }

    private void displayBuildings() {
        Log.i(TAG, "displayBuildings: display the building recyclerview");
        if (mBuildingsList != null) {
            mAdapter = new PostAdapter(this, mBuildingsList);
            mRecyclerView.setAdapter(mAdapter);

            if (selectedFilter == BUILDING_CATEGORY_FAVORITES){
                mAdapter.filterByListId(mFavorites);
            } else if (selectedFilter == BUILDING_CATEGORY_MY_BUILDINGS){
                mAdapter.filterByListId(mMyBuildings);
            }

            if (selectedSort == SORT_A_Z){
                mAdapter.sortByNameAscending();
            } else {
                mAdapter.sortByNameDescending();
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }else{
            //get the adapter ready for the recyclerview when the data isn't here yet
            BuildingPOJO[] temp = new BuildingPOJO[0];
            //this could be a different method to just add the data
            ArrayList<BuildingPOJO> tempAL = new ArrayList<>(Arrays.asList(temp));
            mAdapter = new PostAdapter(MainActivity.this, tempAL);
            mRecyclerView.setAdapter(mAdapter );
        }
    }

    private void fetchBuildings() {
        if (NetworkHelper.hasNetworkAccess(this)) {
            mSwipeRefreshLayout.setRefreshing(true);

            Intent intent = new Intent(this, DOOService.class);
            intent.putExtra(DOOService.REQUEST_METHOD, "GET");
            intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_LIST);

            //add the categoryId param if one was picked...
            if (selectedFilter > NO_SELECTED_CATEGORY_ID) {
                intent.putExtra(DOOService.REQUEST_ENDPOINT, SECURE_URL + "?categoryId=" + selectedFilter);
            } else {
                intent.putExtra(DOOService.REQUEST_ENDPOINT, SECURE_URL);
            }

            startService(intent);
        } else {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show();
            //Such sad. No Network
        }
    }
}
