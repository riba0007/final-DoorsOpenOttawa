package ca.edumedia.riba0007.doorsopenottawa;

import android.app.DialogFragment;
import android.app.TimePickerDialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;
import ca.edumedia.riba0007.doorsopenottawa.services.DOOService;
import ca.edumedia.riba0007.doorsopenottawa.utils.NetworkHelper;

import static ca.edumedia.riba0007.doorsopenottawa.services.DOOService.REQUEST_SERVICE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.BASE_URL;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.CAMERA_REQUEST_CODE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.IMAGE_PATH;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.PROP_BUILDING_DETAILS;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.RESULT_MESSAGE;
import static ca.edumedia.riba0007.doorsopenottawa.utils.Values.RESULT_OBJECT;

/**
 *  Edit a building
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class EditBuildingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String DISCARD_CHANGES_DIALOG_TAG = "Discard Changes Dialog";
    private static final String TAG = "Edit Building";

    private ImageView photoView;
    private EditText satOpen;
    private EditText satClose;
    private EditText sunOpen;
    private EditText sunClose;
    private TextView buildingName;
    private EditText buildingAddress;
    private EditText buildingDescription;
    private FloatingActionButton btnImage;
    private CheckBox isNew;

    private boolean hasPicture = false;
    private SimpleDateFormat dateFormatter;
    private Calendar newCalendar;

    private TimePickerDialog satOpenTimePickerDialog;
    private TimePickerDialog satCloseTimePickerDialog;
    private TimePickerDialog sunOpenTimePickerDialog;
    private TimePickerDialog sunCloseTimePickerDialog;

    private BuildingPOJO buildingPOJO;

    BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(DOOService.SERVICE_PAYLOAD)) {
                String message = intent.hasExtra(DOOService.SERVICE_MESSAGE) ? intent.getStringExtra(DOOService.SERVICE_MESSAGE) : "";
                BuildingPOJO buildingPOJO = (BuildingPOJO) intent.getParcelableExtra(DOOService.SERVICE_PAYLOAD);

                if (hasPicture){
                    uploadImage();
                } else {
                    finishActivity(buildingPOJO, message);
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
        setContentView(R.layout.activity_edit_building);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        newCalendar = Calendar.getInstance();

        findViewsById();

        Intent intent = getIntent();
        if (intent.hasExtra(PROP_BUILDING_DETAILS)) {
            buildingPOJO = (BuildingPOJO) intent.getParcelableExtra(PROP_BUILDING_DETAILS);

            updateFields();
            setDateFields();

        } else {
            buildingPOJO = new BuildingPOJO();
            buildingName.setText(getResources().getString(R.string.error_no_data));
            buildingName.setEnabled(false);
            buildingAddress.setEnabled(false);
            buildingDescription.setEnabled(false);
            btnImage.setEnabled(false);
            satOpen.setEnabled(false);
            satClose.setEnabled(false);
            sunOpen.setEnabled(false);
            sunClose.setEnabled(false);
            isNew.setEnabled(false);
        }


        btnImage.setOnClickListener(this);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_EDIT));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_DELETE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBR, new IntentFilter(DOOService.SERVICE_IMAGE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_save:
                saveBuilding();
                break;
            case R.id.action_discard:
                DialogFragment confirmExitDialogFragment = new ConfirmExitDialogFragment();
                confirmExitDialogFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG_TAG);
                break;
            case R.id.action_delete:
                DialogFragment deleteDialogFragment = new DeleteDialogFragment(new DeleteDialogInterface() {
                    @Override
                    public void confirmDeleteAction() {
                        deleteBuilding();
                    }
                });
                deleteDialogFragment.show(getFragmentManager(), "DELETE_BUILDING");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //connect variables with views
    private void findViewsById(){
        satOpen = (EditText) findViewById(R.id.building_edit_saturday_open);
        satClose = (EditText) findViewById(R.id.building_edit_saturday_close);
        sunOpen = (EditText) findViewById(R.id.building_edit_sunday_open);
        sunClose = (EditText) findViewById(R.id.building_edit_sunday_close);
        buildingName = (TextView) findViewById(R.id.building_edit_name);
        buildingAddress = (EditText) findViewById(R.id.building_edit_address);
        buildingDescription = (EditText) findViewById(R.id.building_edit_description);
        btnImage = (FloatingActionButton) findViewById(R.id.building_edit_btn_change_image);
        photoView = (ImageView) findViewById(R.id.building_edit_image);
        isNew = (CheckBox) findViewById(R.id.building_edit_is_new);
    }

    //fill views with buildingPOJO data
    private void updateFields(){

        buildingName.setText(buildingPOJO.getNameEN());
        buildingAddress.setText(buildingPOJO.getAddressEN());
        buildingDescription.setText(buildingPOJO.getDescriptionEN());
        satOpen.setText(getFormattedDate(buildingPOJO.getSaturdayStart()));
        satClose.setText(getFormattedDate(buildingPOJO.getSaturdayClose()));
        sunOpen.setText(getFormattedDate(buildingPOJO.getSundayStart()));
        sunClose.setText(getFormattedDate(buildingPOJO.getSundayClose()));
        isNew.setChecked(buildingPOJO.getIsNewBuilding());

        String url = BASE_URL + "/" + buildingPOJO.getBuildingId() + IMAGE_PATH;
        Picasso.with(this)
                .load(url)
                .placeholder(R.drawable.noimage)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.noimage)
                .into(photoView);
    }

    //update Date fields
    private void setDateFields() {
        satOpen.setOnClickListener(this);
        satClose.setOnClickListener(this);
        sunOpen.setOnClickListener(this);
        sunClose.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        //saturday Open
        satOpenTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                satOpen.setText(hourOfDay + ":" + minute);
            }
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        //saturday Close
        satCloseTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                satClose.setText(hourOfDay + ":" + minute);
            }
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        //sunday Open
        sunOpenTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                sunOpen.setText(hourOfDay + ":" + minute);
            }
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        //sunday Close
        sunCloseTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                sunClose.setText(hourOfDay + ":" + minute);
            }
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);
    }

    public void deleteBuilding(){
        Intent intent = new Intent(this, DOOService.class);
        intent.putExtra(DOOService.REQUEST_ENDPOINT, BASE_URL + "/" + buildingPOJO.getBuildingId());
        intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_DELETE);
        intent.putExtra(DOOService.REQUEST_METHOD, "DELETE");
        startService(intent);
    }

    private void uploadImage(){
        hasPicture = false;

        Intent intent = new Intent(this, DOOService.class);
        intent.putExtra(DOOService.REQUEST_ENDPOINT, BASE_URL + "/" + buildingPOJO.getBuildingId() + IMAGE_PATH);
        intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_IMAGE);
        intent.putExtra(DOOService.REQUEST_METHOD, "POST");

        Bitmap bitmap  = ((BitmapDrawable) photoView.getDrawable( ) ).getBitmap( );
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 0, baos);
        intent.putExtra(DOOService.IMAGE_BYTEARRAY, baos.toByteArray());

        startService(intent);
    }

    private void saveBuilding(){

        if (buildingPOJO.getBuildingId() == null || buildingPOJO.getBuildingId() <= 0){
            Toast.makeText(this, getResources().getString(R.string.error_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

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
                intent.putExtra(DOOService.REQUEST_ENDPOINT, BASE_URL + "/" + buildingPOJO.getBuildingId());
                intent.putExtra(REQUEST_SERVICE,DOOService.SERVICE_EDIT);
                intent.putExtra(DOOService.REQUEST_METHOD, "PUT");
                intent.putExtra(DOOService.BUILDING_NAME, buildingName.getText().toString());
                intent.putExtra(DOOService.BUILDING_ADDRESS, buildingAddress.getText().toString());
                intent.putExtra(DOOService.BUILDING_DESCRIPTION, buildingDescription.getText().toString());
                intent.putExtra(DOOService.SATURDAY_START, getFormattedDate("SAT", satOpen.getText().toString()));
                intent.putExtra(DOOService.SATURDAY_CLOSE, getFormattedDate("SAT", satClose.getText().toString()));
                intent.putExtra(DOOService.SUNDAY_START, getFormattedDate("SUN", sunOpen.getText().toString()));
                intent.putExtra(DOOService.SUNDAY_CLOSE, getFormattedDate("SUN", sunClose.getText().toString()));
                intent.putExtra(DOOService.IS_NEW, isNew.isChecked());

                /*Bitmap bitmap  = ((BitmapDrawable) photoView.getDrawable( ) ).getBitmap( );
                ByteArrayOutputStream baos = new ByteArrayOutputStream( );
                bitmap.compress( Bitmap.CompressFormat.JPEG, 0, baos);
                intent.putExtra(DOOService.IMAGE_BYTEARRAY, baos.toByteArray());
                */

                startService(intent);
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormattedDate(String date){

        String formattedDate = "";

        if (date != null && !date.isEmpty()){

            formattedDate = date.substring(date.indexOf(" ")+1);
            formattedDate += date.substring(date.indexOf(":")+1).length() == 1 ? 0 : "";

            /*try {
                newCalendar.setTime(dateFormatter.parse(date));
                formattedDate = newCalendar.get(Calendar.HOUR_OF_DAY) + ":" + newCalendar.get(Calendar.MINUTE);
            }catch (ParseException e){
                Log.e(TAG, e.getMessage());
            }*/
        }

        return formattedDate;
    }

    private String getFormattedDate(String date, String time){
        String formattedDate = "";

        //DOO is open SAT and SUN only. Not allowed to choose date
        if (date.equals("SAT")){
            formattedDate = "2017-06-03 ";
        }else {
            formattedDate = "2017-06-04 ";
        }

        if (time.isEmpty()){
            return "";
        } else {
            formattedDate += time.substring(time.indexOf(":")+1).length()==1 ? time + "0" : time;
        }

        return formattedDate;
    }

    private void finishActivity(BuildingPOJO buildingPOJO, String message){
        Intent intent = new Intent();
        intent.putExtra(RESULT_OBJECT, buildingPOJO);
        intent.putExtra(RESULT_MESSAGE, message);

        setResult(RESULT_OK, intent);

        finish();
    }

    public void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBR);
        super.onDestroy();
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
        if(view == satOpen) {
            satOpenTimePickerDialog.show();
        } else if(view == satClose) {
            satCloseTimePickerDialog.show();
        } else if(view == sunOpen) {
            sunOpenTimePickerDialog.show();
        } else if(view == sunClose) {
            sunCloseTimePickerDialog.show();
        } else if (view == btnImage) {
            takePicture();
        }
    }
}
