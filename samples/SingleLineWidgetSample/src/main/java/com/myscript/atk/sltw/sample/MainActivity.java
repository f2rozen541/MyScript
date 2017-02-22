// Copyright MyScript. All rights reserved.

package com.myscript.atk.sltw.sample;

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
import com.myscript.atk.sltw.SingleLineWidgetApi;
import com.myscript.atk.text.CandidateInfo;
import com.myscript.certificate.MyCertificate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements
        SingleLineWidgetApi.OnConfiguredListener,
        SingleLineWidgetApi.OnTextChangedListener,
        SingleLineWidgetApi.OnSingleTapGestureListener,
        SingleLineWidgetApi.OnLongPressGestureListener,
        SingleLineWidgetApi.OnReturnGestureListener,
        SingleLineWidgetApi.OnEraseGestureListener,
        SingleLineWidgetApi.OnSelectGestureListener,
        SingleLineWidgetApi.OnUnderlineGestureListener,
        SingleLineWidgetApi.OnInsertGestureListener,
        SingleLineWidgetApi.OnJoinGestureListener,
        SingleLineWidgetApi.OnOverwriteGestureListener,
        SingleLineWidgetApi.OnUserScrollBeginListener,
        SingleLineWidgetApi.OnUserScrollEndListener,
        SingleLineWidgetApi.OnUserScrollListener,
        CustomEditText.OnSelectionChanged
{
  private static final String TAG = "MainActivity";

  private CustomEditText      mTextField;
  private CandidateAdapter    mCandidateAdapter;
  private LinearLayout        mCandidateBar;
  private GridView            mCandidatePanel;
  private SingleLineWidgetApi mWidget;
  private int                 isCorrectionMode;

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

    mWidget = (SingleLineWidgetApi) findViewById(R.id.widget);

    if (!mWidget.registerCertificate( MyCertificate.getBytes())) {
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
    mWidget.setOnReturnGestureListener(this);
    mWidget.setOnEraseGestureListener(this);
    mWidget.setOnSelectGestureListener(this);
    mWidget.setOnUnderlineGestureListener(this);
    mWidget.setOnInsertGestureListener(this);
    mWidget.setOnJoinGestureListener(this);
    mWidget.setOnOverwriteGestureListener(this);
    mWidget.setOnSingleTapGestureListener(this);
    mWidget.setOnLongPressGestureListener(this);
    mWidget.setOnUserScrollBeginListener(this);
    mWidget.setOnUserScrollEndListener(this);
    mWidget.setOnUserScrollListener(this);

    mWidget.setBaselinePosition(getResources().getDimension(R.dimen.baseline_position));
    mWidget.setWritingAreaBackgroundResource(R.drawable.sltw_bg_pattern);
    mWidget.setScrollbarResource(R.drawable.sltw_scrollbar_xml);
    mWidget.setScrollbarMaskResource(R.drawable.sltw_scrollbar_mask);
    mWidget.setScrollbarBackgroundResource(R.drawable.sltw_scrollbar_background);
    mWidget.setLeftScrollArrowResource(R.drawable.sltw_arrowleft_xml);
    mWidget.setRightScrollArrowResource(R.drawable.sltw_arrowright_xml);
    mWidget.setCursorResource(R.drawable.sltw_text_cursor_holo_light);

    mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");
    mWidget.configure("en_US", "cur_text");

    mWidget.setText(mTextField.getText().toString());
    isCorrectionMode = 0;
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
    int index = mWidget.getCursorIndex();
    mWidget.replaceCharacters(index, index, ":-)");
  }

  public void onSpaceButtonClick(View v) {
    int index = mWidget.getCursorIndex();
    boolean replaced = mWidget.replaceCharacters(index, index, " ");
    if (replaced) {
      mWidget.setCursorIndex(index + 1);
      isCorrectionMode++;
    }
  }

  public void onDeleteButtonClick(View v) {
    int index = mWidget.getCursorIndex();
    CandidateInfo info = mWidget.getCharacterCandidates(mWidget.getCursorIndex() - 1);
    boolean replaced = mWidget.replaceCharacters(info.getStart(), info.getEnd(), null);
    if (replaced) {
      mWidget.setCursorIndex(index - (info.getEnd() - info.getStart()));
      isCorrectionMode++;
    }
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
    if (mWidget.getCursorIndex() != selEnd) {
      mWidget.setCursorIndex(selEnd);
      if (selEnd == mWidget.getText().length()) {
        mWidget.scrollTo(selEnd);
      } else {
        mWidget.centerTo(selEnd);
      }
      updateCandidateBar();
      updateCandidatePanel();
    }
  }

  //--------------------------------------------------------------------------------
  // Widget callbacks

  @Override
  public void onConfigured(SingleLineWidgetApi w, boolean success) {
    Log.d(TAG, "Widget configuration " + (success ? "done" : "failed"));
  }

  @Override
  public void onTextChanged(SingleLineWidgetApi w, String text, boolean intermediate) {
    Log.d(TAG, "Text changed to \"" + text + "\" (" + (intermediate ? "intermediate" : "stable") + ")");
    // temporarily disable selection changed listener to prevent spurious cursor jumps
    mTextField.setOnSelectionChangedListener(null);
    mTextField.setTextKeepState(text);
    if(isCorrectionMode == 0){
      mTextField.setSelection(text.length());
      mTextField.setOnSelectionChangedListener(this);
      mWidget.setCursorIndex(mTextField.length());
    }
    else{
      mTextField.setSelection(mWidget.getCursorIndex());
      mTextField.setOnSelectionChangedListener(this);
      isCorrectionMode--;
    }

    updateCandidateBar();
    updateCandidatePanel();
  }

  @Override
  public void onReturnGesture(SingleLineWidgetApi w, int index) {
    Log.d(TAG, "Return gesture detected at index " + index);
    mWidget.replaceCharacters(index, index, "\n");
  }

  @Override
  public void onSingleTapGesture(SingleLineWidgetApi w, int index) {
    Log.d(TAG, "Single tap gesture detected at index=" + index);
    mWidget.setCursorIndex(index);
    mTextField.setSelection(index);
    updateCandidateBar();
    updateCandidatePanel();
  }

  @Override
  public void onLongPressGesture(SingleLineWidgetApi w, int index) {
    Log.d(TAG, "Long press gesture detected at index=" + index);
    mWidget.setCursorIndex(index);
    isCorrectionMode++;
  }

  @Override
  public void onEraseGesture(SingleLineWidgetApi w, int start, int end) {
    Log.d(TAG, "Erase gesture detected for range [" + start + "-" + end + "]");
    mWidget.setCursorIndex(start);
    isCorrectionMode++;
  }

  @Override
  public void onSelectGesture(SingleLineWidgetApi w, int start, int end) {
    Log.d(TAG, "Select gesture detected for range [" + start + "-" + end + "]");
  }

  @Override
  public void onUnderlineGesture(SingleLineWidgetApi w, int start, int end) {
    Log.d(TAG, "Underline gesture detected for range [" + start + "-" + end + "]");
  }

  @Override
  public void onInsertGesture(SingleLineWidgetApi w, int index) {
    Log.d(TAG, "Insert gesture detected at index " + index);
    mWidget.replaceCharacters(index, index, " ");
    mWidget.setCursorIndex(index + 1);
    isCorrectionMode++;
  }

  @Override
  public void onJoinGesture(SingleLineWidgetApi w , int start, int end) {
    Log.d(TAG, "Join gesture detected for range [" + start + "-" + end + "]");
    mWidget.replaceCharacters(start, end, null);
    mWidget.setCursorIndex(start);
    isCorrectionMode++;
  }

  @Override
  public void onOverwriteGesture(SingleLineWidgetApi w, int start, int end) {
    Log.d(TAG, "Overwrite gesture detected for range [" + start + "-" + end + "]");
    mWidget.setCursorIndex(end);
    isCorrectionMode++;
  }

  @Override
  public void onUserScrollBegin(SingleLineWidgetApi w) {
    Log.d(TAG, "User scroll begin");
  }

  @Override
  public void onUserScrollEnd(SingleLineWidgetApi w) {
    Log.d(TAG, "User scroll end");
  }

  @Override
  public void onUserScroll(SingleLineWidgetApi w) {
    Log.d(TAG, "User scroll");
    if (mWidget.moveCursorToVisibleIndex()) {
      // temporarily disable selection changed listener
      mTextField.setOnSelectionChangedListener(null);
      mTextField.setSelection(mWidget.getCursorIndex());
      mTextField.setOnSelectionChangedListener(this);
      updateCandidateBar();
      updateCandidatePanel();
    }
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
    int index = mWidget.getCursorIndex() - 1;
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

    if (info.isEmpty()) {
      mWidget.clearSelection();
    } else {
      mWidget.selectWord(index);
    }
  }

  // update candidates panel
  private void updateCandidatePanel() {
    if (mCandidatePanel.getVisibility() == View.GONE) {
      return;
    }

    List<CandidateTag> candidates = new ArrayList<>();

    int index = mWidget.getCursorIndex() - 1;
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
