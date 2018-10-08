package com.sanvar.qiqiaoban;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    GridLayout gridLayou;
    ImageView iv[][] = new ImageView[3][5];
    GestureDetector detector;
    ImageView ivSrc;
    ImageView currentNullView;
    int smailViewWidth;
    TranslateAnimation up, down, left, right;
    boolean isMoveing;
    boolean isBegining;
    int count;

    File cropFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initViews(BitmapFactory.decodeStream(getAssets().open("xiongmao.jpg")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initAnimaation();
        detector = new GestureDetector(this, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.btn_random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBegining = false;
                randomMove();
            }
        });
        findViewById(R.id.btn_get_bm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void initViews(Bitmap bm) {
        int w = getResources().getDisplayMetrics().widthPixels / 5 - 5;
        gridLayou = findViewById(R.id.gridLayout);
        gridLayou.removeAllViews();
        smailViewWidth = w;
        int bw = 0;
        ivSrc = findViewById(R.id.ivsrc);
        ivSrc.setImageBitmap(bm);
        bw = bm.getWidth() / 5;
        for (int i = 0; i < iv.length; i++) {
            for (int j = 0; j < iv[i].length; j++) {
                ImageView imageView = new ImageView(this);
                RelativeLayout.LayoutParams pm = new RelativeLayout.LayoutParams(w, w);
                pm.setMargins(1, 1, 1, 1);
                imageView.setLayoutParams(pm);
                if (bm != null) {
                    Bitmap newbm = Bitmap.createBitmap(bm, j * bw, i * bw, bw, bw);
                    imageView.setImageBitmap(newbm);
                    imageView.setTag(new ImgInfo(i, j, newbm));
                }
                iv[i][j] = imageView;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isBegining = true;
                        if (isValid((ImageView) view)) {
                            showAnimation((ImageView) view);
                        }
                    }
                });
                gridLayou.addView(imageView);
            }
        }
        // 最后的一个设置为空白的。
        setNullImage(iv[2][4]);
    }


    private void initAnimaation() {
        up = new TranslateAnimation(0.01f, -smailViewWidth, 0.01f, 0.01f);
        down = new TranslateAnimation(0.01f, smailViewWidth, 0.01f, 0.01f);
        left = new TranslateAnimation(0.01f, 0.01f, 0.01f, -smailViewWidth);
        right = new TranslateAnimation(0.01f, 0.01f, 0.01f, smailViewWidth);
    }


    /**
     * ———————————————————————————————
     * | 0.0 | 0.1 | 0.2 | 0.3 | 0.4 |
     * ———————————————————————————————
     * | 1.0 | 1.1 | 1.2 | 1.3 | 1.4 |
     * ———————————————————————————————
     * | 2.0 | 2.1 | 2.2 | 2.3 | 2.4 |
     * ———————————————————————————————
     * x.y 存储
     */
    private boolean isValid(ImageView iv) {
        ImgInfo info = (ImgInfo) iv.getTag();
        ImgInfo currentInfo = (ImgInfo) currentNullView.getTag();
        if (info.x == currentInfo.x && info.y == currentInfo.y + 1) {
            // 在空格的右边
            Log.d(TAG, "isValid: 右");
            return true;
        } else if (info.x == currentInfo.x && info.y == currentInfo.y - 1) {
            // 在空格的左边
            Log.d(TAG, "isValid: 左");
            return true;
        } else if (info.y == currentInfo.y && info.x == currentInfo.x + 1) {
            //在空格的下边
            Log.d(TAG, "isValid: 下");
            return true;
        } else if (info.y == currentInfo.y && info.x == currentInfo.x - 1) {
            // 在空格的上面
            Log.d(TAG, "isValid: 上");
            return true;
        }
        return false;
    }


    private void setNullImage(ImageView v) {
        v.setImageBitmap(null);
        currentNullView = v;
    }

    private void showAnimation(final ImageView v) {
        if (isMoveing) return;
        Animation animation = null;
        if (v.getX() > currentNullView.getX()) {
            animation = up;
        } else if (v.getX() < currentNullView.getX()) {
            animation = down;
        } else if (v.getY() > currentNullView.getY()) {
            animation = left;
        } else {
            animation = right;
        }
        animation.setDuration(50);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isMoveing = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isMoveing = false;
                v.clearAnimation();
                swapDate(v);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);
    }

    private void swapDate(ImageView v) {
        ImgInfo currentInfo = (ImgInfo) currentNullView.getTag();
        ImgInfo tag = (ImgInfo) v.getTag();
        currentInfo.bm = tag.bm;
        currentInfo.bx = tag.bx;
        currentInfo.by = tag.by;
        currentNullView.setTag(currentInfo);
        currentNullView.setImageBitmap(tag.bm);
        count++;
        setNullImage(v);
        if (isBegining && isSuccessed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示").setMessage("恭喜你，通关了。")
                    .setCancelable(false)
                    .setNegativeButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.show();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return detector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    private static final String TAG = "MainActivity";

    @Override
    public boolean onFling(MotionEvent e, MotionEvent e1, float v, float v1) {
        int d = getDirection(e, e1);
        isBegining = true;
        selectTagView(d, true);
        return false;
    }


    private void randomMove() {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int r = random.nextInt(4) + 1;
            selectTagView(r, false);
        }
        count = 0;
    }

    private void selectTagView(int d, boolean useAnimation) {
        ImgInfo info = (ImgInfo) currentNullView.getTag();
        int x = info.x;
        int y = info.y;
        if (d == 1) {
            y++;
        } else if (d == 2) {
            y--;
        } else if (d == 3) {
            x++;
        } else if (d == 4) {
            x--;
        }
        // 在3行5列的范围内。
        if (x >= 0 && x < iv.length && y >= 0 && y < iv[0].length) {
            if (useAnimation) {
                showAnimation(iv[x][y]);
            } else {
                swapDate(iv[x][y]);
            }
        }
    }

    private int getDirection(MotionEvent e, MotionEvent e1) {
        if (Math.abs(e.getX() - e1.getX()) > Math.abs(e.getY() - e1.getY())) {
            // 横向滑动
            // e.getX() > e1.getX() 为向左，否则为向右
            return (e.getX() > e1.getX()) ? 1 : 2;
        } else {
            // 纵向滑动
            // e.getY() > e1.getY() 为向上，否则为向下
            return (e.getY() > e1.getY()) ? 3 : 4;
        }
    }

    private boolean isSuccessed() {
        boolean result = true;
        for (int i = 0; i < iv.length; i++) {
            for (int j = 0; j < iv[i].length; j++) {
                if (iv[i][j] == currentNullView) continue;
                ImgInfo info = (ImgInfo) iv[i][j].getTag();
                if (!info.valid()) {
                    result = false;
                    return result;
                }
            }
        }
        return result;
    }

    Dialog bottomDialog;

    private void showDialog() {
        if (bottomDialog == null) {
            bottomDialog = new Dialog(this, R.style.BottomDialog);
        }
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.findViewById(R.id.btn_take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                takePhoto();
            }
        });
        bottomDialog.findViewById(R.id.btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                PhotoUtils.openPic(MainActivity.this, 101);
            }
        });
        bottomDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 0) {
            sendCameraIntent();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
        }

    }

    String imgPaht;
    Uri imageUrl;

    private void sendCameraIntent() {
        File f = new File(Environment.getExternalStorageDirectory(), "images");
        f.mkdir();
        File newFile = new File(f, "img.jpg");
        imgPaht = newFile.getAbsolutePath();
        imageUrl = FileProvider.getUriForFile(this, "com.sanvar.qiqiaoban.fileprovider", newFile);   // 主要就是这行代码，通过FileProvider获取文件的uri
        PhotoUtils.takePicture(this, imageUrl, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == 0) {
                    sendCameraIntent();
                } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == 0) {

                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: " + requestCode);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 100) {
                if (cropFile == null) {
                    cropFile = new File(Environment.getExternalStorageDirectory(), "crop.jpg");
                }
                Uri uri = Uri.fromFile(cropFile);
                PhotoUtils.cropImageUri(this, imageUrl, uri, 480, 320, 480, 320, 102);
            } else if (requestCode == 101) {
                if (cropFile == null) {
                    cropFile = new File(Environment.getExternalStorageDirectory(), "crop.jpg");
                }

                Uri newUri = Uri.parse(PhotoUtils.getPath(this, data.getData()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.w(TAG, "onActivityResult: "+ newUri.getPath() );
                    File f = new File(Environment.getExternalStorageDirectory(),"images");

                    newUri = FileProvider.getUriForFile(this, "com.sanvar.qiqiaoban.fileprovider", new File(newUri.getPath()));
                }

                Uri uri = Uri.fromFile(cropFile);
                PhotoUtils.cropImageUri(this, newUri, uri, 480, 320, 480, 320, 102);
            } else if (requestCode == 102) {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(cropFile.getAbsolutePath());
                    initViews(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy: ");
    }
}
