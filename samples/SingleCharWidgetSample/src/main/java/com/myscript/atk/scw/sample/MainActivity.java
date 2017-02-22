// Copyright MyScript. All rights reserved.

package com.myscript.atk.scw.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.myscript.atk.core.ui.IStroker;
import com.myscript.atk.scw.SingleCharWidgetApi;
import com.myscript.atk.text.CandidateInfo;
import com.myscript.certificate.MyCertificate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements
        SingleCharWidgetApi.OnConfiguredListener,
        SingleCharWidgetApi.OnTextChangedListener,
        SingleCharWidgetApi.OnSingleTapGestureListener,
        SingleCharWidgetApi.OnLongPressGestureListener,
        SingleCharWidgetApi.OnBackspaceGestureListener,
        SingleCharWidgetApi.OnReturnGestureListener,
        CustomEditText.OnSelectionChanged
{
  private static final String TAG = "MainActivity";

  private CustomEditText    mTextField;
  private CandidateAdapter  mCandidateAdapter;
  private LinearLayout      mCandidateBar;
  private GridView          mCandidatePanel;
  private SingleCharWidgetApi  mWidget;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    mCandidateAdapter = new CandidateAdapter(this);

    mTextField = (CustomEditText) findViewById(R.id.textField);
    mTextField.setOnSelectionChangedListener(this);

    mCandidateBar = (LinearLayout) findViewById(R.id.candidateBar);
    mCandidatePanel = (GridView) findViewById(R.id.candidatePanel);
    mCandidatePanel.setAdapter(mCandidateAdapter);

    mWidget = (SingleCharWidgetApi) findViewById(R.id.widget);

    if (!mWidget.registerCertificate(MyCertificate.getBytes())) {
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

    mWidget.setOnConfiguredListener(this);
    mWidget.setOnTextChangedListener(this);
    mWidget.setOnBackspaceGestureListener(this);
    mWidget.setOnReturnGestureListener(this);
    mWidget.setOnSingleTapGestureListener(this);
    mWidget.setOnLongPressGestureListener(this);

    mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");
    mWidget.configure("en_US", "si_text");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mWidget.dispose();
  }

  //--------------------------------------------------------------------------------
  // UI callbacks

  private class CandidateTag {
    int start;
    int end;
    String text;
  }

  public void onClearButtonClick(View v) {
    mWidget.clear();
  }

  public void onCandidateButtonClick(View v) {
    CandidateTag tag = (CandidateTag) v.getTag();
    if (tag != null) {
      mWidget.replaceCharacters(tag.start, tag.end, tag.text);
    }
  }

  public void onMoreButtonClick(View v) {
    mCandidatePanel.setVisibility(mCandidatePanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    updateCandidatePanel();
  }

  public void onEmoticonButtonClick(View v) {
    mWidget.insertString(":-)");
  }

  public void onSpaceButtonClick(View v) {
    mWidget.insertString(" ");
  }

  public void onDeleteButtonClick(View v) {
    CandidateInfo info = mWidget.getCharacterCandidates(mWidget.getInsertIndex() - 1);
    mWidget.replaceCharacters(info.getStart(), info.getEnd(), null);
  }

  public void onPencilButtonClick(View v) {
    PopupMenu popupMenu = new PopupMenu(this, v);

    popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.FELT_PEN.ordinal(), Menu.NONE, R.string.effect_felt_pen);
    popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.FOUNTAIN_PEN.ordinal(), Menu.NONE, R.string.effect_fountain_pen);
    popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.CALLIGRAPHIC_BRUSH.ordinal(), Menu.NONE, R.string.effect_calligraphic_brush);
    popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.CALLIGRAPHIC_QUILL.ordinal(), Menu.NONE, R.string.effect_calligraphic_quill);
    popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.QALAM.ordinal(), Menu.NONE, R.string.effect_qalam);

    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        mWidget.setInkEffect(IStroker.Stroking.values()[item.getItemId()]);
        return true;
      }
    });

    popupMenu.show();
  }

  @Override
  public void onSelectionChanged(EditText editText, int selStart, int selEnd) {
    Log.d(TAG, "Selection changed to [" + selStart + "-" + selEnd + "]");
    mWidget.setInsertIndex(selStart);
    updateCandidateBar();
    updateCandidatePanel();
  }

  //--------------------------------------------------------------------------------
  // Widget callbacks

  @Override
  public void onConfigured(SingleCharWidgetApi w, boolean success) {
    Log.d(TAG, "Widget configuration " + (success ? "done" : "failed"));
  }

  @Override
  public void onTextChanged(SingleCharWidgetApi w, String text, boolean intermediate) {
    Log.d(TAG, "Text changed to \"" + text + "\" (" + (intermediate ? "intermediate" : "stable") + "), insert index=" + mWidget.getInsertIndex());
    // temporarily disable selection changed listener to prevent spurious cursor jumps
    mTextField.setOnSelectionChangedListener(null);
    mTextField.setTextKeepState(text);
    mTextField.setSelection(mWidget.getInsertIndex());
    mTextField.setOnSelectionChangedListener(this);
    updateCandidateBar();
    updateCandidatePanel();
  }

  @Override
  public void onBackspaceGesture(SingleCharWidgetApi w, int index, int count) {
    Log.d(TAG, "Backspace gesture detected at index " + index + " (" + count + ")");
    CandidateInfo info = mWidget.getCharacterCandidates(index - 1);
    mWidget.replaceCharacters(info.getStart(), info.getEnd(), null);
  }

  @Override
  public void onReturnGesture(SingleCharWidgetApi w, int index) {
    Log.d(TAG, "Return gesture detected at index " + index);
    mWidget.replaceCharacters(index, index, "\n");
  }

  @Override
  public boolean onSingleTapGesture(SingleCharWidgetApi w, float x, float y) {
    Log.d(TAG, "Single tap gesture detected at x=" + x + " y=" + y);
    // we don't handle the gesture
    return false;
  }

  @Override
  public boolean onLongPressGesture(SingleCharWidgetApi w, float x, float y) {
    Log.d(TAG, "Long press gesture detected at x=" + x + " y=" + y);
    // we don't handle the gesture
    return false;
  }

  //--------------------------------------------------------------------------------
  // Candidates

  private class CandidateAdapter extends BaseAdapter {

    private Context mContext;
    private List<CandidateTag> mCandidates;

    public CandidateAdapter(Context context) {
      mContext = context;
      mCandidates = Collections.emptyList();
    }

    public void setCandidates(List<CandidateTag> candidates) {
      mCandidates = candidates;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mCandidates.size();
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      Button b;
      if (convertView == null) {
        b = new Button(mContext);
        b.setAllCaps(false);
        b.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onCandidateButtonClick(v);
          }
        });
      } else {
        b = (Button) convertView;
      }
      CandidateTag tag = mCandidates.get(position);
      b.setTag(tag);
      b.setText(tag.text.replace("\n", "\u21B2"));
      return b;
    }
  }

  // update candidates bar
  private void updateCandidateBar() {
    int index = mWidget.getInsertIndex() - 1;
    if (index < 0) {
      index = 0;
    }

    CandidateInfo info = mWidget.getWordCandidates(index);

    int start = info.getStart();
    int end = info.getEnd();
    List<String> labels = info.getLabels();
    List<String> completions = info.getCompletions();

    for (int i=0; i<3; i++) {
      Button b = (Button) mCandidateBar.getChildAt(i);

      CandidateTag tag = new CandidateTag();
      if (i < labels.size()) {
        tag.start = start;
        tag.end = end;
        tag.text = labels.get(i) + completions.get(i);
      } else {
        tag = null;
      }

      b.setTag(tag);

      if (tag != null) {
        b.setText(tag.text.replace("\n", "\u21B2"));
      } else {
        b.setText("");
      }
    }
  }

  // update candidates panel
  private void updateCandidatePanel() {
    if (mCandidatePanel.getVisibility() == View.GONE) {
      return;
    }

    List<CandidateTag> candidates = new ArrayList<>();

    int index = mWidget.getInsertIndex() - 1;
    if (index < 0) {
      index = 0;
    }

    CandidateInfo[] infos = {
      // add word-level candidates
      mWidget.getWordCandidates(index),
      // add character-level candidates
      mWidget.getCharacterCandidates(index),
    };

    for (CandidateInfo info : infos) {
      int start = info.getStart();
      int end = info.getEnd();
      List<String> labels = info.getLabels();
      List<String> completions = info.getCompletions();

      for (int i=0; i<labels.size(); i++) {
        CandidateTag tag = new CandidateTag();
        tag.start = start;
        tag.end = end;
        tag.text = labels.get(i) + completions.get(i);
        candidates.add(tag);
      }
    }

    if (candidates.isEmpty()) {
      mCandidatePanel.setVisibility(View.GONE);
    }

    mCandidateAdapter.setCandidates(candidates);
  }

}
