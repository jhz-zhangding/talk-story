package com.efrobot.talkstory;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.efrobot.talkstory.utils.MyAnimatorUpdateListener;
import com.efrobot.talkstory.utils.RoundImageView;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_test);
		setFullScreen();

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private RoundImageView v;
		private boolean isFirst = true;
		private ObjectAnimator anim;
		private MyAnimatorUpdateListener listener;
		private int layoutId;

		@SuppressWarnings("deprecation")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			v = (RoundImageView) rootView.findViewById(R.id.iv);
			v.setOutsideColor(Color.BLUE);
			v.setInsideColor(Color.RED);
			v.setImageDrawable(getResources().getDrawable(R.mipmap.p11));
//			Bitmap bit = BitmapFactory.decodeResource(getResources(),
//					R.mipmap.p11);
//			Matrix matrix = new Matrix();
//			matrix.postScale(0.1f, 0.1f);
//			Bitmap overlay = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
//					bit.getHeight(), matrix, true);
//			Bitmap bitmap = FastBulr.doBlur(overlay, 5, true);
//
//			Drawable d = new BitmapDrawable(bitmap);
//			rootView.setBackgroundDrawable(d);
//			Button bt = (Button) rootView.findViewById(R.id.bt);
			LinearInterpolator lin = new LinearInterpolator();
			anim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
			anim.setDuration(15000);
			anim.setInterpolator(lin);
			anim.setRepeatMode(Animation.RESTART);
			anim.setRepeatCount(-1);
			listener = new MyAnimatorUpdateListener(anim);
			anim.addUpdateListener(listener);
			anim.start();

//			bt.setOnClickListener(new View.OnClickListener() {

//				@Override
//				public void onClick(View arg0) {


//					Button bt = (Button) arg0;
//
//					if (isFirst) {
//						anim.start();
//						bt.setText("pause");
//						isFirst = false;
//					} else {
//
//						if (listener.isPause()) {
//							listener.play();
//							bt.setText("pause");
//						} else if (listener.isPlay()) {
//							listener.pause();
//							bt.setText("start");
//						}
//					}
//				}
//			});

			return rootView;
		}
	}

	public void setFullScreen() {
		int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

		if (android.os.Build.VERSION.SDK_INT >= 19) {
			uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
		} else {
			uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
		}

		getWindow().getDecorView().setSystemUiVisibility(uiFlags);
	}




}
