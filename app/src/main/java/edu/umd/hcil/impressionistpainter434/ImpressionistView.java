package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    //contains image which drawing resembles
    private ImageView _imageView;

    //Set up drawing options
    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private Paint _paintBorder = new Paint();

    //Sets initial properties for drawing
    private int _alpha = 150;
    private int _radius = 15;
    private BrushType _brushType = BrushType.Line;

    //Used for tracking velocity
    private VelocityTracker mVelocityTracker = null;

    //Special feature: invert color and random brush options
    private boolean _invert = false;
    private boolean _random = false;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    public Bitmap getBitmap(){
        return _offScreenBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //Grabs x and y coordinates where screen is being touched
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();

        //Used for velocity tracking
        int index = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(index);

        //get copy of imageview as bitmap
        Bitmap imageViewBitmap = _imageView.getDrawingCache();

        //grabs pixel color from picture in imageview
        int colorAtTouchPixelInImage = imageViewBitmap.getPixel((int) touchX, (int) touchY);

        if(_invert == false){
            _paint.setColor(colorAtTouchPixelInImage);
        } else {
            //inverts color
            _paint.setColor(0xFFFFFF - colorAtTouchPixelInImage);
        }

        //set transparency of color
        _paint.setAlpha(_alpha);

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //if velocity is being used for brush
                if(_brushType == BrushType.Square){
                    if(mVelocityTracker == null) {
                        // Retrieve a new VelocityTracker object to track velocity
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                    else {
                        // Reset the velocity tracker
                        mVelocityTracker.clear();
                    }
                    //starts tracking the motionevent
                    mVelocityTracker.addMovement(motionEvent);
                }

                //checks if random brush is selected
                if(_random == true){
                    Random num = new Random();

                    //grabs random number to choose from brush types
                    int rand = num.nextInt(3);

                    System.out.println(rand);

                    if(rand == 0){
                        _brushType = BrushType.Flower;
                    } else if(rand == 1){
                        _brushType = BrushType.Square;
                    } else {
                        _brushType = BrushType.Line;
                    }
                }
            case MotionEvent.ACTION_MOVE:

                if(_brushType == BrushType.Flower){
                    //Draws multiple circles to create flowery effect
                    _offScreenCanvas.drawCircle(touchX,touchY,_radius,_paint);
                    _offScreenCanvas.drawCircle(touchX,touchY-10,_radius,_paint);
                    _offScreenCanvas.drawCircle(touchX+5,touchY+10,_radius,_paint);
                    _offScreenCanvas.drawCircle(touchX-10,touchY-5,_radius,_paint);
                    _offScreenCanvas.drawCircle(touchX+10,touchY-5,_radius,_paint);
                    _offScreenCanvas.drawCircle(touchX-5,touchY+10,_radius,_paint);
                }
                else if(_brushType == BrushType.Square){
                    //starts tracking the motionevent
                    mVelocityTracker.addMovement(motionEvent);

                    //computes velocity.
                    mVelocityTracker.computeCurrentVelocity(1000);

                    //Retrieve the x and y velocity for each pointer ID.
                    float xVel = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
                    float yVel = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                    //sets new radius according to velocity
                    float rad = _radius + (xVel/200) + (yVel/200);

                    //uses squares to draw
                    _offScreenCanvas.drawRect(touchX-rad, touchY - rad, touchX + rad, touchY + rad, _paint);
                }
                else {
                    //uses lines to draw
                    _offScreenCanvas.drawLine(touchX-_radius-5, touchY - _radius-5, touchX + _radius+5, touchY + _radius+5, _paint);
                }
        }

        invalidate();
        return true;
    }

    public void toggleInvert(){
        _invert = !_invert;
    }

    public void toggleRandom(){
        _random = !_random;
    }

    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

