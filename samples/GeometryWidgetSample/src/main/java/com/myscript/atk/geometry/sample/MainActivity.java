package com.myscript.atk.geometry.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.myscript.atk.geometry.widget.GeometryWidgetApi;
import com.myscript.certificate.MyCertificate;

public class MainActivity extends Activity implements GeometryWidgetApi.OnEditingListener
{
  private static boolean DBG = false;
  private static final String TAG = "MainActivity";

  private GeometryWidgetApi mWidget;
  private Handler mMainHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // Set application title
    setTitle(getResources().getString(R.string.activity_name));

    mWidget = (GeometryWidgetApi) findViewById(R.id.geometryWidget);

    if(!mWidget.registerCertificate(MyCertificate.getBytes()))
    {
      AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
      dlgAlert.setMessage("Please use a valid certificate.");
      dlgAlert.setTitle("Invalid certificate");
      dlgAlert.setCancelable(false);
      dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface dialog, int which)
                {
                  //dismiss the dialog
                }
              });
      dlgAlert.create().show();
      return;
    }

    mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf/");
    mWidget.configure("shape", "standard");
    mWidget.setOnEditingListener(this);

    mMainHandler = new Handler(getMainLooper());

    // configure clear button
    final View clearButton = findViewById(R.id.action_clear);
    if (clearButton != null)
    {
      clearButton.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(final View view)
        {
          mWidget.clear(true);
        }
      });
    }
  }

  @Override
  protected void onDestroy()
  {
    if (mWidget != null)
    {
      mWidget.release();
      mWidget = null;
    }

    super.onDestroy();
  }

  // ----------------------------------------------------------------------
  // Geometry Widget library - Items Edition

  private void showInputLength(final GeometryWidgetApi widget, final float existingValue, final long idGeom) {

    // get prompts.xml view
    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
    View promptView = layoutInflater.inflate(R.layout.input_length, null);
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
    alertDialogBuilder.setView(promptView);

    final EditText editText = (EditText) promptView.findViewById(R.id.lengthEdit);
    int _v = (int)((existingValue * 100.0f) + 0.5f);
    float v = (float)_v / 100.f;
    final String textValue = Float.toString(v);
    editText.setText(textValue, TextView.BufferType.EDITABLE);

    // setup a dialog window
    alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                String text = String.valueOf(editText.getText());
                if (textValue.equals(text)) {
                  // set existing value without loss of precision
                  widget.setValue(idGeom, existingValue);
                } else {
                  float value = Float.parseFloat(text);
                  widget.setValue(idGeom, value);
                }
              }
            })
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        widget.undo();
                        dialog.cancel();
                      }
                    });

    AlertDialog alert = alertDialogBuilder.create();
    alert.show();
  }

  private void showInputAngle(final GeometryWidgetApi widget, final float existingValue, final long idGeom) {

    // get prompts.xml view
    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
    View promptView = layoutInflater.inflate(R.layout.input_angle, null);
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
    alertDialogBuilder.setView(promptView);


    final EditText editText = (EditText) promptView.findViewById(R.id.angleEdit);

    float existingDegrees = (float) (180.0f * existingValue / Math.PI);
    int _v = (int)((existingDegrees * 100.0f) + 0.5f);
    float v = (float)_v / 100.f;
    final String textValue = Float.toString(v);
    editText.setText(textValue, TextView.BufferType.EDITABLE);

    // setup a dialog window
    alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                String text = String.valueOf(editText.getText());
                if (textValue.equals(text)) {
                  // set existing value without loss of precision
                  widget.setValue(idGeom, existingValue);
                } else {
                  float valueDegrees = Float.parseFloat(text);
                  widget.setValue(idGeom, (float) (Math.PI * valueDegrees / 180.0f));
                }
              }
            })
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        widget.undo();
                        dialog.cancel();
                      }
                    });

    AlertDialog alert = alertDialogBuilder.create();
    alert.show();
  }

  private void showInputLabel(final GeometryWidgetApi widget, final String label, final long idGeom) {
    // get prompts.xml view
    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
    View promptView = layoutInflater.inflate(R.layout.input_label, null);
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
    alertDialogBuilder.setView(promptView);

    final EditText editText = (EditText) promptView.findViewById(R.id.labelEdit);
    // setup a dialog window
    alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                String text = String.valueOf(editText.getText());
                widget.setLabel(idGeom, text);
              }
            })
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        widget.undo();
                        dialog.cancel();
                      }
                    });

    editText.setText(label, TextView.BufferType.EDITABLE);
    AlertDialog alert = alertDialogBuilder.create();
    alert.show();
  }
  @Override
  public void onEditingLengthValue(final GeometryWidgetApi widget, final float existingValue, final PointF position, final long id) {
    if (DBG)
      Log.d(TAG, "onEditingLengthValue(" + existingValue + ", (" + position.x + ", " + position.y + "), " + id + ")");

    Runnable myRunnable = new Runnable() {
      @Override
      public void run()
      {
        showInputLength(widget, existingValue, id);
      }
    };
    mMainHandler.post(myRunnable);
  }

  @Override
  public void onEditingAngleValue(final GeometryWidgetApi widget, final float existingValue, final PointF position, final long id) {
    if (DBG)
      Log.d(TAG, "onEditingAngleValue(" + existingValue + ", (" + position.x + ", " +  position.y + "), " + id +")");

    Runnable myRunnable = new Runnable() {
      @Override
      public void run()
      {
        showInputAngle(widget, existingValue, id);
      }
    };
    mMainHandler.post(myRunnable);
  }

  @Override
  public void onEditingLabel(final GeometryWidgetApi widget, final String existingLabel, final PointF position, final long id) {
    if (DBG)
      Log.d(TAG, "onEditingLabel(" + existingLabel + ", (" + position.x + ", " + position.y + "), " + id + ")");

    Runnable myRunnable = new Runnable() {
      @Override
      public void run()
      {
        showInputLabel(widget, existingLabel, id);
      }
    };
    mMainHandler.post(myRunnable);
  }
}
