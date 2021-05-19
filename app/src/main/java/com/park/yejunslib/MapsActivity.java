package com.park.yejunslib;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;
import com.kakao.kakaonavi.options.RpOption;
import com.kakao.kakaonavi.options.VehicleType;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.navi.NaviClient;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Intent.ACTION_VIEW;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "YejunsLib";
    private int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    TextView status;

    private GoogleMap mMap;
    public static final float DEF_ZOOM = 16f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        target_title = "";
        target_lat = 0.0;
        target_lng = 0.0;

        status = (TextView) findViewById(R.id.status);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);


        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //status.setText("위도: " + location.getLatitude() + "\n경도:"
                //        + location.getLongitude() + "\n고도:"
                //        + location.getAltitude());
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();

                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.map_myposition))
                        .title("예준isHere");

                mMap.addMarker(options);

                //mMap.addMarker(new MarkerOptions().position(pos).title("예준isHere"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEF_ZOOM));

                searchLib();
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);

        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void searchLib(){
        getApi();
    }

    private void getApi(){

        new AsyncTask<Void, Void, String>() {
            ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                /*progress = new ProgressDialog(MainActivity.this);
                progress.setTitle("다운로드");
                progress.setMessage("download");
                progress.setProgressStyle((ProgressDialog.STYLE_SPINNER));
                progress.setCancelable(false);
                progress.show();*/
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                StringBuffer sb = new  StringBuffer();
                try {
                    JSONObject json = new JSONObject(s);
                    json = json.getJSONObject("SeoulPublicLibrary");

                    JSONArray rows = json.getJSONArray("row");

                    int length = rows.length();
                    //int length = 5;
                    for(int i=0; i < length; i ++){
                        JSONObject result = (JSONObject) rows.get(i);
                        String libName = result.getString("LBRRY_NAME");

                        String lib_pos_lat = result.getString("XCNTS");
                        String lib_pos_lng = result.getString("YDNTS");

                        LatLng lib_pos = new LatLng(Double.parseDouble(lib_pos_lat), Double.parseDouble(lib_pos_lng));
                        MarkerOptions options = new MarkerOptions()
                                .position(lib_pos)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.lib_marker))
                                .title(libName + "is Here");

                        mMap.addMarker(options);

                    }

                    //status.setText(sb);

                }catch (Exception e ){
                    e.printStackTrace();
                }

                //textView.setText(sb.toString());
                //progress.dismiss();
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    //서울시 오픈 API 제공(샘플 주소 json으로 작업)
                    //result = Remote.getData("http://swopenapi.seoul.go.kr/api/subway/sample/json/realtimeStationArrival/0/5/%EC%84%9C%EC%9A%B8");
                    result = Remote.getData("http://openapi.seoul.go.kr:8088/sample/json/SeoulPublicLibrary/1/5/");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }
        }.execute();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //Toast.makeText(this, marker.getTitle() + marker.getPosition(), Toast.LENGTH_SHORT).show();
        status.setText(marker.getTitle());

        target_title = marker.getTitle();
        target_lat = marker.getPosition().latitude;
        target_lng = marker.getPosition().longitude;

        return false;
    }

    String target_title;
    Double target_lat, target_lng;

    public void navi_go(View view) {

        if(target_lat == 0.0){
            Toast.makeText(this, "도서관을 선택해주세요.", Toast.LENGTH_LONG).show();
            return ;
        }

        if (NaviClient.getInstance().isKakaoNaviInstalled(getApplicationContext())) {
            Log.i(TAG, "카카오내비 앱으로 길안내 가능");
            // 카카오내비 앱으로 길안내
            // Location.Builder를 사용하여 Location 객체를 만든다.
            com.kakao.kakaonavi.Location destination = com.kakao.kakaonavi.Location.newBuilder(target_title, target_lng, target_lat).build();
            NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setVehicleType(VehicleType.TWO_WHEEL).setRpOption(RpOption.SHORTEST).build();
            KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(destination).setNaviOptions(options);

            KakaoNaviService.getInstance().navigate(this, builder.build());
        } else {
            Log.i(TAG, "카카오내비 미설치: 웹 길안내 사용 권장");
            // 웹 브라우저에서 길안내
            /*String url = null;
            if (mKeyword != null && !mKeyword.isEmpty())
                url = String.format(HttpUrl.GONAVI_PREFIX, mKeyword, lat, lng);
            else
                url = String.format(HttpUrl.GONAVI_PREFIX, mTitleAddress, lat, lng);

            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);*/
        }

    }
}