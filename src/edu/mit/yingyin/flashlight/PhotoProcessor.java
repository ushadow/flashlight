package edu.mit.yingyin.flashlight;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Matrix;

public class PhotoProcessor {

  public static Bitmap toGrayScale(Bitmap bmpOriginal, int rotation) {
    int width = bmpOriginal.getWidth();
    int height = bmpOriginal.getHeight();
    int newWidth = width;
    int newHeight = height;
    if (rotation == 90) {
      newWidth = height;
      newHeight = width;
    }
      
    Bitmap bmpGraycale = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.RGB_565);
    Canvas c = new Canvas(bmpGraycale);
    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
    Paint paint = new Paint();
    paint.setColorFilter(f);
    Matrix matrix = new Matrix();
    matrix.postRotate(rotation, width / 2, height / 2);
    matrix.postTranslate((newWidth - width) / 2, (newHeight - height) / 2);
    c.drawBitmap(bmpOriginal, matrix, paint);
    return bmpGraycale;
  }
}
