package ml.derek.uros2.android;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import ml.derek.uros2.desktop.Draw;
import ml.derek.uros2.desktop.ShapeDetect;
import ml.derek.uros2.desktop.util.MatType;
import ml.derek.uros2.desktop.util.Operations;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.Arrays;

public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener
{
    private CameraBridgeViewBase mOpenCvCameraView;
    private MatType selectedMat = MatType.Full;
    private double thresh1 = 150;
    private double thresh2 = 80;

    private Rect regionOfInterest = null;
    private Mat lastDoor = null;
    private float angle[] = null;
    private float regionAngle[] = null;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Core.MinMaxLocResult minMax = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create buttons for enums
        LinearLayout buttonHolder = (LinearLayout) findViewById(R.id.buttonHolder);
        for(MatType matType : MatType.values())
        {
            final Button button = new Button(getApplicationContext());
            button.setText(matType.toString());
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    selectedMat = MatType.valueOf(button.getText().toString());
                }
            });
            buttonHolder.addView(button);

        }

        // Create threshold sliders
        EditText editThresh1 = new EditText(getApplicationContext());
        editThresh1.setInputType(InputType.TYPE_CLASS_NUMBER);
        editThresh1.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {

                    double dthresh = Double.parseDouble(s.toString());
                    if (dthresh > 0)
                        thresh1 = Double.parseDouble(s.toString());
                }
                catch(NumberFormatException e)
                {
                    thresh1 = 100;
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        editThresh1.setText(String.valueOf(thresh1));
        buttonHolder.addView(editThresh1);

        // Create threshold sliders
        EditText editThresh2 = new EditText(getApplicationContext());
        editThresh2.setInputType(InputType.TYPE_CLASS_NUMBER);
        editThresh2.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    double dthresh = Double.parseDouble(s.toString());
                    if (dthresh > 0)
                        thresh2 = Double.parseDouble(s.toString());
                } catch (NumberFormatException e)
                {
                    thresh2 = 50;
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        editThresh2.setText(String.valueOf(thresh2));
        buttonHolder.addView(editThresh2);

        Log.i("camera", "called onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(640, 360);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("camera", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);


        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height)
    {
    }

    public void onCameraViewStopped()
    {
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                angle = orientation;
            }
        }
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        // Get the input frame and the door if it exists from the frame
        Mat newImage = inputFrame.rgba();
        Mat result = new Mat();
        MatOfPoint door = ShapeDetect.getDoor(thresh1, thresh2, newImage, newImage, selectedMat);

        // Draw the phones current orientation on the screen
        if(minMax != null)
        {
            Imgproc.putText(newImage, "minVal : " + minMax.minVal,
                    new Point(0, newImage.height() - 20),
                    Core.FONT_HERSHEY_SIMPLEX, 0.3,
                    new Scalar(255, 255, 255));
            Imgproc.putText(newImage, "maxVal: " + minMax.maxVal,
                    new Point(0, newImage.height() - 50),
                    Core.FONT_HERSHEY_SIMPLEX, 0.3,
                    new Scalar(255, 255, 255));
        }

        if(door != null)
        {
            Rect bounds = Imgproc.boundingRect(door);

            // Get our template to match when there is no door detected
            lastDoor = new Mat(newImage.clone(), bounds.clone());
            //Core.normalize(lastDoor, lastDoor, 0, 1, Core.NORM_MINMAX, -1, new Mat());
            regionOfInterest = bounds;

            // Draw the door and its bounds onto the image to show the user
            newImage = Draw.contours(newImage, door);
            newImage = Draw.rect(bounds, newImage);
            return newImage;
        }
        else if(lastDoor != null)
        {
            // angle[0] = <-->
            // angle[1] = <-\/
            // angle[2] = ^ \/
            // Comment!
            Imgproc.matchTemplate(newImage, lastDoor, result, Imgproc.TM_SQDIFF);
            minMax = Core.minMaxLoc(result);
            Point match = minMax.minLoc;
            Rect rect = new Rect(match, new Point(match.x + lastDoor.cols(), match.y + lastDoor.rows()));
            newImage = Draw.rect(rect, newImage);

            return newImage;
        }
        else
        {
            return newImage;
        }
    }
}