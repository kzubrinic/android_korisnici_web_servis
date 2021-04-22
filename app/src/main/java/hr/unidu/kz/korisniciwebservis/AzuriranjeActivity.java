package hr.unidu.kz.korisniciwebservis;

import android.annotation.SuppressLint;
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
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import hr.unidu.kz.korisniciwebservis.pojo.User;
import hr.unidu.kz.korisniciwebservis.pojo.Result;

// Ažuriranje korisnika pomoću web servisa
public class AzuriranjeActivity extends AppCompatActivity {
    private TextView id, korisnik, ime;
    private Context con;
    private String wsUrl;
    private User kor;

    @SuppressLint("SetTextI18n")
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
        // Stvaranje novog korisnika - metoda POST s praznim id-jem (vrijednost id-ja je 0)
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
        // izmjena korisnika - metoda PUT - na url se lijepi "slash" i id korisnika čiji podaci se mijenjaju
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

    // Čitanje korisnika za zadani id - pomoću web servisa
    public void citanje(View v) {
        // provjera unesenih podataka
        if (id.getText().toString().length() < 1) {
            Toast.makeText(con, "Morate unijeti id", Toast.LENGTH_LONG).show();
            return;
        }
        Gson gson = new Gson();
        User k = new User(Integer.parseInt(id.getText().toString()), korisnik.getText().toString(), ime.getText().toString());
        String kj = gson.toJson(k, User.class);
        // čitanje korisnika - metoda GET -  na url se lijepi "slash" i id korisnika čiji podaci se čitaju
        new WSHelper(this).execute(wsUrl + "/" + id.getText().toString(), "GET", kj);
    }

    // Otvara prozorčić potvrde brisanje
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
        // brisanje korisnika - metoda DELETE -  na url se lijepi "slash" i id korisnika čiji podaci se brišu
        new WSHelper(this).execute(wsUrl + "/" + id.getText().toString(), "DELETE", kj);
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        finish();
    }

    // Privatni razred - jednostavnosti radi da bi mogao bez problema ažurirati ekranska polja aktivnosti
    // Ova obrada se odrađuje u pozadini - u drugom procesu da ne blokira korisničko sučelje.
    // Po završetku obrade izvodi se metoda onPostExecute koja ažurira korisničko sučelje
    // doInBackground prima parametar tipa polje Stringova, sa vraća objekt tipa Result
    // onProgressUpdate metoda se ne koristi (tip Void)
    // onPostExecute prima parametar tipa Result
    private static class WSHelper extends AsyncTask<String, Void, Result> {
        // How to use a static inner AsyncTask class
        // To prevent leaks, you can make the inner class static. The problem with that, though, is that
        // you no longer have access to the Activity's UI views or member variables. You can pass in a
        // reference to the Context but then you run the same risk of a memory leak. (Android can't
        // garbage collect the Activity after it closes if the AsyncTask class has a strong reference to it.)
        // The solution is to make a weak reference to the Activity (or whatever Context you need).
        // https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur#answer-46166223
        private WeakReference<AzuriranjeActivity> activityReference;
        // Weak reference na aktivnost - efikasnije oslobađanje memorije
        public WSHelper(AzuriranjeActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Result doInBackground(String[] parms) {
            InputStream es;
            Result korisnici = new Result();
            HttpURLConnection conn = null;
            try {
                // metoda prima više parametara:
                // 1. parametar - URL web servisa
                // 2. parametar - metoda (POST, PUT, DELETE)
                // 3. parametar - string s JSON porukom koja se šalje
                conn = (HttpURLConnection) new URL(parms[0]).openConnection();
                // postavljamo kodnu stranicu da bi se znakovi prenijeli ispravno
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                // koristi se zadana metoda (GET, POST, PUT ili DELETE)
                conn.setRequestMethod(parms[1]);
                // POST i PUT primaju parametre u tijelu upita (OutputStream)
                if (parms[1].equals("POST") || parms[1].equals("PUT")) {
                    conn.setDoOutput(true);
                    OutputStream output = conn.getOutputStream();
                    output.write(parms[2].getBytes(StandardCharsets.UTF_8));
                    output.close();
                    // Ako je obrada prošla u redu - dohvati povratnu poruku (InputStream) i pretvori ju u String
                    //  inače se dohvaćaju podaci greške iz error streama
                    if ((es = conn.getErrorStream()) == null) {
                        // Dohvaćamo InputStream koji vraća web servis i pretvaramo ga u JSON String
                        String res = inputStreamToString(conn.getInputStream());
                        // parsiramo podatke JSON formatu u objekt tipa Result
                        Gson gson = new Gson();
                        korisnici = gson.fromJson(res, Result.class);
                       } else { // dohvaćamo error stream
                        korisnici = pripremiGresku(es);
                    }
                } else if (parms[1].equals("DELETE")) {
                    // DELETE zahtjeva prazno tijelo kod upita inače javlja grešku 400
                    conn.setDoInput(false);
                    if ((es = conn.getErrorStream()) == null) {
                        // Stvaramo prazan objekt rezultata brisanja radi konzistentne obrade s drugim metodama
                        korisnici = new Result();
                        korisnici.setCode(conn.getResponseCode());
                        korisnici.setStatus("success");
                    } else { // dohvaćamo error stream
                        korisnici = pripremiGresku(es);
                    }
                } else if (parms[1].equals("GET")) {
                    if ((es = conn.getErrorStream()) == null) {
                        String res = inputStreamToString(conn.getInputStream());
                        Gson gson = new Gson();
                        korisnici = gson.fromJson(res, Result.class);
                    } else { // dohvaćamo error stream
                        korisnici = pripremiGresku(es);
                    }
                }
                korisnici.setMethod(parms[1]);
            } catch (Exception e) {
                e.printStackTrace();
                korisnici = new Result();
                korisnici.setCode(-1);
                korisnici.setStatus("error");
                korisnici.setMessage(e.getMessage());
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return korisnici;
        }

        private Result pripremiGresku(InputStream es) {
            String greska = inputStreamToString(es);
            Gson gson = new Gson();
            return gson.fromJson(greska, Result.class);
        }

        // Ova metoda prima polje objekata tipa Result kada asinhrona obrada iz metode doInBackground završi
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Result rez) {
            // Dohvati referencu na aktivnost iz "slabe reference"
            AzuriranjeActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            if (rez.getStatus().equals("error") || rez.getStatus().equals("fail")) {
                Toast.makeText(activity, "(" + rez.getCode() + ") " + rez.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            // Uspješna obrada - ovisno o metodi ažuriraju se različiti dijelovi korisničkog sučelja
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
            Toast.makeText(activity, "Obrada uspješna!" , Toast.LENGTH_LONG).show();
        }
    }

    // Pomoćna metoda koja vraća String iz primljenog input ili error streama
    private static String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String res = s.hasNext() ? s.next() : "";
        s.close();
        return res;
    }
}

