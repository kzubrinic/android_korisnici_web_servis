package hr.unidu.kz.korisniciwebservis;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

// Pregled korisnika koji se dohvaćaju iz web servisa
public class DohvatPodataka  extends Activity {
    private String wsUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        wsUrl = intent.getStringExtra("url_web_servisa");
        wsUrl = wsUrl + "/" + intent.getStringExtra("godina") + "/" + intent.getStringExtra("drzava");
        // Pokreni asinhronu obradu - dohvaćanje zapisa pomoću web servisa
        new WSPregledHelper(this).execute(wsUrl);

    }

    // Privatni razred - jednostavnosti radi da bi mogao bez problema ažurirati ekranska polja aktivnosti
    // Ova obrada se odrađuje u pozadini - u drugom procesu da ne blokira korisničko sučelje.
    // Po završetku obrade izvodi se metoda onPostExecute koja ažurira korisničko sučelje
    // doInBackground prima parametar tipa polje Stringova
    // onProgressUpdate metoda se ne koristi (tip Void)
    // onPostExecute prima parametar tipa Result
    public static class WSPregledHelper extends AsyncTask<String, Void, String> {
        // How to use a static inner AsyncTask class
        // To prevent leaks, you can make the inner class static. The problem with that, though, is that
        // you no longer have access to the Activity's UI views or member variables. You can pass in a
        // reference to the Context but then you run the same risk of a memory leak. (Android can't
        // garbage collect the Activity after it closes if the AsyncTask class has a strong reference to it.)
        // The solution is to make a weak reference to the Activity (or whatever Context you need).
        // https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur#answer-46166223
        private WeakReference<DohvatPodataka> activityReference;

        WSPregledHelper(DohvatPodataka context) {
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
            Intent pov = new Intent();
            pov.putExtra("json", rez);
            DohvatPodataka activity = activityReference.get();
            activity.setResult(RESULT_OK, pov);
            activity.finish();
        }
    }
}

