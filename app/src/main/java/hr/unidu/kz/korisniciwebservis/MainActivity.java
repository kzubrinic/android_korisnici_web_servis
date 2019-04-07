package hr.unidu.kz.korisniciwebservis;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void azuriranje(View v) {
        Intent intent = new Intent(this, AzuriranjeActivity.class);
        startActivity(intent);
    }
    public void pregled(View v) {
        Intent intent = new Intent(this, PregledActivity.class);
        startActivity(intent);
    }
}
