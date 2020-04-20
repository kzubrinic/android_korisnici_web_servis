package hr.unidu.kz.korisniciwebservis;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ucitajPostavke();
    }
    public void azuriranje(View v) {
        Intent intent = new Intent(this, AzuriranjeActivity.class);
        intent.putExtra("url_web_servisa", url);
        startActivity(intent);
    }
    public void pregled(View v) {
        Intent intent = new Intent(this, PregledActivity.class);
        intent.putExtra("url_web_servisa", url);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obrada akcija izbornika opcija
        switch (item.getItemId()) {
            case R.id.prva_stavka:
                prikaziPostavke();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void prikaziPostavke() {
        Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
        // Nakon što se izmjena postavki završi, želimo ažurirati
        // komponente aktivnosti pa koristimo ovu metodu (a ne "običnu" startActivity).
        startActivityForResult(intent, 7);
    }
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == 7) {
            // Aktivnost postavki je završila obradu, primjenjujemo postavke
            if (resCode == RESULT_OK) {
                ucitajPostavke();
            }
        }
    }
    // Po pokretanju programa učitavaju se postavke ako postoje
    // Ako ih nema, primjenjuju se defaultne vrijendosti.
    // Spremljen je inicijalni URL web servisa
    private void ucitajPostavke() {
        // Dohvaća spremljene vrijednosti postavki
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        url = prefs.getString("url_web_servisa", "");
        if (url == null || url.length()<1){
            url = getString(R.string.url_web_servisa);
        }
     }

}
