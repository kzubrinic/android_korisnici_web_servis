package hr.unidu.kz.korisniciwebservis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import hr.unidu.kz.korisniciwebservis.pojo.Greska;
import hr.unidu.kz.korisniciwebservis.pojo.User;
import hr.unidu.kz.korisniciwebservis.pojo.Result;

// Ažuriranje korisnika pomoću web servisa
public class AzuriranjeActivity extends AppCompatActivity {
    private TextView id, korisnik, ime;
    private Context con;
    private String wsUrl;
    private Greska err = new Greska();
    private User kor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_azuriranje);
        id = findViewById(R.id.id);
        korisnik = findViewById(R.id.korisnik);
        ime = findViewById(R.id.naziv);
        con = this;
        Intent intent = getIntent();
        if (intent.hasExtra("korisnik")) {
            id.setText(Integer.toString(intent.getIntExtra("id", 0)));
            korisnik.setText(intent.getStringExtra("korisnik"));
            ime.setText(intent.getStringExtra("ime"));
        }
        wsUrl = intent.getStringExtra("url_web_servisa");
    }

    // Unos novog zapisa pomoću web servisa
    public void unos(View v) {
        // provjera unesenih podataka
        if (korisnik.getText().toString().length() < 1 ||
                ime.getText().toString().length() < 1) {
            Toast.makeText(con, "Morate unijeti korisničko ime i ime", Toast.LENGTH_LONG).show();
            return;
        }
        Gson gson = new Gson();
        User k = new User(0, korisnik.getText().toString(), ime.getText().toString());
        String kj = gson.toJson(k, User.class);
        // stvaranje novog korisnika - metoda POST s praznim id-jem (vrijednost id-ja je 0)
        new WSHelper(this).execute(wsUrl, "POST", kj);
    }

    // Izmjena zapisa pomoću web servisa
    public void izmjena(View v) {
        // provjera unesenih podataka
        if (id.getText().toString().length() < 1 ||
                korisnik.getText().toString().length() < 1 ||
                ime.getText().toString().length() < 1) {
            Toast.makeText(con, "Morate unijeti id, korisničko ime i ime", Toast.LENGTH_LONG).show();
            return;
        }
        Gson gson = new Gson();
        User k = new User(Integer.parseInt(id.getText().toString()), korisnik.getText().toString(), ime.getText().toString());
        String kj = gson.toJson(k, User.class);
        Log.i("PUT", kj);
        // izmjena korisnika - metoda PUT - na url se dodaje id korisnika koji se mijenja
        new WSHelper(this).execute(wsUrl + "/" + id.getText().toString(), "PUT", kj);
    }

    // Brisanje zapisa pomoću web servisa
    public void brisanje(View v) {
        // provjera unesenih podataka
        if (id.getText().toString().length() < 1) {
            Toast.makeText(con, "Morate unijeti id", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog brisanjeBox = izvrsiBrisanje();
        brisanjeBox.show();
    }

    // Čitanje zapisa za zadani id - pomoću web servisa
    public void citanje(View v) {
        // provjera unesenih podataka
        if (id.getText().toString().length() < 1) {
            Toast.makeText(con, "Morate unijeti id", Toast.LENGTH_LONG).show();
            return;
        }
        Gson gson = new Gson();
        User k = new User(Integer.parseInt(id.getText().toString()), korisnik.getText().toString(), ime.getText().toString());
        String kj = gson.toJson(k, User.class);
        // čitanje korisnika - metoda GET - na url se dodaje id korisnika koji se čita
        new WSHelper(this).execute(wsUrl + "/" + id.getText().toString(), "GET", kj);
    }

    /*
        Otvara prozorčić potvrde brisanje
     */
    private AlertDialog izvrsiBrisanje() {
        return new AlertDialog.Builder(this)
                .setTitle("Brisanje")
                .setMessage("Želite li obrisati zapis?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        obrisi(); // programski kod brisanja
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void obrisi() {
        Gson gson = new Gson();
        User k = new User(Integer.parseInt(id.getText().toString()), korisnik.getText().toString(), ime.getText().toString());
        String kj = gson.toJson(k, User.class);
        // brisanje korisnika - metoda DELETE - na url se dodaje id korisnika koji se mijenja
        new WSHelper(this).execute(wsUrl + "/" + id.getText().toString(), "DELETE", kj);
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        finish();
    }

    private static class WSHelper extends AsyncTask<String, Void, Result> {

        private WeakReference<AzuriranjeActivity> activityReference;

        // only retain a weak reference to the activity
        public WSHelper(AzuriranjeActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Result doInBackground(String[] parms) {
            int br = parms.length;
            InputStream es;

            HttpURLConnection conn = null;
            try {
                Result korisnici = new Result();
                // metoda prima više parametara:
                // 1. parametar - URL web servisa
                // 2. parametar - metoda (POST, PUT, DELETE)
                // 3. parametar - string s JSON porukom koja se šalje
                conn = (HttpURLConnection) new URL(parms[0]).openConnection();
                // postavljamo kodnu stranicu da bi se znakovi prikazali ispravno
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                // koristi se zadane metoda (GET, POST, PUT ili DELETE)
                conn.setRequestMethod(parms[1]);
                // POST i PUT primaju parametre u tijelu upita (OutputStream)
                if (parms[1].equals("POST") || parms[1].equals("PUT")) {
                    conn.setDoOutput(true);
                    OutputStream output = conn.getOutputStream();
                    output.write(parms[2].getBytes("UTF-8"));
                    output.close();
                    // Ako je obrada prošla u redu - dohvati povratnu poruku (InputStream) i pretvori ju u String
                    if ((es = conn.getErrorStream()) == null) {
                        //if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201 ) {
                        // Dohvaćamo InputStream koji vraća web servis i pretvaramo ga u JSON String
                        String res = inputStreamToString(conn.getInputStream());
                        // parsiramo podatke JSON formatu u objekt tipa Users
                        Gson gson = new Gson();
                        korisnici = gson.fromJson(res, Result.class);
                        //activityReference.get().kor = korisnici.getData()[0];
                        // metodi onPostExecute šalje se id korisnika
                        //return korisnici.getData()[0].getId();
                    } else {
                        korisnici = pripremiGresku(es);
                    }
                } else if (parms[1].equals("DELETE")) {
                    // DELETE zahtjeva prazno tijelo kod upita inače javlja grešku 400
                    conn.setDoInput(false);
                    if ((es = conn.getErrorStream()) == null) {
                        // metodi onPostExecute šalje se id korisnika
                        korisnici = new Result();
                        korisnici.setCode(conn.getResponseCode());
                        korisnici.setStatus("success");
                    } else {
                        korisnici = pripremiGresku(es);
                    }
                } else if (parms[1].equals("GET")) {
                    if ((es = conn.getErrorStream()) == null) {
                        String res = inputStreamToString(conn.getInputStream());
                        // parsiramo podatke JSON formatu u objekt tipa Users
                        Gson gson = new Gson();
                        korisnici = gson.fromJson(res, Result.class);
                    } else {
                        korisnici = pripremiGresku(es);
                    }
                }
                korisnici.setMethod(parms[1]);
                return korisnici;
            } catch (IOException e) {
                e.printStackTrace();
                activityReference.get().err.setError(e.getMessage());
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return null;
        }

        private Result pripremiGresku(InputStream es) {
            String greska = inputStreamToString(es);
            Gson gson = new Gson();
            return gson.fromJson(greska, Result.class);
        }

        // metoda prima polje objekata tipa Result
        @Override
        protected void onPostExecute(Result rez) {
            AzuriranjeActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            if (rez.getStatus().equals("error") || rez.getStatus().equals("fail")) {
                Toast.makeText(activity, "(" + rez.getCode() + ") " + rez.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            // Uspješna obrada
            if (rez.getMethod().equals("DELETE")) {
                activity.id.setText("");
                activity.korisnik.setText("");
                activity.ime.setText("");
                activity.id.requestFocus();
            } else if ((rez.getMethod().equals("POST")) && (rez.getData() != null && rez.getData().length > 0)) {
                activity.id.setText(Integer.toString(rez.getData()[0].getId()));
            } else if ((rez.getMethod().equals("GET")) && (rez.getData() != null && rez.getData().length > 0)) {
                activity.id.setText(Integer.toString(rez.getData()[0].getId()));
                activity.korisnik.setText(rez.getData()[0].getUsername());
                activity.ime.setText(rez.getData()[0].getName());
            }
            Toast.makeText(activity, "Obrada uspješna! " + rez.getData()[0].getId(), Toast.LENGTH_LONG).show();
        }
    }

    /*
        Pomoćna metoda koja dohvaća String iz primljenog input ili error streama
    */
    private static String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String res = s.hasNext() ? s.next() : "";
        s.close();
        return res;
    }
}

