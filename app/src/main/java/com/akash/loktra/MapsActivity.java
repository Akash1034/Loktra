package com.akash.loktra;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.akash.loktra.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1000 * 15 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 15 * 1; // 1 minute
    private static final float SMALLEST_DISPLACEMENT = 0.25F;
    private static final int COLOR_BLACK_ARGB = Color.BLUE;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    Button startButton;
    Button stopButton;
    TextView display;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    ArrayList<LatLng> LatLngSet = new ArrayList<>();
    Polyline polyline;
    Date date;
    String startTime;
    String endTime;
    long hours;
    long minutes;
    long seconds;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (!isGooglePlayServicesAvailable()) {
//            finish();
//        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        display = (TextView) findViewById(R.id.display);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    /* private boolean isGooglePlayServicesAvailable() {
         int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
         if (ConnectionResult.SUCCESS == status) {
             return true;
         } else {
             GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
             return false;
         }
     }
 */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        double Latitude = mCurrentLocation.getLatitude();
        double Longitude = mCurrentLocation.getLongitude();
        LatLng a = new LatLng(Latitude, Longitude);
        LatLngSet.add(a);
        Log.d(TAG, mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
    }

    private void addMarker() {
        MarkerOptions options = new MarkerOptions();

        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        Marker mapMarker = mMap.addMarker(options);
        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        mapMarker.setTitle("Ended");
        Log.d(TAG, "Marker added.............................");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                16));
        Log.d(TAG, "Zoom done.............................");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            float zoomLevel = (float) 16.0;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        }


    }


    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {
            case R.id.buttonStart:
                Toast.makeText(getApplicationContext(), "Tracking Started", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
                //    addMarker();
                startTime = getTime();
                Log.d(TAG, startTime);
                break;
            case R.id.buttonStop:
                Toast.makeText(getApplicationContext(), "Tracking Stopped", Toast.LENGTH_SHORT).show();
                stopLocationUpdates();
                addMarker();
                endTime = getTime();
                Log.d(TAG, endTime);
                drawPath();
                difference();
                displayTime();
                Log.d(TAG, LatLngSet.toString());
                break;
        }

    }

    public String getTime() {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm:ss");
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        String localTime = date.format(currentLocalTime);
        return localTime;
    }

    public void difference()
    {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            Date d1=format.parse(startTime);
            Date d2=format.parse(endTime);
            long diff=d2.getTime()-d1.getTime();
            seconds=diff/(1000)%60;
            minutes=diff/(60*1000)%60;
            hours=diff/(60*60*1000)%24;
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    void displayTime() {

        String Text="Total Shift Time:"+hours+"hrs "+minutes+"mins "+seconds+"sec";
        display.setText(Text);
        display.setVisibility(View.VISIBLE);

    }

    private void drawPath() {

        mMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().geodesic(true);
        for (int i = 0; i < LatLngSet.size(); i++) {
            LatLng point = LatLngSet.get(i);
            options.add(point);
        }
        addMarker(); //add Marker in current position
        polyline = mMap.addPolyline(options); //add Polyline
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

}
