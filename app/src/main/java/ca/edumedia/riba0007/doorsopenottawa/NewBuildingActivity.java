package ca.edumedia.riba0007.doorsopenottawa;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;
import ca.edumedia.riba0007.doorsopenottawa.services.DOOService;
import ca.edumedia.riba0007.doorsopenottawa.utils.NetworkHelper;

import static ca.edumedia.riba0007.doorsopenottawa.services.DOOService.REQUEST_SERVICE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.ADD_PATH;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BASE_URL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.CAMERA_REQUEST_CODE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.RESULT_OBJECT;

/**
 *  Activity to create a new building
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class NewBuildingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String DISCARD_CHANGES_DIALOG_TAG = "Discard Changes Dialog";

    private ImageView photoView;
    private EditText buildingName;
    private EditText buildingAddress;
    private FloatingActionButton btnImage;

    private boolean hasPicture = false;

    BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(DOOService.SERVICE_PAYLOAD)) {

                BuildingPOJO buildingPOJO = (BuildingPOJO) intent.getParcelableExtra(DOOService.SERVICE_PAYLOAD);
                finishActivity(buildingPOJO);

            } else if (intent.hasExtra(DOOService.SERVICE_EXCEPTION)) {
                String message = intent.getStringExtra(DOOService.SERVICE_EXCEPTION);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_building);

        //dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        findViewsById();

        btnImage.setOnClickListener(this);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_ADD));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveBuilding();
            return true;
        }

        if (id == R.id.action_discard) {
            DialogFragment newFragment = new ConfirmExitDialogFragment();
            newFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG_TAG);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //connect variables with views
    private void findViewsById(){
        buildingName = (EditText) findViewById(R.id.building_add_name);
        buildingAddress = (EditText) findViewById(R.id.building_add_address);
        btnImage = (FloatingActionButton) findViewById(R.id.building_add_btn_change_image);
        photoView = (ImageView) findViewById(R.id.building_add_image);
    }



    private void saveBuilding(){
        if (NetworkHelper.hasNetworkAccess(this)) {

            boolean error = false;

            if (buildingAddress.getText().toString().isEmpty()) {

                //set error message and focus
                buildingAddress.setError(getText(R.string.error_building_address));
                buildingAddress.requestFocus();
                error = true;
            }

            if (buildingName.getText().toString().isEmpty()) {

                //set error message and focus
                buildingName.setError(getText(R.string.error_building_name));
                buildingName.requestFocus();
                error = true;
            }

            if (!error) {
                //call on the Service to make the call
                Intent intent = new Intent(this, DOOService.class);
                intent.putExtra(DOOService.REQUEST_ENDPOINT, BASE_URL+ADD_PATH);
                intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_ADD);
                intent.putExtra(DOOService.REQUEST_METHOD, "POST");
                intent.putExtra(DOOService.BUILDING_NAME, buildingName.getText().toString());
                intent.putExtra(DOOService.BUILDING_ADDRESS, buildingAddress.getText().toString());

                Bitmap bitmap  = ((BitmapDrawable) photoView.getDrawable( ) ).getBitmap( );
                ByteArrayOutputStream baos = new ByteArrayOutputStream( );
                bitmap.compress( Bitmap.CompressFormat.JPEG, 0, baos);
                intent.putExtra(DOOService.IMAGE_BYTEARRAY, baos.toByteArray());


                startService(intent);
            }
        } else {
            Toast.makeText(this, R.string.error_no_network, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormattedDate(String date, String time){
        String formattedString = "";

        if (date.equals("SAT")){
            formattedString = "2017-06-03 ";
        }else {
            formattedString = "2017-06-04 ";
        }

        if (time.isEmpty()){
            return "";
        } else {
            formattedString += time;
        }

        return formattedString;
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBR);
        super.onDestroy();
    }

    private void finishActivity(BuildingPOJO buildingPOJO){
        Intent intent = new Intent();
        intent.putExtra(RESULT_OBJECT, buildingPOJO);

        setResult(RESULT_OK, intent);

        finish();
    }

    public void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent){
        Bundle extras;
        Bitmap bitmap;
        if(resultCode == RESULT_CANCELED){
            //Toast.makeText(getApplicationContext(), "No picture taken", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                extras = resultIntent.getExtras();
                bitmap = (Bitmap) extras.get("data");
                if(bitmap != null){
                    //there is a picture
                    photoView.setImageBitmap(bitmap);
                    hasPicture = true;
                }
                break;
        }
    }

    @Override
    //on click functions
    public void onClick(View view) {
        if (view == btnImage) {
            takePicture();
        }
    }
}
