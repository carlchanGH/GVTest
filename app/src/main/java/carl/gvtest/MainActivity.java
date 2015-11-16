package carl.gvtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Context context = this;
    private EditText editText;
    private GoogleApiClient googleApiClient = null;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Initialization
        editText = (EditText) findViewById(R.id.direction_input);

        // Build Google API client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Checks if the input destination is valid
    // If so, starts Directions activity
    // If not, request the user to try again
    public void checkDestination(View view) {
        final Location location = connected ?
                LocationServices.FusedLocationApi.getLastLocation(googleApiClient): null;
        if (location == null) {
            Toast toast = Toast.makeText(context, Values.LOCATION_ERROR, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        final String destination = editText.getText().toString();
        // Make API request
        try {
            GoogleApi.getInstance(this).makeDirectionsHttpRequest(location, destination, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String status = "";
                    try {
                        status = response.getString("status");
                    } catch(JSONException e) {
                        Log.e("GoodVibes", "JSON exception", e);
                        Toast toast = Toast.makeText(context, Values.UNKNOWN_ERROR, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    switch (status) {
                        case "OK":
                            Intent intent = new Intent(context, Directions.class);
                            intent.putExtra("location", location);
                            intent.putExtra("destination", destination);
                            intent.putExtra("response", response.toString());
                            startActivity(intent);
                            break;
                        case "NOT_FOUND":
                            Toast.makeText(context, Values.DESTINATION_ERROR, Toast.LENGTH_SHORT).show();
                            break;
                        case "ZERO_RESULTS":
                            Toast.makeText(context, Values.PATH_ERROR, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, Values.UNKNOWN_ERROR, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast toast = Toast.makeText(context, Values.UNKNOWN_ERROR, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e("GoodVibes", "Unsupported Encoding exception", e);
            Toast toast = Toast.makeText(context, Values.UNKNOWN_ERROR, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connected = false;
        Toast toast = Toast.makeText(context, Values.UNKNOWN_ERROR, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }
}
