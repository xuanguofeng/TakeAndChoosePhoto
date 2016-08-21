package xuanguofeng.photodemos;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {


    private static final int TAKE_PHOTO_REQUEST_TWO = 444;
    private static final int TAKE_PHOTO_REQUEST_ONE = 333;
    private static final int TAKE_PHOTO_REQUEST_THREE = 555;
    private Button btn_take_photo, btn_take_photo2, btn_choose_photo, btn_choose_photo2;

    private ImageView iv_image;
    private Uri imageUri;
    //    private List<Uri> imageUris=new ArrayList<>();
    private ContentResolver resolver = null;
    private byte[] mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_take_photo = (Button) findViewById(R.id.btn_take_photo);
        btn_choose_photo = (Button) findViewById(R.id.btn_choose_photo);
        btn_choose_photo2 = (Button) findViewById(R.id.btn_choose_photo2);
        btn_take_photo2 = (Button) findViewById(R.id.btn_take_photo2);
        iv_image = (ImageView) findViewById(R.id.iv_image);


        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                imageUri = createImageUri(MainActivity.this);
//                Intent intent = new Intent();
//                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//如果不设置EXTRA_OUTPUT getData()  获取的是bitmap数据  是压缩后的
//                startActivityForResult(intent, TAKE_PHOTO_REQUEST_ONE);
                try {
                    imageUri = TakePhotoUtils.takePhoto(MainActivity.this, TAKE_PHOTO_REQUEST_THREE);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        btn_choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromAlbum();
            }
        });
        btn_choose_photo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromAlbum2();
            }
        });


        btn_take_photo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_TWO);
            }
        });

    }


    public void pickImageFromAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 111);

    }

    public void pickImageFromAlbum2() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 222);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case 111:
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Uri imageUri = data.getData();
                    Log.e("TAG", imageUri.toString());
                    iv_image.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO_REQUEST_ONE:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(MainActivity.this, imageUri);
                    Toast.makeText(MainActivity.this, "点击取消  拍照", Toast.LENGTH_LONG).show();
                    return;
                }

                try {

                    //如果拍照图片过大会无法显示
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_image.setImageBitmap(bitmap1);


                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case TAKE_PHOTO_REQUEST_TWO:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(MainActivity.this, imageUri);
                    return;
                }
                Bitmap photo = data.getParcelableExtra("data");
                iv_image.setImageBitmap(photo);

                break;
            case TAKE_PHOTO_REQUEST_THREE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }


                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), getOptions(imageUri.getPath()));
                iv_image.setImageBitmap(bitmap);


                break;
            default:
                break;

        }


    }


    private static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }


    public static void delteImageUri(Context context, Uri uri) {
        context.getContentResolver().delete(uri, null, null);

    }


    /**
     * 获取压缩图片的options
     *
     * @return
     */
    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 4;      //此项参数可以根据需求进行计算
        options.inJustDecodeBounds = false;

        return options;
    }

}
