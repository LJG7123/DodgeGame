package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private long backKeyPressedTime = 0;
    public String myDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.example.dodgegame.View.GameMenu()).commit();
    }

    public void onFragmentChange(String fragment) {
        switch(fragment) {
            case "메뉴화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.example.dodgegame.View.GameMenu()).commit();
                break;
            case "게임화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.example.dodgegame.View.GamePlay()).commit();
                break;
            case "랭크화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.example.dodgegame.View.GameRank()).commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\'버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(this);
            System.runFinalizersOnExit(true);
            System.exit(0);
        }
    }
}