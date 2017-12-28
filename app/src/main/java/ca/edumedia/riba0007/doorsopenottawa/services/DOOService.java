package ca.edumedia.riba0007.doorsopenottawa.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import ca.edumedia.riba0007.doorsopenottawa.models.BuildingPOJO;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *  Make server requests and manage responses
 *  @author Priscila Ribas da Costa (riba0007)
 */
public class DOOService extends IntentService {

    public static final String TAG = "DOOService";
    public static final String SERVICE_PAYLOAD = "DOOService_Payload";
    public static final String SERVICE_MESSAGE = "DOOService_Message";
    public static final String SERVICE_EXCEPTION = "DOOService_Exception";
    public static final String REQUEST_PACKAGE = "DOOService_RequestPackage";

    public static final String REQUEST_SERVICE = "action";
    public static final String SERVICE_LIST = "buildings_list";
    public static final String SERVICE_ADD = "buildings_add";
    public static final String SERVICE_EDIT = "building_edit";
    public static final String SERVICE_DETAILS = "building_details";
    public static final String SERVICE_DELETE = "building_delete";

    public static final String REQUEST_ENDPOINT = "endPoint";
    public static final String REQUEST_METHOD = "method";
    public static final String BUILDING_NAME = "bName";
    public static final String BUILDING_ADDRESS = "bAddress";
    public static final String BUILDING_DESCRIPTION = "bDescription";
    public static final String SATURDAY_START = "saturdayStart";
    public static final String SATURDAY_CLOSE = "saturdayClose";
    public static final String SUNDAY_START = "sundayStart";
    public static final String SUNDAY_CLOSE = "sundayClose";
    public static final String IS_NEW = "isNewBuilding";
    public static final String IMAGE_BYTEARRAY = "imageByteArray";

    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final static OkHttpClient client = new OkHttpClient();

    public DOOService ( ){
        //hard code the name of my class because the superclass requires something
        super("GetBuildingList");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //create a local broadcast manager
        if (intent.hasExtra(REQUEST_SERVICE)) {

            String endpoint = intent.getStringExtra(REQUEST_ENDPOINT);
            //method could be used to determine which function to call
            String method = intent.getStringExtra(REQUEST_METHOD);
            String service = intent.getStringExtra(REQUEST_SERVICE);
            String credentials = Credentials.basic("riba0007", "password");
            RequestBody requestBody = null;
            Request request = null;

            //request body
            switch (service) {

                case SERVICE_DELETE:
                case SERVICE_LIST:
                    break;

                case SERVICE_ADD:
                    requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("nameEN", intent.getStringExtra(BUILDING_NAME))
                        .addFormDataPart("addressEN", intent.getStringExtra(BUILDING_ADDRESS))
                        .addFormDataPart("image", "riba0007building.jpg",
                                RequestBody.create(MEDIA_TYPE_JPEG, intent.getByteArrayExtra(IMAGE_BYTEARRAY) ) )
                        .build();
                    break;

                case SERVICE_EDIT:

                    String json = "{" +
                            "\"addressEN\":\"" + intent.getStringExtra(BUILDING_ADDRESS) + "\"," +
                            "\"descriptionEN\":\"" + intent.getStringExtra(BUILDING_DESCRIPTION) + "\"," +
                            "\"saturdayStart\":\"" + intent.getStringExtra(SATURDAY_START) + "\"," +
                            "\"saturdayClose\":\"" + intent.getStringExtra(SATURDAY_CLOSE) + "\"," +
                            "\"sundayStart\":\"" + intent.getStringExtra(SUNDAY_START) + "\"," +
                            "\"sundayClose\":\"" + intent.getStringExtra(SUNDAY_CLOSE) + "\"," +
                            "\"isNewBuilding\":" + intent.getBooleanExtra(IS_NEW, false) +
                            "}";
                    requestBody = RequestBody.create(MEDIA_TYPE_JSON, json);

                    /*requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("addressEN", intent.getStringExtra(BUILDING_ADDRESS))
                        .addFormDataPart("descriptionEN", intent.getStringExtra(BUILDING_DESCRIPTION))
                        .addFormDataPart("saturdayStart", intent.getStringExtra(SATURDAY_START))
                        .addFormDataPart("saturdayClose", intent.getStringExtra(SATURDAY_CLOSE))
                        .addFormDataPart("sundayStart", intent.getStringExtra(SUNDAY_START))
                        .addFormDataPart("sundayClose", intent.getStringExtra(SUNDAY_CLOSE))
                        //.addFormDataPart("isNewBuilding", String.valueOf(intent.getBooleanExtra(IS_NEW, true)))
                        //.addFormDataPart("image", "building.jpg",
                        //        RequestBody.create(MEDIA_TYPE_JPEG, intent.getByteArrayExtra(IMAGE_BYTEARRAY) ) )
                        .build();*/
                    break;
            }

            //request
            switch (method){
                case "POST":
                    request = new Request.Builder()
                            .header("Authorization", credentials)
                            .addHeader("Accept", "application/json; q=0.5")
                            .url(endpoint)
                            .post(requestBody)
                            .build();
                    break;
                case "GET":
                    request = new Request.Builder()
                            .header("Authorization", credentials)
                            .addHeader("Accept", "application/json; q=0.5")
                            .url(endpoint)
                            .get()
                            .build();
                    break;
                case "PUT":
                    request = new Request.Builder()
                            .header("Authorization", credentials)
                            .addHeader("Accept", "application/json; q=0.5")
                            .url(endpoint)
                            .put(requestBody)
                            .build();
                    break;
                case "DELETE":
                    request = new Request.Builder()
                            .header("Authorization", credentials)
                            .addHeader("Accept", "application/json; q=0.5")
                            .url(endpoint)
                            .delete()
                            .build();
            }

            //execute request
            try {
                Response response = client.newCall(request).execute(); //synchronous request

                //Do something with the response...
                if (response.isSuccessful()) {
                    String output = response.body().string();
                    Log.i(TAG, output);

                    //response
                    switch (service){
                        case SERVICE_LIST:
                            returnBuildingArray(output, service);
                            break;
                        case SERVICE_DELETE:
                        case SERVICE_EDIT:
                        case SERVICE_ADD:
                            returnBuildingObject(output, service);
                    }

                } else {
                    Intent messageIntent = new Intent(SERVICE_MESSAGE);
                    messageIntent.putExtra(SERVICE_EXCEPTION, response.message());
                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                    manager.sendBroadcast(messageIntent);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Intent messageIntent = new Intent(SERVICE_MESSAGE);
                messageIntent.putExtra(SERVICE_EXCEPTION, e.getMessage());
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                manager.sendBroadcast(messageIntent);
                return;
            }

        } else {
            Intent messageIntent = new Intent(SERVICE_LIST);
            messageIntent.putExtra(SERVICE_EXCEPTION, "No action selected");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.sendBroadcast(messageIntent);
            return;
        }
    }

    private void returnBuildingArray(String response, String service){
        Gson gson = new Gson();
        BuildingPOJO[] dataItems = gson.fromJson(response, BuildingPOJO[].class);

        Intent messageIntent = new Intent(service);
        messageIntent.putExtra(SERVICE_PAYLOAD, dataItems);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }

    private void returnBuildingObject(String response, String service){
        Gson gson = new Gson();
        BuildingPOJO dataItem = gson.fromJson(response, BuildingPOJO.class);

        Intent messageIntent = new Intent(service);
        messageIntent.putExtra(SERVICE_PAYLOAD, dataItem);
        messageIntent.putExtra(SERVICE_MESSAGE, service);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "GetBuildingList onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "GetBuildingList onDestroy");
    }

}
