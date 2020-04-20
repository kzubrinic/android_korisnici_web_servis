package hr.unidu.kz.korisniciwebservis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class PrefsActivity extends AppCompatActivity {
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        i = getIntent();
    }

    // Kada se zatvara ekran, vraća informaciju o tome pozivatelju kako bi
    // se primijenjene postavke mogle odmah primijeniti. Inače bi se primijenile
    // tek po ponovnom pokretanju aktivnosti.
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, i);
        // završetak - potvrda
        finish();
    }
}
