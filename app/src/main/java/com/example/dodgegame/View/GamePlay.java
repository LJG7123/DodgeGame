package com.example.dodgegame.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dodgegame.Bullet;
import com.example.dodgegame.DBManager;
import com.example.dodgegame.MainActivity;
import com.example.dodgegame.R;
import com.example.dodgegame.RepeatListener;
/*import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;*/

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GamePlay extends Fragment {

    MainActivity mainActivity;
    String myDeviceId;

    ViewGroup rootView;
    Timer timer;

    static long score;
    TextView current_score;
    ImageButton btn_left, btn_up, btn_down, btn_right;
    ImageView craft;

    ArrayList<Bullet> bullets = new ArrayList();
    static ConstraintLayout gameLayout, rightWall;
    static LinearLayout bottomWall;

    AlertDialog ad1,ad2;
    NumberPicker npName1,npName2, npName3;
    String initials;
    boolean registerRank = false;
    ProgressDialog mProgressDialog;
    long score_10th;

    DBManager dbmanager;
    SQLiteDatabase sqlitedb;

    boolean GameOver=false;

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        myDeviceId = mainActivity.myDeviceId;


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.game_play, container, false);

        btn_left = rootView.findViewById(R.id.btn_left);
        btn_up = rootView.findViewById(R.id.btn_up);
        btn_down = rootView.findViewById(R.id.btn_down);
        btn_right = rootView.findViewById(R.id.btn_right);
        craft = rootView.findViewById(R.id.craft);
        gameLayout = rootView.findViewById(R.id.game_layout);
        bottomWall = rootView.findViewById(R.id.operation_keys);
        rightWall = rootView.findViewById(R.id.right_wall);
        current_score = rootView.findViewById(R.id.current_score);

        btn_left.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (craft.getX() > 0) {
                    craft.setX(craft.getX() - 5);
                }
            }
        }));
        btn_up.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (craft.getY() > 0) {
                    craft.setY(craft.getY() - 5);
                }
            }
        }));
        btn_down.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (craft.getY() + craft.getHeight() < bottomWall.getY()) {
                    craft.setY(craft.getY() + 5);
                }
            }
        }));
        btn_right.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (craft.getX() + craft.getWidth() < rightWall.getX()) {
                    craft.setX(craft.getX() + 5);
                }
            }
        }));

        rootView.post(new Runnable() {
            @Override
            public void run() {
                // ?????? ??????
                createBullets("??????", 10);

                /** ???????????? ???????????? ??????
                 * 1. ?????? ??????
                 * 2. ????????????
                 * 3. ??????(???) ??????
                 ??? ???????????? ??????????????? ?????? */
                final Handler handler = new Handler() {
                    //  ?????? ?????? ?????? ??????.
                    long startTime = System.currentTimeMillis();
                    long normalSeconds = 5;
                    long specialSeconds = 5;

                    public void handleMessage(Message msg) {

                        for (int i = 0; i < bullets.size(); i++) {
                            // 1. ?????? ????????? ??????
                            bullets.get(i).move();
                            bullets.get(i).reflection(bottomWall.getY() - bullets.get(i).getHeight(), rightWall.getX() - bullets.get(i).getWidth());

                            if(GameOver) break;

                            // 2. ?????? ??????
                            if(((int)bullets.get(i).getX() - 75 < (int)craft.getX() && (int)craft.getX() < (int)bullets.get(i).getX() + 75)
                                    && ((int)bullets.get(i).getY() - 75 < (int)craft.getY() && (int)craft.getY() < (int)bullets.get(i).getY() + 75)) {
                                if(isCollisionDetected(craft, (int)craft.getX(), (int)craft.getY(), bullets.get(i), (int)bullets.get(i).getX(), (int)bullets.get(i).getY())) {
                                    timer.cancel();
                                    GameOver = true;

                                    btn_up.setEnabled(false);
                                    btn_down.setEnabled(false);
                                    btn_left.setEnabled(false);
                                    btn_right.setEnabled(false);

                                    showCollisionDialog();
                                    break;
                                }
                            }
                        }

                        if(!GameOver) {
                            // 3. ?????? ??????
                            long currentTime = System.currentTimeMillis();
                            score = (currentTime - startTime) / 1000;
                            current_score.setText("?????? ?????? : " + score);

                            if(((currentTime - startTime) / 1000) == normalSeconds) { //5??? ??????
                                createBullets("??????", 1); //?????? 1?????? ??????
                                normalSeconds += 5;
                            }
                            if(((currentTime - startTime) / 1000) == specialSeconds) { //10??? ??????
                                createBullets("??????", 1); //?????? 1?????? ??????
                                specialSeconds += 10;
                            }
                        }
                    }
                };
                TimerTask timertask = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                };
                timer = new Timer();
                timer.schedule(timertask, 0, 10);

            }
        });
        return rootView;
    }

    public void createBullets(String type, int count) {
        int deviceWidth = (int) rightWall.getX();
        int deviceHeight = (int) bottomWall.getY();
        int random; //?????? ?????? ????????? ?????? ??????
        int xsp_random, ysp_random;

        for (int i = 0; i < count; i++) {
            if(type == "??????") {
                xsp_random = new Random().nextInt(10) + 1;
                ysp_random = new Random().nextInt(10) + 1;
            }
            else {
                xsp_random = new Random().nextInt(10) + 21;
                ysp_random = new Random().nextInt(10) + 21;
            }
            random = new Random().nextInt(2 * (deviceWidth + deviceHeight));

            if (0 <= random && random < deviceWidth) {
                bullets.add(new Bullet(getContext(), random, 0, xsp_random, ysp_random));
            } else if (deviceWidth <= random && random < deviceWidth + deviceHeight) {
                bullets.add(new Bullet(getContext(), deviceWidth, random - deviceWidth, xsp_random, ysp_random));
            } else if (deviceWidth + deviceHeight <= random && random < 2 * deviceWidth + deviceHeight) {
                bullets.add(new Bullet(getContext(), random - (deviceWidth + deviceHeight), deviceHeight, xsp_random, ysp_random));
            } else {
                bullets.add(new Bullet(getContext(), 0, random - (2 * deviceWidth + deviceHeight), xsp_random, ysp_random));
            }

            if(type != "??????")    bullets.get(bullets.size()-1).setImageResource(R.drawable.bullet_special);
            gameLayout.addView(bullets.get(bullets.size()-1));
        }
    }

    public void showCollisionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.collision_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);   //???????????? ??????

        TextView gameOverScore = dialogView.findViewById(R.id.info_2);
        gameOverScore.setText(Long.toString(score));

        Button btn_retry = dialogView.findViewById(R.id.btn_retry);
        Button btn_rank_register = dialogView.findViewById(R.id.btn_rank_register);
        Button btn_main = dialogView.findViewById(R.id.btn_main);

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ad1.dismiss();
                mainActivity.onFragmentChange("????????????");
            }
        });

        btn_rank_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(registerRank == false) {
                    mProgressDialog = ProgressDialog.show(getContext(), "",
                            "?????? ?????? ???????????? ?????? ???...", true);
                    mProgressDialog.show();

                    dbmanager = new DBManager(mainActivity);
                    sqlitedb = dbmanager.getReadableDatabase();

                    String sql = "SELECT * FROM Score ORDER BY score desc";

                    //Cursor cursor = sqlitedb.rawQuery("SELECT * FROM Score ORDER BY " + "score" + ";");
                    Cursor cursor = sqlitedb.rawQuery(sql,null);

                    long lowest_score=0;

                    int i = 0;
                    while(cursor.moveToNext()) {
                        lowest_score = cursor.getLong(cursor.getColumnIndex("score"));
                        i++;
                        if(i>=10) break;
                    }
                    mProgressDialog.dismiss();
                    if (score > lowest_score) {
                        showCollisionDialog_rank();
                    } else {
                        if(i>=10)
                            Toast.makeText(getContext(), "??????! ????????? ?????? ????????? ?????? ????????? ??? ??? ????????????. ?????? ????????? ????????? ?????? ????????? " + (lowest_score + 1) + "????????????.", Toast.LENGTH_LONG).show();
                        else
                            showCollisionDialog_rank();
                    }
                }
                else { Toast.makeText(mainActivity, "?????? ??????????????? ???????????????.", Toast.LENGTH_SHORT).show(); }
            }
        });

        btn_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ad1.dismiss();
                mainActivity.onFragmentChange("????????????");
            }
        });
        ad1 = builder.show();
    }

    public void showCollisionDialog_rank() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.collision_dialog_rank, null);
        builder.setView(dialogView);

        npName1 = dialogView.findViewById(R.id.npName1);
        npName2 = dialogView.findViewById(R.id.npName2);
        npName3 = dialogView.findViewById(R.id.npName3);

        String[] alphabet = {"A","B","C","D","E","F","G","H", "I", "J", "K","L",
                "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}; //etc

        npName1.setMaxValue(0);
        npName1.setMaxValue(25);
        npName1.setDisplayedValues(alphabet);

        npName2.setMaxValue(0);
        npName2.setMaxValue(25);
        npName2.setDisplayedValues(alphabet);

        npName3.setMaxValue(0);
        npName3.setMaxValue(25);
        npName3.setDisplayedValues(alphabet);

        Button btn_ok = dialogView.findViewById(R.id.btn_ok);
        Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initials = String.valueOf((char)(npName1.getValue() + 65)) +
                        (char)(npName2.getValue() + 65) +
                        (char)(npName3.getValue() + 65);

                try {
                    dbmanager = new DBManager(mainActivity);
                    sqlitedb = dbmanager.getWritableDatabase();

                    // ???????????? ????????? ????????? ??????
                    ContentValues values = new ContentValues();
                    values.put("DeviceId", myDeviceId);
                    values.put("initials", initials);
                    values.put("score", score);

                    // ???????????? ??????
                    long newRowId = sqlitedb.insert("Score", null, values);

                    sqlitedb.close();
                    dbmanager.close();

                    Toast.makeText(mainActivity, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    registerRank = true;
                    ad2.dismiss();

                } catch(SQLiteException e) {
                    Toast.makeText(mainActivity,  e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ad2.dismiss();
            }
        });
        ad2 = builder.show();
    }

    public static boolean isCollisionDetected(View view1, int x1, int y1, View view2, int x2, int y2) {

        Bitmap bitmap1 = getViewBitmap(view1);
        Bitmap bitmap2 = getViewBitmap(view2);;
        if (bitmap1 == null || bitmap2 == null) { throw new IllegalArgumentException("bitmaps cannot be null"); }

        Rect bounds1 = new Rect(x1, y1, x1 + bitmap1.getWidth(), y1 + bitmap1.getHeight());
        Rect bounds2 = new Rect(x2, y2, x2 + bitmap2.getWidth(), y2 + bitmap2.getHeight());
        if (Rect.intersects(bounds1, bounds2)) {
            Rect collisionBounds = getCollisionBounds(bounds1, bounds2);

            for (int i = collisionBounds.left; i < collisionBounds.right; i++) {
                for (int j = collisionBounds.top; j < collisionBounds.bottom; j++) {
                    int bitmap1Pixel = bitmap1.getPixel(i - x1, j - y1);
                    int bitmap2Pixel = bitmap2.getPixel(i - x2, j - y2);
                    if (isFilled(bitmap1Pixel) && isFilled(bitmap2Pixel)) {
                        bitmap1.recycle();
                        bitmap2.recycle();
                        return true;
                    }
                }
            }
        }
        bitmap1.recycle();
        bitmap2.recycle();
        return false;
    }

    private static Bitmap getViewBitmap(View v) {
        if (v.getMeasuredHeight() <= 0) {
            int specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            v.measure(specWidth, specWidth);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);
            return b;
        }
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    private static boolean isFilled(int pixel) {
        return pixel != Color.TRANSPARENT;
    }

    private static Rect getCollisionBounds(Rect rect1, Rect rect2) {
        int left = Math.max(rect1.left, rect2.left);
        int top = Math.max(rect1.top, rect2.top);
        int right = Math.min(rect1.right, rect2.right);
        int bottom = Math.min(rect1.bottom, rect2.bottom);
        return new Rect(left, top, right, bottom);
    }
}