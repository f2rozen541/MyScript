package com.myscript.atk.math.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.myscript.atk.math.widget.MathWidgetApi;
import com.myscript.certificate.MyCertificate;

public class MainActivity extends Activity
{
  private static final String TAG = "MainActivity";

  private MathWidgetApi mWidget;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // Set application title
    setTitle(getResources().getString(R.string.activity_name));

    mWidget = (MathWidgetApi) findViewById(R.id.mathWidget);

    if (!mWidget.registerCertificate(MyCertificate.getBytes()))
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

    mWidget.clearSearchPath();
    mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf/");
    mWidget.configure("math", "standard");

    // Configure clear button
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

}
