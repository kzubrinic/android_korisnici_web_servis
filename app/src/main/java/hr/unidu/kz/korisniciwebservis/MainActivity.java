package hr.unidu.kz.korisniciwebservis;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import hr.unidu.kz.korisniciwebservis.pojo.Praznik;

public class MainActivity extends AppCompatActivity {
    private static final String url = "https://date.nager.at/api/v3/PublicHolidays";
    private RecyclerView recyclerView;
    private EditText godina, drzava;
    private ProgressBar pgsBar;
    private ArrayList<Praznik> praznici;
    private PrazniciAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        godina = findViewById(R.id.godina);
        drzava = findViewById(R.id.drzava);
        pgsBar = findViewById(R.id.progressBar);
        praznici = new ArrayList<>();

        recyclerView = findViewById(R.id.pregled_praznika);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new PrazniciAdapter(praznici, this);
        recyclerView.setAdapter(mAdapter);
    }

    // Poziva se aktivnost dohvata s weba i pregleda dohvaćenih podataka
    public void praznici(View v) {
        if(godina.getText().toString().isEmpty() || drzava.getText().toString().isEmpty()){
            Toast.makeText(this, "Godina i oznaka države moja se popuniti!", Toast.LENGTH_LONG).show();
            return;
        }
        String wsUrl = url + "/" + godina.getText().toString() + "/" + drzava.getText().toString();
        // Pokreni asinhronu obradu - dohvaćanje zapisa pomoću web servisa
        pgsBar.setIndeterminate(true);
        new WSPregledHelper(this).execute(wsUrl);
    }

    // Privatni razred - jednostavnosti radi da bi mogao bez problema ažurirati ekranska polja aktivnosti
    // Ova obrada se odrađuje u pozadini - u drugom procesu da ne blokira korisničko sučelje.
    // Po završetku obrade izvodi se metoda onPostExecute koja ažurira korisničko sučelje
    // doInBackground prima parametar tipa polje Stringova
    // onProgressUpdate metoda se ne koristi (tip Void)
    // onPostExecute prima parametar tipa Result
    private static class WSPregledHelper extends AsyncTask<String, Void, String> {
        // How to use a static inner AsyncTask class
        // To prevent leaks, you can make the inner class static. The problem with that, though, is that
        // you no longer have access to the Activity's UI views or member variables. You can pass in a
        // reference to the Context but then you run the same risk of a memory leak. (Android can't
        // garbage collect the Activity after it closes if the AsyncTask class has a strong reference to it.)
        // The solution is to make a weak reference to the Activity (or whatever Context you need).
        // https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur#answer-46166223
        private WeakReference<MainActivity> activityReference;

        WSPregledHelper(MainActivity context) {
            // Weak reference na aktivnost - efikasnije oslobađanje memorije
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... urls) {
            int br = urls.length;
            InputStream es;
            String res = "";
            // šaljemo samo 1 parametar (URL web servisa), iako metoda može primiti polje parametara
            HttpURLConnection conn = null;

            try {
                // povezujemo se sa zadanim URL-om pomoću GET metode
                conn = (HttpURLConnection) new URL(urls[0]).openConnection();
                // postavljamo kodnu stranicu da bi se znakovi prikazali ispravno
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                // koristimo HTTP GET metodu za dohvat
                conn.setRequestMethod("GET");
                // Ako je obrada prošla u redu - dohvati povratnu poruku (InputStream) i pretvori ju u String
                //  inače se dohvaćaju podaci greške iz error streama
                if ((es = conn.getErrorStream()) == null) {
                    // pretvaramo ulazni InputStream u String
                    res = inputStreamToString(conn.getInputStream());

                }
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return res;
        }

        // Pomoćna metoda koja vraća String iz primljenog input ili error streama
        private String inputStreamToString(InputStream is) {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";
            s.close();
            return res;
        }

        @Override
        // metoda prima polje objekata tipa User
        protected void onPostExecute(String rez) {
            MainActivity activity = activityReference.get();
            Gson gson = new Gson();
            Type tipListe = new TypeToken<ArrayList<Praznik>>(){}.getType();
            ArrayList<Praznik> pom = gson.fromJson(rez, tipListe);
            if(pom==null || pom.size()==0){
                activity.pgsBar.setIndeterminate(false);
                Toast.makeText(activity, String.format("Za zadane uvjete (Godina: %s, Država: %s) nema podataka!", activity.godina.getText().toString(), activity.drzava.getText().toString()), Toast.LENGTH_LONG).show();
                return;
            }
            // Adpater čuva referencu na listu pa se ne smije poslati novi objekt već se ta
            //  ista lista mora ažurirati (očistiti pa napuniti).
            activity.praznici.clear();
            for(Praznik p: pom)
                activity.praznici.add(p);
            // Metoda onPostExecute se izvodi u grafičkoj dretvi pa je u njoj sigurno
            //   ažurirati grafičke komponente aktivnosti
            activity.mAdapter.notifyDataSetChanged();
            activity.pgsBar.setIndeterminate(false);
        }
    }

}
