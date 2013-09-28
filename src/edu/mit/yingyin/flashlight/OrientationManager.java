package edu.mit.yingyin.flashlight;


public class OrientationManager {

  public enum Orientation {
    PORTRAIT_NORMAL, PORTRAIT_INVERTED, LANDSCAPE_NORMAL, LANDSCAPE_INVERTED
  };

  private Orientation o = Orientation.PORTRAIT_NORMAL;

  public void update(int orientation) {
    if (orientation >= 315 || orientation < 45) {
      o = Orientation.PORTRAIT_NORMAL;
    } else if (orientation < 315 && orientation >= 225) {
      o = Orientation.LANDSCAPE_NORMAL;
    } else if (orientation < 225 && orientation >= 135) {
      o = Orientation.PORTRAIT_INVERTED;
    } else {
      o = Orientation.LANDSCAPE_INVERTED;
    }
  }

  public Orientation orientation() {
    return o;
  }
  
  public int cameraDisplayOrientation() {
    switch (o) {
      case PORTRAIT_NORMAL:
        return 90;
      case LANDSCAPE_NORMAL:
        return 0;
      case LANDSCAPE_INVERTED:
        return 180;
      default:
        return 90;
    }
  }

}
