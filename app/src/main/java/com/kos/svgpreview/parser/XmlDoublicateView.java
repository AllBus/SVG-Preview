package com.kos.svgpreview.parser;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Kos on 23.09.2016.
 */

public class XmlDoublicateView extends View {

	private XmlView.VectorDrawableCompatState mVectorState=null;
	//private XmlView source=null;


	public void setSource(XmlView source) {
		this.mVectorState = new XmlView.VectorDrawableCompatState(source.getVectorState());
		invalidate();
	}

	private Rect drawRect=new Rect(0,0,0,0);

	ColorFilter colorFilter=new ColorFilter();


	public XmlDoublicateView(Context context) {
		super(context);
	}

	public XmlDoublicateView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public XmlDoublicateView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public XmlDoublicateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mVectorState!=null){

			int scaledWidth=getWidth();
			int scaledHeight=getHeight();


			mVectorState.createCachedBitmapIfNeeded(scaledWidth, scaledHeight);
			if (!mVectorState.canReuseCache()) {
				mVectorState.updateCachedBitmap(scaledWidth, scaledHeight);
				mVectorState.updateCacheStates();
			}
			mVectorState.drawCachedBitmapWithRootAlpha(canvas, colorFilter, drawRect);
		}
	}

	public float getViewPortWidth(){
		return mVectorState.mVPathRenderer.mViewportWidth;
	}

	public float getViewPortHeight(){
		return mVectorState.mVPathRenderer.mViewportHeight;
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		resizeImage(w, h);
	}

	private void resizeImage(int w, int h) {
		float vW=getViewPortWidth();
		float vH=getViewPortHeight();

		float s=h*vW;
		float v=vH*w;

		if (v<s){
			//horizontal
			if (vW>0) {
				int sh=(int) (w*vH/vW);
				drawRect.set(0, (h-sh)/2, w, (h+sh)/2 );
			}
		}else{
			//vertical
			if (vH>0) {
				int sw=(int) (h*vW/vH);
				drawRect.set((w-sw)/2, 0,(w+sw)/2 , h);
			}
		}
	}
}
