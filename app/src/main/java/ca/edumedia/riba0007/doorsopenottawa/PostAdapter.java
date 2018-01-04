package ca.edumedia.riba0007.doorsopenottawa;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;

import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.ACTIVITY_EDIT_BUILDING;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PROP_BUILDING_DETAILS;

/**
 *  Adapter to control the recycler view
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {


    private Context mContext;
    //private DataItem[] mDataItems;
    private ArrayList<BuildingPOJO> mBuildingPOJOs;
    private static final String BASE_URL = "https://doors-open-ottawa.mybluemix.net/buildings/";
    private static final String IMAGE_PATH = "/image";
    public static final String TAG = "TAG";

    public PostAdapter(Context context, ArrayList<BuildingPOJO> buildings) {
        mContext = context;
        this.mBuildingPOJOs = buildings;
    }

    //NEW method to allow for overriding
    public void setData(BuildingPOJO[] buildingPOJOs) {
        mBuildingPOJOs.clear();
        mBuildingPOJOs.addAll(new ArrayList<>(Arrays.asList(buildingPOJOs)));
        notifyDataSetChanged();
    }

    public void sortByNameAscending() {
        Collections.sort( mBuildingPOJOs, new Comparator<BuildingPOJO>() {
            @Override
            public int compare( BuildingPOJO lhs, BuildingPOJO rhs ) {
                return lhs.getNameEN().compareToIgnoreCase( rhs.getNameEN() );
            }
        });

        notifyDataSetChanged();
    }

    public void sortByNameDescending() {
        Collections.sort( mBuildingPOJOs, Collections.reverseOrder(new Comparator<BuildingPOJO>() {
            @Override
            public int compare( BuildingPOJO lhs, BuildingPOJO rhs ) {
                return lhs.getNameEN().compareToIgnoreCase( rhs.getNameEN() );
            }
        }));

        notifyDataSetChanged();
    }

    //not in use
    //filter by favorites or mybuildings
    public void filterByListId(List<Integer> listIds){
        ArrayList<BuildingPOJO> newBuildingPOJOs = new ArrayList<BuildingPOJO>();

        for (BuildingPOJO buildingPOJO : mBuildingPOJOs){
           for (Integer buildingId : listIds){
               if (buildingId.equals(buildingPOJO.getBuildingId())){
                   newBuildingPOJOs.add(buildingPOJO);
                   break;
               }
           }
        }

        mBuildingPOJOs = newBuildingPOJOs;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View postView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false);

        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final BuildingPOJO buildingPOJO = mBuildingPOJOs.get(position);

        String url = BASE_URL + buildingPOJO.getBuildingId() + IMAGE_PATH; //+ "?"+String.valueOf(System.currentTimeMillis());
        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.noimage)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.noimage)
                .into(holder.imageView);

        //set the text in the various views
        holder.title.setText( buildingPOJO.getNameEN() );
        holder.body.setText( "( "+ buildingPOJO.getBuildingId() + " ) " + buildingPOJO.getCategoryEN() );

        //check favorite
        if (((MainActivity) mContext).mFavorites.contains(buildingPOJO.getBuildingId())){
            holder.btnFav.setBackground(mContext.getDrawable(R.drawable.ic_favorite_filled));
        } else {
            holder.btnFav.setBackground(mContext.getDrawable(R.drawable.ic_favorite_empty));
        }

        //check my buildings
        if (!((MainActivity) mContext).mMyBuildings.contains(buildingPOJO.getBuildingId())){
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDel.setVisibility(View.GONE);
        }


        Log.i(TAG, "onBindViewHolder: " + buildingPOJO.getNameEN() );

        //add click listeners
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext.getApplicationContext(), DetailsActivity.class);
                intent.putExtra(PROP_BUILDING_DETAILS, buildingPOJO);
                mContext.startActivity(intent);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {

                if (!((MainActivity) mContext).mMyBuildings.contains(buildingPOJO.getBuildingId())){
                    return false;
                }

                Intent intent = new Intent(mContext.getApplicationContext(), EditBuildingActivity.class);
                intent.putExtra(PROP_BUILDING_DETAILS, buildingPOJO);
                ((MainActivity) mContext).startActivityForResult(intent,ACTIVITY_EDIT_BUILDING);
                return false;
            }
        });

        holder.btnEdit.setOnClickListener( new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), EditBuildingActivity.class);
                intent.putExtra(PROP_BUILDING_DETAILS, buildingPOJO);
                ((MainActivity) mContext).startActivityForResult(intent,ACTIVITY_EDIT_BUILDING);
            }
        });

        holder.btnDel.setOnClickListener( new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment deleteDialogFragment = new DeleteDialogFragment(new DeleteDialogInterface() {
                    @Override
                    public void confirmDeleteAction() {
                        ((MainActivity) mContext).deleteBuilding(buildingPOJO.getBuildingId());
                    }
                });
                deleteDialogFragment.show(((MainActivity) mContext).getFragmentManager(), "DELETE_BUILDING");
            }
        });

        holder.btnFav.setOnClickListener( new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( ((MainActivity) mContext).favoriteBuilding(buildingPOJO.getBuildingId()) ){
                    ((ImageButton) v).setBackground(mContext.getDrawable(R.drawable.ic_favorite_filled));
                } else {
                    ((ImageButton) v).setBackground(mContext.getDrawable(R.drawable.ic_favorite_empty));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mBuildingPOJOs == null){
            return 0;
        }
        return mBuildingPOJOs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        //declare a member variable for each view
        public View mView;
        public TextView title;
        public TextView body;
        public ImageView imageView;
        public ImageButton btnEdit;
        public ImageButton btnDel;
        public ImageButton btnFav;

        public ViewHolder(View itemView) {
            super(itemView);
            //set the value for each member variable
            mView = itemView;
            title = (TextView) mView.findViewById(R.id.building_title);
            body = (TextView) mView.findViewById(R.id.building_category);
            imageView = (ImageView) mView.findViewById(R.id.building_image);
            btnEdit = (ImageButton) mView.findViewById(R.id.btn_edit);
            btnDel = (ImageButton) mView.findViewById(R.id.btn_delete);
            btnFav = (ImageButton) mView.findViewById(R.id.btn_favorite);
        }
    }
}