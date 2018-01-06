package ca.edumedia.riba0007.doorsopenottawa;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;

import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BASE_URL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.IMAGE_PATH;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PROP_BUILDING_DETAILS;

/**
 *  Building details
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView mName;
    private TextView mDescription;
    private ImageView mImage;
    private TextView mOpenHours;
    private Geocoder mGeocoder;

    private GoogleMap mMap;

    private BuildingPOJO buildingPOJO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mName = (TextView) findViewById(R.id.building_details_title);
        mImage = (ImageView) findViewById(R.id.building_details_image);
        mDescription = (TextView) findViewById(R.id.building_details_description);
        mOpenHours = (TextView) findViewById(R.id.building_details_open);

        mGeocoder = new Geocoder( this, Locale.CANADA );

        Intent intent = getIntent();
        if (intent.hasExtra(PROP_BUILDING_DETAILS)) {
            buildingPOJO = (BuildingPOJO) intent.getParcelableExtra(PROP_BUILDING_DETAILS);

            String url = BASE_URL + "/" + buildingPOJO.getBuildingId() + IMAGE_PATH;
            Picasso.with(this)
                    .load(url)
                    .placeholder(R.drawable.noimage)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .error(R.drawable.noimage)
                    .into(mImage);

            mName.setText(buildingPOJO.getNameEN());
            mDescription.setText(buildingPOJO.getDescriptionEN());

            String sOpenHours = getResources().getString(R.string.text_open_hours,
                    formatOpenHours(buildingPOJO.getSaturdayStart(),buildingPOJO.getSaturdayClose()),
                    formatOpenHours(buildingPOJO.getSundayStart(),buildingPOJO.getSundayClose()));

            mOpenHours.setText(sOpenHours);

        } else {
            mName.setText(getResources().getString(R.string.error_no_data));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        pin();
    }

    /** Locate and pin locationName to the map. */
    private void pin( ) {

        //try by address
        try {
            Address address = mGeocoder.getFromLocationName(buildingPOJO.getAddressEN(), 1).get(0);
            LatLng ll = new LatLng( address.getLatitude(), address.getLongitude() );
            mMap.addMarker( new MarkerOptions().position(ll).title(buildingPOJO.getAddressEN()) );
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(ll, 13.F) );
            //Toast.makeText(this, "Pinned: " + buildingPOJO.getAddressEN(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

            //try by lat / long
            try {
                LatLng ll = new LatLng( buildingPOJO.getLatitude(), buildingPOJO.getLongitude() );
                mMap.addMarker( new MarkerOptions().position(ll).title(buildingPOJO.getAddressEN()) );
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(ll, 13.F) );
                //Toast.makeText(this, "Pinned: " + buildingPOJO.getAddressEN(), Toast.LENGTH_SHORT).show();
            } catch (Exception er){
                //Toast.makeText(this, "Not found: " + buildingPOJO.getAddressEN(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatOpenHours(String sOpen, String sClose){

        String formattedString = getResources().getString(R.string.text_closed);

        if (sOpen != null && sClose != null){

            formattedString = getResources().getString(R.string.text_open_hour_time,
                    sOpen.substring(8,10) , sOpen.substring(11, 16) , sClose.substring(11, 16));
        }

        return formattedString;
    }

}
