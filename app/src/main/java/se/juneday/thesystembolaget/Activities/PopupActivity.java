package se.juneday.thesystembolaget.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import se.juneday.thesystembolaget.R;
import se.juneday.thesystembolaget.dialogs.AgeDialog;

public class PopupActivity extends AppCompatActivity {
    private Button button;
    private Button noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
        noButton = (Button) findViewById(R.id.button2);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    public void openDialog() {
        AgeDialog ad = new AgeDialog();
        ad.show(getSupportFragmentManager(), "age dialog");
    }

   public void closeActivity() {
        finish();
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}