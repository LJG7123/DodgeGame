package com.example.dodgegame.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dodgegame.DBManager;
import com.example.dodgegame.ListView.ListViewAdapter;
import com.example.dodgegame.ListView.RankData;
import com.example.dodgegame.MainActivity;
import com.example.dodgegame.NetworkStatus;
import com.example.dodgegame.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameRank extends Fragment {
    String myDeviceId;
    MediaPlayer mediaPlayer;

    //FirebaseFirestore db;
    ArrayList<RankData> dataList = new ArrayList();

    String deviceId, initials;
    Long score;
    ListView lv_rank;

    MainActivity mainActivity;
    Button btn_returnMenu;

    ProgressDialog mProgressDialog;

    Button btn_edit_save;
    String btnStatus = "edit";
    EditText edt_message;
    String deviceId_1st = "초기값";

    DBManager dbmanager;
    SQLiteDatabase sqlitedb;

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        myDeviceId = mainActivity.myDeviceId;

        mProgressDialog = ProgressDialog.show(getContext(), "",
                "랭킹 정보를 불러오는 중...", true);
        mProgressDialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.game_rank , container, false);

        btn_returnMenu = rootView.findViewById(R.id.btn_returnMenu);
        lv_rank = rootView.findViewById(R.id.lv_rank);
        btn_edit_save = rootView.findViewById(R.id.btn_edit_save);
        edt_message = rootView.findViewById(R.id.edt_message);

        try {
            // Personnel 테이블에서 인물정보 추출
            dbmanager = new DBManager(mainActivity);
            sqlitedb = dbmanager.getReadableDatabase();
            //Cursor cursor = sqlitedb.query("Score", null, null, null, null, null, null);

            String sql = "SELECT * FROM Score ORDER BY score desc";

            //Cursor cursor = sqlitedb.rawQuery("SELECT * FROM Score ORDER BY " + "score" + ";");
            Cursor cursor = sqlitedb.rawQuery(sql,null);

            // 각 인물정보의 반복 출력을 통한 목록화

            dataList.add(new RankData("","이니셜",(long)0));
            int i = 0;
            while(cursor.moveToNext()) {

                deviceId = cursor.getString(cursor.getColumnIndex("DeviceId"));
                if (deviceId_1st == "초기값") deviceId_1st = deviceId;
                initials = cursor.getString(cursor.getColumnIndex("initials"));
                score = cursor.getLong(cursor.getColumnIndex("score"));

                dataList.add(new RankData(deviceId, initials, score));

                i++;
                if(i>=10) break;
            }

            cursor.close();
            sqlitedb.close();
            dbmanager.close();

            ListViewAdapter adapter = new ListViewAdapter(getContext(), R.layout.rank_item, dataList, myDeviceId);
            lv_rank.setAdapter(adapter);

            register_button_listener();

            mProgressDialog.dismiss();

        } catch(SQLiteException e) {
            Toast.makeText(mainActivity,  e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return rootView;
    }

    void register_button_listener() {
        btn_returnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onFragmentChange("메뉴화면");
                /*mediaPlayer = MediaPlayer.create(getContext(), R.raw.cancel);
                mediaPlayer.start();*/
            }
        });

        btn_edit_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*mediaPlayer = MediaPlayer.create(getContext(), R.raw.ok);
                mediaPlayer.start();*/

                if (btnStatus == "edit") {
                    if (myDeviceId.equals(deviceId_1st)) {
                        edt_message.setEnabled(true);
                        btn_edit_save.setText("저장");
                        btnStatus = "save";
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("랭킹 1위의 특권")
                                .setMessage("랭킹 1위만 편집이 가능합니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                } else if (btnStatus == "save") {
                    Map<String, Object> map = new HashMap<>();
                    map.put("message", edt_message.getText().toString());
                    //db.collection("Message").document("Rank_1st").set(map);
                    edt_message.setEnabled(false);
                    Toast.makeText(getContext(), "메세지가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    btn_edit_save.setText("편집");
                    btnStatus = "edit";
                }
            }
        });
    }
}