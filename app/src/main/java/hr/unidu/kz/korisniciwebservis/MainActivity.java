package hr.unidu.kz.korisniciwebservis;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import hr.unidu.kz.korisniciwebservis.pojo.Praznik;

public class MainActivity extends AppCompatActivity {
    private String url;
    private RecyclerView recyclerView;
    private EditText godina, drzava;
    private ArrayList<Praznik> praznici;
    private PrazniciAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        godina = findViewById(R.id.godina);
        drzava = findViewById(R.id.drzava);
        praznici = new ArrayList<>();

        ucitajPostavke();
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

    // Poziva se aktivnost pregleda podataka
    public void praznici(View v) {
        Intent intent = new Intent(this, DohvatPodataka.class);
        intent.putExtra("godina", godina.getText().toString());
        intent.putExtra("drzava", drzava.getText().toString());
        intent.putExtra("url_web_servisa", url);
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == 0 && resCode == RESULT_OK){
            Gson gson = new Gson();
            Type tipListe = new TypeToken<ArrayList<Praznik>>(){}.getType();
            ArrayList<Praznik> pom = gson.fromJson(data.getStringExtra("json"), tipListe);
            // Adpater čuva referencu na listu pa se ne smije poslati novi objekt već se ta
            //  ista lista mora ažurirati (očistiti pa napuniti).HR
            praznici.clear();
            for(Praznik p: pom)
                praznici.add(p);
            mAdapter.notifyDataSetChanged();

            // specify an adapter (see also next example)

        }else if (reqCode == 7 && resCode == RESULT_OK) {
                // Aktivnost postavki je završila obradu, primjenjujemo postavke
                ucitajPostavke();
        }
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
    // Izbornik postavke u kojem se može promijeniti URL web servisa
    private void prikaziPostavke() {
        Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
        // Nakon što se izmjena postavki završi, želimo ažurirati
        // komponente aktivnosti pa koristimo ovu metodu (a ne "običnu" startActivity).
        startActivityForResult(intent, 7);
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
