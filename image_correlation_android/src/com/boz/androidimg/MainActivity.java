package com.boz.androidimg;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.ibm.imgengine.DeepResult;
import com.ibm.imgengine.Engine;
import com.ibm.imgengine.Filter.FilterKind;
import com.ibm.imgengine.IDisplayCorrelation;
import com.ibm.imgengine.IImageFactory;

public class MainActivity extends Activity implements IDisplayCorrelation {

	private FilterKind filterKind = FilterKind.SOBEL;
	private TextView result;
	private String inputFolderName = "data";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// =================== radiogroup image filter =================================
		RadioGroup filter = (RadioGroup)findViewById(R.id.filter);
		filter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
					case R.id.none :
						filterKind = FilterKind.NONE;
						break;
					case R.id.grey :
						filterKind = FilterKind.GREY;
						break;
					case R.id.sodel :
						filterKind = FilterKind.SOBEL;
						break;
				}
			}
		});
		filter.check(R.id.sodel);
		// =================== radiogroup data select =================================
		RadioGroup dataSelect = (RadioGroup)findViewById(R.id.data);
		dataSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
					case R.id.number :
						inputFolderName = "data";
						break;
					case R.id.head :
						inputFolderName = "headicons";
						break;
				}
			}
		});
		dataSelect.check(R.id.number);
		// =================== start button =================================
		Button start = (Button)findViewById(R.id.button_start);
		start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				start();
			}
		});
		// =================== start button =================================
		Button clear = (Button)findViewById(R.id.clear);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clear();
			}
		});
		// =================== Text part =================================
		result = (TextView)findViewById(R.id.result);
	}

	private void start() {
		IImageFactory factory = new ImageBitmapFactory(this);
		new Engine(inputFolderName,filterKind,factory,this).run();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void clear() {
		result.setText("");
	}

	@Override
	public void displayText(String msg) {
		StringBuilder sb = new StringBuilder(result.getText());
		sb.append('\n');
		sb.append(msg);
		result.setText(sb.toString());
		result.invalidate();
	}

	@Override
	public void displayDeepResult(DeepResult result) {
		// TODO : code me
//		for (int img=0; img<result.numberOfImages; img++) {
//			for (int channel=0; channel<result.numberOfChannel; channel++) {
//				ArrayList<DeepResult.Entry> entry = result.sortedResult[img][channel];
//				if ((entry != null) && (entry.size() > 2)) {
//					DeepResult.Entry second = entry.get(1); // best == 0
//					displayText(result.imageNames[img]+" : 2nd : "+result.imageNames[second.imageIndex]+" ("+second.score+")");
//				}
//			}
//		}
	}
}
