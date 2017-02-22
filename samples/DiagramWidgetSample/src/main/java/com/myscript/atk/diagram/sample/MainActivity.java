package com.myscript.atk.diagram.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.myscript.atk.diagram.widget.DiagramWidgetApi;
import com.myscript.certificate.MyCertificate;

public class MainActivity extends Activity
{
  private DiagramWidgetApi mWidget;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // Set application title
    setTitle(getResources().getString(R.string.activity_name));

    //Retrieve Diagram Widget API
    mWidget = (DiagramWidgetApi) findViewById(R.id.diagramWidget);

    //Try to register the MyScript certificate
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

    //Tell the widget API where to find the configuration files (*.conf)
    mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf/");

    // Configure the recognition engine
    mWidget.configure("analyzer", "diagram");
    mWidget.configure("shape", "diagram");
    mWidget.configure("en_US", "cur_text");

    // configure clear button
    final View clearButton = findViewById(R.id.action_clear);
    if (clearButton != null) {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!mWidget.hasSelection())
                    mWidget.clear();
                else
                    mWidget.clearSelection();
            }
        });
    }

    final View undoButton = findViewById(R.id.action_undo);
    if (undoButton != null)
    {
        undoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                if (mWidget.canUndo())
                    mWidget.undo();
            }
        });
    }

      final View redoButton = findViewById(R.id.action_redo);
      if (redoButton != null)
      {
          redoButton.setOnClickListener(new View.OnClickListener()
          {
              @Override
              public void onClick(final View view)
              {
                  if (mWidget.canRedo())
                      mWidget.redo();
              }
          });
      }

      final View convertButton = findViewById(R.id.action_convert);
      if (convertButton != null)
      {
          convertButton.setOnClickListener(new View.OnClickListener()
          {
              @Override
              public void onClick(final View view)
              {
                      mWidget.beautify();
              }
          });
      }
  }

  @Override
  protected void onDestroy()
  {
    if (mWidget != null)
    {
      //This method destroys the widget and clear all diagram objects of the memory
      mWidget.release();
      mWidget = null;
    }

    super.onDestroy();
  }

}
