package edu.mit.yingyin.flashlight;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
  
  private class PictureCaptureHandler implements PictureCallback {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      Bitmap picutre = BitmapFactory.decodeByteArray(data, 0, data.length);
      Bitmap gray = PhotoProcessor.toGrayScale(picutre, om.cameraDisplayOrientation());
      imageView.setVisibility(View.VISIBLE);
      imageView.getLayoutParams().height = preview.getHeight();
      imageView.getLayoutParams().width = preview.getWidth();
      imageView.setImageBitmap(gray);
    }
  }
  
  private class OrientationChangeHandler extends OrientationEventListener {
    
    public OrientationChangeHandler(Context context) {
      super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    public void enable() {
      if (canDetectOrientation())
        super.enable();
    }

    @Override
    public void onOrientationChanged(int orientation) {
      om.update(orientation);
      setCameraDisplayOrientation(om.cameraDisplayOrientation());
    }
  }

  // ***Define your Instance Variables***
  // I've left mine here but feel free to do it your own way

  private boolean isLightOn = false;
  private Camera camera;
  private Button button;
  private View v;
  private SurfaceHolder previewHolder;
  private Parameters p;
  private ImageView imageView;
  private OrientationManager om = new OrientationManager();
  private SurfaceView preview;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    OrientationChangeHandler och = new OrientationChangeHandler(this);
    och.enable();
    
    /*
     * Retrieve the layout elements (buttonFlashlight and backgroundView) and cast them into objects
     * we can use here.
     */
    button = (Button) findViewById(R.id.buttonFlashlight);
    v = (View) findViewById(R.id.backgroundView);
    v.setBackgroundColor(Color.BLACK);
    imageView = (ImageView) findViewById(R.id.imageView);
    
    /*
     * Retrieve the application's context (basically the state in which it's in) and ask it for its
     * PackageManager. This PackageManager will then allow us to figure out if the phone has a
     * Camera. You will want to use the hasSystemFeature method of the PackageManager class (google
     * it) in an if-statement as I've setup for you below
     */

    Context context = this;
    PackageManager pm = context.getPackageManager();

    if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      Log.e("flashlight", "Your device has no camera!");// this prints a message in the LogCat when we run
                                                 // our app!
      return;// we escape out of here since there's no camera
    }
    
    if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
      Log.e("flashlight", "Your device has no flash!");
      return;
    }

    // start the camera
    openCamera();
    
    preview = (SurfaceView) findViewById(R.id.preview);
    previewHolder = preview.getHolder();
    previewHolder.addCallback(new Callback() {
      
      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {}
      
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        try {
          previewHolder = holder;
          camera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      
      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.stopPreview();
        previewHolder = null;
      }});
    
    // /set the clicklistener on our button
    button.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {

        if (isLightOn) {
          /*
           * INSERT CODE here that does the following: 1. Prints to the LogCat the state you are
           * about to change the light to ("Flashlight is on/off!") 2. Turns on or off the flash
           * (google "setFlashMode" on Parameters of the camera ('p') 3. Starts or stops the preview
           * (required for camera access) [e.g. camera.stopPreview(); is run when you want to switch
           * it off] 4. Changes the background color of our backgroundView (which we'ved named 'v'
           * above) 5. Adjusts the boolean value accordingly (so we can toggle it again next time)
           */
          Log.i("flashlight", String.format("Flashlight is off!"));
          button.setText(R.string.button_turnon_flash);
          p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
          camera.setParameters(p);
          // Preivew is stopped after the image is taken.
          camera.takePicture(null, null, new PictureCaptureHandler());
          v.setBackgroundColor(Color.BLACK);
          isLightOn = false;
        } else {

          /*
           * INSERT CODE here that does the following: 1. Prints to the LogCat the state you are
           * about to change the light to ("Flashlight is on/off!") 2. Turns on or off the flash
           * (google "setFlashMode" on Parameters of the camera ('p') 3. Starts or stops the preview
           * (required for camera access) [e.g. camera.stopPreview(); is run when you want to switch
           * it off] 4. Changes the background color of our backgroundView to Color.WHITE when the
           * light is on and Color.BLACK when the light is off (which we'ved named 'v' above) 5.
           * Adjusts the boolean value accordingly (so we can toggle it again next time)
           */
          Log.i("flashlight", String.format("Flashlight is on!"));
          button.setText(R.string.button_capture);
          p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
          camera.setParameters(p);
          camera.startPreview();
          v.setBackgroundColor(Color.WHITE);
          imageView.setVisibility(View.INVISIBLE);
          isLightOn = true;
        }

      }
    });
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    openCamera();
  }
  
  @Override
  protected void onStop() {
    super.onStop();

    /*
     * Here we handle the case when the app is Stopped (via exit or by the OS) [no need for code
     * here, just understand why we've put this here in the onStop Method of the application
     * lifecycle]
     */
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }
  
  private void openCamera() {
    if (camera == null) {
      camera = Camera.open();
      p = camera.getParameters();
    }
    setCameraDisplayOrientation(90);
  }
  
  private void setCameraDisplayOrientation(int degree) {
    if (camera != null)
      camera.setDisplayOrientation(degree);
  }

}
