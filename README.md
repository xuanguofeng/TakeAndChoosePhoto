# TakeAndChoosePhoto
一个拍照、或者从系统相册获取照片的的demo

#### 概述
在做Android开发中还是会经常选择照片然后做上传操作的。但是其中选择照片系统的有两种方式，第一种是拍照、第二种是从相册中选择。这里分别介绍下。

其中拍照有两种方法，从系统相册选择有两种方法，会分别介绍和分析。

本篇文章属于个人总结式方便日后查看，如果大家觉得有些地方不对，欢迎留言指正。谢谢。

#### 拍照获取照片的方法
刚才说过会介绍两种方法，其实无论几种方法原理都是一个。就是通过intent发出隐式意图调用系统的照相机，然后在获取到从相机返回的图片，这里的两种主要是返回方式有两种。

1、直接返回图片。

2、提前创建好存放图片的Uri然后拍照返回后存储起来。

###### **先说第一种：**

```
 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST);
```
没错，简单的两行代码就可以调取摄像头进行拍照了，这时候我们是通过Intent指定activion: MediaStore.ACTION_IMAGE_CAPTURE去查找符合条件的程序。相机里面会对这个action做处理，这一步属于intent的操作了，这里不再赘述。


```
case TAKE_PHOTO_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "取消了拍照", Toast.LENGTH_LONG).show();
                    return;
                }
                Bitmap  photo = data.getParcelableExtra("data");
                iv_image.setImageBitmap(photo);

                break;
```
上面的代码是onActivityResult中的处理，判断request后做拍照返回处理，其中data直接返回Bitmap,不过这里要注意一点就是，这个Bitmap会经过系统压缩。所以有时候可能看起来照片并没有那么清晰。也正是由于是系统压缩的原因，这个图片基本不会很大，基本不会OOM。
###### **再说第二种方法：**
第二种方法其实也是一样的，只不过我们事先定义好uri,然后图片会存储到这个uri中，然后我们可以通过这个uri在本地找到具体的图片，然后做处理，展示。

```
  private static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }

```
上述代码是创建一个uri用来存储拍照后的照片。

```
public static void delteImageUri(Context context, Uri uri) {
        context.getContentResolver().delete(uri, null, null);

    }
```
上述代码是用来删除一个本地uri


```
   btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = createImageUri(MainActivity.this);
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//如果不设置EXTRA_OUTPUT getData()  获取的是bitmap数据  是压缩后的
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_ONE);


            }
        });
```
然后通过上述代码创建imageUri然后发起拍照，方式同样用Intent,可参第一种方法。

```
   if (resultCode == RESULT_CANCELED) {
            delteImageUri(MainActivity.this,imageUri);
                    return;
                }
  case TAKE_PHOTO_REQUEST_ONE:
                 iv_image.setImageURI(imageUri);
                    break;
```
最后就是获取拍照的照片做处理或者显示。

其中如果取消的话就删除创建的rui。

```
Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
iv_image.setImageBitmap(bitmap);
```
还可以直接通过MediaStore获取bitmap进行设置。

以上方法经测试在可以正常获取照片。

但是这样还会有个问题，就是如果图片过大的情况下，会有异常。

```
W/OpenGLRenderer: Bitmap too large to be uploaded into a texture (3120x4208, max=4096x4096)

```
如上代码所示，会直接报bitmap的过大而无法显示图片。


第一种处理方法就是对图片进行处理。
这里介绍第二种处理方式，就是用不同的方式去创建存储图片的文件。


```
public class TakePhotoUtils {



    /**
     * 拍照
     */
    public static Uri takePhoto(Activity mActivity, int flag) throws IOException {
        //指定拍照intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri = null;
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            String sdcardState = Environment.getExternalStorageState();
            File outputImage = null;
            if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                outputImage = createImageFile(mActivity);
            } else {
                Toast.makeText(mActivity.getApplicationContext(), "内存异常", Toast.LENGTH_SHORT).show();
            }
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (outputImage != null) {
                imageUri = Uri.fromFile(outputImage);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                mActivity.startActivityForResult(takePictureIntent, flag);
            }
        }

        return imageUri;
    }





    public static  File createImageFile(Activity mActivity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;//创建以时间命名的文件名称
        File storageDir = getOwnCacheDirectory(mActivity, "takephoto");//创建保存的路径
        File image = new File(storageDir.getPath(), imageFileName + ".jpg");
        if (!image.exists()) {
            try {
                //在指定的文件夹中创建文件
                image.createNewFile();
            } catch (Exception e) {
            }
        }

        return image;
    }


    /**
     * 根据目录创建文件夹
     * @param context
     * @param cacheDir
     * @return
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        //判断sd卡正常挂载并且拥有权限的时候创建文件
        if ( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }


    /**
     * 检查是否有权限
     * @param context
     * @return
     */
    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == 0;
    }


}

```

代码没什么难点，简单写了一个工具类，里面封装了一个拍照的方法，并返回一个存储拍照后的路径。
路径是自己制定文件夹后创建一个文件，用于存储照片。文件名是根据时间命名的，以免重复。

```
    if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }


                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), getOptions(imageUri.getPath()));
                iv_image.setImageBitmap(bitmap);

```
然后在onActivityResult的方法中进行处理，这里也最好对图片进行下压缩处理。然后就可以正常显示拍照后的图片了。



```

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
```
这里的只是简单的处理方法，按照指定参数压缩下，这里的inSapleSizes是需要根据自己需求进行算法的。

这样基本就可以通过拍照来获取照片了

#### 从相册选择照片来展示
其实拍照主要也是通过intent来调用系统相册，然后通过返回数据在onActivityResult中进行处理。

```

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
```

如图两种方式均可以调用系统相册进行选择照片。

```
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
    
```

然后返回的处理方式基本是一样的 拿到uri后进行对图片处理就好了。这里说明下如果图片过大可能也需要进行二次处理。

可能还有还有些地方说的不够清晰或者和错误。希望大家指正。





