package com.example.lenovo.mygraduation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lenovo.mygraduation.Bean.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView button_back;
    private String user_id;
    private String user_pw;
    private String user_name;
    private MyGlobal myApp;
    private RelativeLayout layout_head;
    private RelativeLayout layout_name;
    private ImageView imageView_head;
    private TextView tv_id;
    private TextView tv_save;
    private EditText et_name;
    private static final int PHOTO_GRAPH = 2;// 拍照
    private static final int PHOTO_ZOOM = 3; // 缩放
    private static final int PHOTO_RESULT = 4;// 结果
    private static final String IMAGE_UNSPECIFIED = "image/*";
    //共享参数
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    //远程数据库信息
    static private String connectString = "jdbc:mysql://192.168.199.228:3306/openfire"
            + "?autoReconnect=true&useUnicode=true&useSSL=false"
            + "&characterEncoding=utf-8&serverTimezone=UTC";
    static private String  sql_user="root";
    static private String  sql_pwd="123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Init();
        SetListener();
    }

    public void Init(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        myApp = (MyGlobal)getApplication();
        FindView();
        shared = getSharedPreferences("user", MODE_PRIVATE);
        editor = shared.edit();
        user_name = shared.getString("user_name", "");
        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("user_id", "");
        user_pw = bundle.getString("user_pw", "");
        Bitmap bitmap = myApp.getImage(user_id);
        if(bitmap != null){
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            Drawable d = (Drawable) bd;
            imageView_head.setBackground(d);
        }
        tv_id.setText(user_id);
        et_name.setText(user_name);
    }

    public void FindView(){
        button_back = (ImageView)findViewById(R.id.button_back);
        layout_head = (RelativeLayout)findViewById(R.id.info_head);
        layout_name = (RelativeLayout)findViewById(R.id.info_name);
        imageView_head = (ImageView)findViewById(R.id.image_head);
        tv_id = (TextView)findViewById(R.id.tv_id);
        tv_save = (TextView)findViewById(R.id.tv_save);
        et_name = (EditText)findViewById(R.id.et_name);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_back){
            finish();
        }
        if(id == R.id.info_head){
            AlertDialog.Builder builder=new AlertDialog.Builder(InfoActivity.this);
            builder.setTitle("头像");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            String[] items=new String[]{"相册上传","拍摄上传"};
            builder.setItems(items, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        //从相册获取图片
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
                        startActivityForResult(intent, PHOTO_ZOOM);
                    }
                    else if(which == 1){
                        //从拍照获取图片
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(),"temp.jpg")));
                        startActivityForResult(intent, PHOTO_GRAPH);
                    }
                }
            });
            builder.create().show();
        }
        if(id == R.id.tv_save){
            Bitmap bitmap = imageView_head.getDrawingCache();
            myApp.saveUserHead(user_id, bitmap);
            updateMineFragment();
            String temp_name = et_name.getText().toString();
            new Thread(){
                public void run(){
                    try {
                        Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                        Statement stmt=con.createStatement();
                        String QueryString = "update myuser set user_name = '"+temp_name +"' where user_id = '"+user_id+"';";
                        int i = stmt.executeUpdate(QueryString);
                        System.out.println("远程数据库修改成功");
                        stmt.close();
                        con.close();
                        editor.putString("user_name", temp_name);
                        editor.commit();
                        myApp.MakeToast("保存成功");
                    } catch (SQLException e) { //连接失败
                        e.printStackTrace();
                        myApp.MakeToast("姓名保存失败");
                    }
                }
            }.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (resultCode == 0) return;

        // 拍照
        if (requestCode == PHOTO_GRAPH) {
            // 设置文件保存路径
            File picture = new File(Environment.getExternalStorageDirectory()
                    + "/temp.jpg");
            startPhotoZoom(Uri.fromFile(picture));
        }

        if (data == null) return;

        // 读取相册缩放图片
        if (requestCode == PHOTO_ZOOM) {
            startPhotoZoom(data.getData());
        }

        // 处理结果
        if (requestCode == PHOTO_RESULT) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);// (0-100)压缩文件
                //此处可以把Bitmap保存到sd卡中，具体请看：http://www.cnblogs.com/linjiqin/archive/2011/12/28/2304940.html
                imageView_head.setImageBitmap(photo); //把图片显示在ImageView控件上
            }
        }
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_RESULT);
    }

    public void SetListener(){
        button_back.setOnClickListener(this);
        layout_head.setOnClickListener(this);
        tv_save.setOnClickListener(this);
    }

    private void updateMineFragment(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(3);
        EventBus.getDefault().post(eventMessage);
    }
}
