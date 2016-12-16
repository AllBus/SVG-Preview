package com.kos.svgpreview.parser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.View;

import com.kos.svgpreview.parser.graphics.PathDataNode;
import com.kos.svgpreview.parser.graphics.PathParser;
import com.kos.svgpreview.parser.graphics.TypedArrayUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

/**
 * Created on 23.09.2016.
 *
 * @author Kos
 */

public class XmlView extends View {
	//static final String LOGTAG = "VectorDrawableCompat2";
	static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;
	private static final String SHAPE_CLIP_PATH = "clip-path";
	private static final String SHAPE_GROUP = "group";
	private static final String SHAPE_PATH = "path";
	private static final String SHAPE_VECTOR = "vector";


	private int mMaxWidth = Integer.MAX_VALUE;
	private int mMaxHeight = Integer.MAX_VALUE;


	private VectorDrawableCompatState mVectorState = new VectorDrawableCompatState();

	private Rect drawRect = new Rect(0, 0, 0, 0);
	float dp = 1;

	ColorFilter colorFilter = new ColorFilter();

	public XmlView(Context context) {
		super(context);
		init(context);
	}

	public XmlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public XmlView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}


	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public XmlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		init(context);
	}


	public boolean hasImage() {
		return getViewPortWidth() > 0 && getViewPortHeight() > 0;
	}


	private void init(Context context) {
		dp = context.getResources().getDisplayMetrics().density;
	}

	public VectorDrawableCompatState getVectorState() {
		return mVectorState;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);


		int w = (int) getViewPortWidth();
		int h = (int) getViewPortHeight();

		// Desired aspect ratio of the view's contents (not including padding)
		float desiredAspect = 0.0f;

		// We are allowed to change the view's width
		boolean resizeWidth = false;

		// We are allowed to change the view's height
		boolean resizeHeight = false;

		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);


		if (w <= 0) w = 1;
		if (h <= 0) h = 1;


		resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
		resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

		desiredAspect = (float) w / (float) h;


		final int pleft = getPaddingLeft();
		final int pright = getPaddingRight();
		final int ptop = getPaddingTop();
		final int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		if (resizeWidth || resizeHeight) {
			/* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension.
            */

			// Get the max possible width given our constraints
			widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec);

			// Get the max possible height given our constraints
			heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec);

			if (desiredAspect != 0.0f) {
				// See what our actual aspect ratio is
				final float actualAspect = (float) (widthSize - pleft - pright) /
						(heightSize - ptop - pbottom);

				if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

					boolean done = false;

					// Try adjusting width to be proportional to height
					if (resizeWidth) {
						int newWidth = (int) (desiredAspect * (heightSize - ptop - pbottom)) +
								pleft + pright;

						// Allow the width to outgrow its original estimate if height is fixed.
						if (!resizeHeight) {
							widthSize = resolveAdjustedSize(newWidth, mMaxWidth, widthMeasureSpec);
						}

						if (newWidth <= widthSize) {
							widthSize = newWidth;
							done = true;
						}
					}

					// Try adjusting height to be proportional to width
					if (!done && resizeHeight) {
						int newHeight = (int) ((widthSize - pleft - pright) / desiredAspect) +
								ptop + pbottom;

						// Allow the height to outgrow its original estimate if width is fixed.
						if (!resizeWidth) {
							heightSize = resolveAdjustedSize(newHeight, mMaxHeight,
									heightMeasureSpec);
						}

						if (newHeight <= heightSize) {
							heightSize = newHeight;
						}
					}
				}
			}
		} else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
			w += pleft + pright;
			h += ptop + pbottom;

			w = Math.max(w, getSuggestedMinimumWidth());
			h = Math.max(h, getSuggestedMinimumHeight());

			widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
			heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		int scaledWidth = getWidth();
		int scaledHeight = getHeight();

		if (scaledWidth <= 0 || scaledHeight <= 0)
			return;

		int mh=canvas.getMaximumBitmapHeight();
		int mw=canvas.getMaximumBitmapWidth();

		if (scaledWidth>mw || scaledHeight>mh){
			//todo: если картинка слишком большая мы её рисовать не будем.
			return;
		}

		mVectorState.createCachedBitmapIfNeeded(scaledWidth, scaledHeight);
		if (!mVectorState.canReuseCache()) {
			mVectorState.updateCachedBitmap(scaledWidth, scaledHeight);
			mVectorState.updateCacheStates();
		}
		mVectorState.drawCachedBitmapWithRootAlpha(canvas, colorFilter, drawRect);
	}

	public void fromVectorState(VectorDrawableCompatState vectorState) {
		mVectorState = new VectorDrawableCompatState(vectorState);
		requestLayout();
		resizeImage(getWidth(), getHeight());
		invalidate();
	}

	public static VectorDrawableCompatState parse(XmlPullParser parser) throws XmlPullParserException, IOException {

		final VectorDrawableCompatState state = new VectorDrawableCompatState();
		final VPathRenderer pathRenderer = new VPathRenderer();
		state.mVPathRenderer = pathRenderer;
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
			// Empty loop.
		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		if (SHAPE_VECTOR.equals(parser.getName())) {
			updateStateFromTypedArray(state, parser);
			inflateInternal(state, parser);
			//	resizeImage(getWidth(), getHeight());
		}
		//	invalidate();

		return state;
	}

	public float getViewPortWidth() {
		return mVectorState.mVPathRenderer.mViewportWidth * dp;
	}

	public float getViewPortHeight() {
		return mVectorState.mVPathRenderer.mViewportHeight * dp;
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		resizeImage(w, h);
	}

	private void resizeImage(int w, int h) {
		float vW = getViewPortWidth();
		float vH = getViewPortHeight();

		float s = h * vW;
		float v = vH * w;

		if (v < s) {
			//horizontal
			if (vW > 0) {
				int sh = (int) (w * vH / vW);
				drawRect.set(0, (h - sh) / 2, w, (h + sh) / 2);
			}
		} else {
			//vertical
			if (vH > 0) {
				int sw = (int) (h * vW / vH);
				drawRect.set((w - sw) / 2, 0, (w + sw) / 2, h);
			}
		}
	}

	//===================================

	static class VPathRenderer {
		/* Right now the internal data structure is organized as a tree.
		 * Each node can be a group node, or a path.
		 * A group node can have groups or paths as children, but a path node has
		 * no children.
		 * One example can be:
		 *                 Root Group
		 *                /    |     \
		 *           Group    Path    Group
		 *          /     \             |
		 *         Path   Path         Path
		 *
		 */
		// Variables that only used temporarily inside the draw() call, so there
		// is no need for deep copying.
		private final Path mPath;
		private final Path mRenderPath;
		private static final Matrix IDENTITY_MATRIX = new Matrix();
		private final Matrix mFinalPathMatrix = new Matrix();

		private Paint mStrokePaint;
		private Paint mFillPaint;
		private PathMeasure mPathMeasure;

		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		private int mChangingConfigurations;
		private final VGroup mRootGroup;
		float mBaseWidth = 0;
		float mBaseHeight = 0;
		float mViewportWidth = 0;
		float mViewportHeight = 0;
		int mRootAlpha = 0xFF;
		String mRootName = null;

		final ArrayMap<String, Object> mVGTargetsMap = new ArrayMap<String, Object>();

		public VPathRenderer() {
			mRootGroup = new VGroup();
			mPath = new Path();
			mRenderPath = new Path();
		}

		public void setRootAlpha(int alpha) {
			mRootAlpha = alpha;
		}

		public int getRootAlpha() {
			return mRootAlpha;
		}

		// setAlpha() and getAlpha() are used mostly for animation purpose, since
		// Animator like to use alpha from 0 to 1.
		public void setAlpha(float alpha) {
			setRootAlpha((int) (alpha * 255));
		}

		@SuppressWarnings("unused")
		public float getAlpha() {
			return getRootAlpha() / 255.0f;
		}

		public VPathRenderer(VPathRenderer copy) {
			mRootGroup = new VGroup(copy.mRootGroup, mVGTargetsMap);
			mPath = new Path(copy.mPath);
			mRenderPath = new Path(copy.mRenderPath);
			mBaseWidth = copy.mBaseWidth;
			mBaseHeight = copy.mBaseHeight;
			mViewportWidth = copy.mViewportWidth;
			mViewportHeight = copy.mViewportHeight;
			mChangingConfigurations = copy.mChangingConfigurations;
			mRootAlpha = copy.mRootAlpha;
			mRootName = copy.mRootName;
			if (copy.mRootName != null) {
				mVGTargetsMap.put(copy.mRootName, this);
			}
		}

		private void drawGroupTree(VGroup currentGroup, Matrix currentMatrix,
								   Canvas canvas, int w, int h, ColorFilter filter) {

			// Calculate current group's matrix by preConcat the parent's and
			// and the current one on the top of the stack.
			// Basically the Mfinal = Mviewport * M0 * M1 * M2;
			// Mi the local matrix at level i of the group tree.
			currentGroup.mStackedMatrix.set(currentMatrix);

			currentGroup.mStackedMatrix.preConcat(currentGroup.mLocalMatrix);

			// Save the current clip information, which is local to this group.
			canvas.save();

			// Draw the group tree in the same order as the XML file.
			for (int i = 0; i < currentGroup.mChildren.size(); i++) {
				Object child = currentGroup.mChildren.get(i);

				if (child instanceof VGroup) {
					VGroup childGroup = (VGroup) child;
					drawGroupTree(childGroup, currentGroup.mStackedMatrix,
							canvas, w, h, filter);
				} else if (child instanceof VPath) {
					VPath childPath = (VPath) child;

					drawPath(currentGroup, childPath, canvas, w, h, filter);
				}
			}

			canvas.restore();
		}

		public void draw(Canvas canvas, int w, int h, ColorFilter filter) {
			// Traverse the tree in pre-order to draw.
			drawGroupTree(mRootGroup, IDENTITY_MATRIX, canvas, w, h, filter);
		}

		private void drawPath(VGroup vGroup, VPath vPath, Canvas canvas, int w, int h,
							  ColorFilter filter) {
			final float scaleX = w / mViewportWidth;
			final float scaleY = h / mViewportHeight;
			final float minScale = Math.min(scaleX, scaleY);
			final Matrix groupStackedMatrix = vGroup.mStackedMatrix;

			mFinalPathMatrix.set(groupStackedMatrix);
			mFinalPathMatrix.postScale(scaleX, scaleY);


			final float matrixScale = getMatrixScale(groupStackedMatrix);
			if (matrixScale == 0) {
				// When either x or y is scaled to 0, we don't need to draw anything.
				return;
			}
			vPath.toPath(mPath);
			final Path path = mPath;

			mRenderPath.reset();

			if (vPath.isClipPath()) {
				mRenderPath.addPath(path, mFinalPathMatrix);
				canvas.clipPath(mRenderPath);
			} else {

				VFullPath fullPath = (VFullPath) vPath;
				if (fullPath.mTrimPathStart != 0.0f || fullPath.mTrimPathEnd != 1.0f) {
					float start = (fullPath.mTrimPathStart + fullPath.mTrimPathOffset) % 1.0f;
					float end = (fullPath.mTrimPathEnd + fullPath.mTrimPathOffset) % 1.0f;

					if (mPathMeasure == null) {
						mPathMeasure = new PathMeasure();
					}
					mPathMeasure.setPath(mPath, false);

					float len = mPathMeasure.getLength();
					start = start * len;
					end = end * len;
					path.reset();
					if (start > end) {
						mPathMeasure.getSegment(start, len, path, true);
						mPathMeasure.getSegment(0f, end, path, true);
					} else {
						mPathMeasure.getSegment(start, end, path, true);
					}
					path.rLineTo(0, 0); // fix bug in measure
				}
				mRenderPath.addPath(path, mFinalPathMatrix);

				if (fullPath.mFillColor != Color.TRANSPARENT) {
					if (mFillPaint == null) {
						mFillPaint = new Paint();
						mFillPaint.setStyle(Paint.Style.FILL);
						mFillPaint.setAntiAlias(true);
					}

					final Paint fillPaint = mFillPaint;
					fillPaint.setColor(applyAlpha(fullPath.mFillColor, fullPath.mFillAlpha));
					fillPaint.setColorFilter(filter);
					canvas.drawPath(mRenderPath, fillPaint);
				}

				if (fullPath.mStrokeColor != Color.TRANSPARENT) {
					if (mStrokePaint == null) {
						mStrokePaint = new Paint();
						mStrokePaint.setStyle(Paint.Style.STROKE);
						mStrokePaint.setAntiAlias(true);
					}

					final Paint strokePaint = mStrokePaint;
					if (fullPath.mStrokeLineJoin != null) {
						strokePaint.setStrokeJoin(fullPath.mStrokeLineJoin);
					}

					if (fullPath.mStrokeLineCap != null) {
						strokePaint.setStrokeCap(fullPath.mStrokeLineCap);
					}

					strokePaint.setStrokeMiter(fullPath.mStrokeMiterlimit);
					strokePaint.setColor(applyAlpha(fullPath.mStrokeColor, fullPath.mStrokeAlpha));
					strokePaint.setColorFilter(filter);
					final float finalStrokeScale = minScale * matrixScale;
					strokePaint.setStrokeWidth(fullPath.mStrokeWidth * finalStrokeScale);
					canvas.drawPath(mRenderPath, strokePaint);
				}
			}
		}

		private static float cross(float v1x, float v1y, float v2x, float v2y) {
			return v1x * v2y - v1y * v2x;
		}

		private float getMatrixScale(Matrix groupStackedMatrix) {
			// Given unit vectors A = (0, 1) and B = (1, 0).
			// After matrix mapping, we got A' and B'. Let theta = the angel b/t A' and B'.
			// Therefore, the final scale we want is min(|A'| * sin(theta), |B'| * sin(theta)),
			// which is (|A'| * |B'| * sin(theta)) / max (|A'|, |B'|);
			// If  max (|A'|, |B'|) = 0, that means either x or y has a scale of 0.
			//
			// For non-skew case, which is most of the cases, matrix scale is computing exactly the
			// scale on x and y axis, and take the minimal of these two.
			// For skew case, an unit square will mapped to a parallelogram. And this function will
			// return the minimal height of the 2 bases.
			float[] unitVectors = new float[]{0, 1, 1, 0};
			groupStackedMatrix.mapVectors(unitVectors);
			float scaleX = (float) Math.hypot(unitVectors[0], unitVectors[1]);
			float scaleY = (float) Math.hypot(unitVectors[2], unitVectors[3]);
			float crossProduct = cross(unitVectors[0], unitVectors[1], unitVectors[2],
					unitVectors[3]);
			float maxScale = Math.max(scaleX, scaleY);

			float matrixScale = 0;
			if (maxScale > 0) {
				matrixScale = Math.abs(crossProduct) / maxScale;
			}
//			if (DBG_VECTOR_DRAWABLE) {
//				Log.d(LOGTAG, "Scale x " + scaleX + " y " + scaleY + " final " + matrixScale);
//			}
			return matrixScale;
		}
	}

	private static class VGroup {
		// mStackedMatrix is only used temporarily when drawing, it combines all
		// the parents' local matrices with the current one.
		private final Matrix mStackedMatrix = new Matrix();

		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		final ArrayList<Object> mChildren = new ArrayList<>();

		private float mRotate = 0;
		private float mPivotX = 0;
		private float mPivotY = 0;
		private float mScaleX = 1;
		private float mScaleY = 1;
		private float mTranslateX = 0;
		private float mTranslateY = 0;

		// mLocalMatrix is updated based on the update of transformation information,
		// either parsed from the XML or by animation.
		private final Matrix mLocalMatrix = new Matrix();
		private int mChangingConfigurations;
		private int[] mThemeAttrs;
		private String mGroupName = null;


		public VGroup(VGroup copy, ArrayMap<String, Object> targetsMap) {
			mRotate = copy.mRotate;
			mPivotX = copy.mPivotX;
			mPivotY = copy.mPivotY;
			mScaleX = copy.mScaleX;
			mScaleY = copy.mScaleY;
			mTranslateX = copy.mTranslateX;
			mTranslateY = copy.mTranslateY;
			mThemeAttrs = copy.mThemeAttrs;
			mGroupName = copy.mGroupName;
			mChangingConfigurations = copy.mChangingConfigurations;
			if (mGroupName != null) {
				targetsMap.put(mGroupName, this);
			}

			mLocalMatrix.set(copy.mLocalMatrix);

			final ArrayList<Object> children = copy.mChildren;
			for (int i = 0; i < children.size(); i++) {
				Object copyChild = children.get(i);
				if (copyChild instanceof VGroup) {
					VGroup copyGroup = (VGroup) copyChild;
					mChildren.add(new VGroup(copyGroup, targetsMap));
				} else {
					VPath newPath = null;
					if (copyChild instanceof VFullPath) {
						newPath = new VFullPath((VFullPath) copyChild);
					} else if (copyChild instanceof VClipPath) {
						newPath = new VClipPath((VClipPath) copyChild);
					} else {
						throw new IllegalStateException("Unknown object in the tree!");
					}
					mChildren.add(newPath);
					if (newPath.mPathName != null) {
						targetsMap.put(newPath.mPathName, newPath);
					}
				}
			}
		}

		public VGroup() {
		}

		public String getGroupName() {
			return mGroupName;
		}

		public Matrix getLocalMatrix() {
			return mLocalMatrix;
		}

		public void inflate(XmlPullParser parser) {
			updateStateFromTypedArray(parser);

		}

		@SuppressWarnings("ResourceType")
		private void updateStateFromTypedArray(XmlPullParser parser) {
			// Account for any configuration changes.
			// mChangingConfigurations |= Utils.getChangingConfigurations(a);

			// Extract the theme attributes, if any.
			mThemeAttrs = null; // TODO TINT THEME Not supported yet a.extractThemeAttrs();

			// This is added in API 11
			mRotate = TypedArrayUtils.getNamedFloat(parser, "rotation", mRotate);

			mPivotX = TypedArrayUtils.getNamedFloat(parser, "pivotX", mPivotX);
			mPivotY = TypedArrayUtils.getNamedFloat(parser, "pivotY", mPivotY);

			// This is added in API 11
			mScaleX = TypedArrayUtils.getNamedFloat(parser, "scaleX", mScaleX);

			// This is added in API 11
			mScaleY = TypedArrayUtils.getNamedFloat(parser, "scaleY", mScaleY);

			mTranslateX = TypedArrayUtils.getNamedFloat(parser, "translateX", mTranslateX);
			mTranslateY = TypedArrayUtils.getNamedFloat(parser, "translateY", mTranslateY);

			final String groupName = TypedArrayUtils.getNamedString(parser, "name", "");

			if (groupName != null) {
				mGroupName = groupName;
			}

			updateLocalMatrix();
		}

		private void updateLocalMatrix() {
			// The order we apply is the same as the
			// RenderNode.cpp::applyViewPropertyTransforms().
			mLocalMatrix.reset();
			mLocalMatrix.postTranslate(-mPivotX, -mPivotY);
			mLocalMatrix.postScale(mScaleX, mScaleY);
			mLocalMatrix.postRotate(mRotate, 0, 0);
			mLocalMatrix.postTranslate(mTranslateX + mPivotX, mTranslateY + mPivotY);
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		public float getRotation() {
			return mRotate;
		}

		@SuppressWarnings("unused")
		public void setRotation(float rotation) {
			if (rotation != mRotate) {
				mRotate = rotation;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getPivotX() {
			return mPivotX;
		}

		@SuppressWarnings("unused")
		public void setPivotX(float pivotX) {
			if (pivotX != mPivotX) {
				mPivotX = pivotX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getPivotY() {
			return mPivotY;
		}

		@SuppressWarnings("unused")
		public void setPivotY(float pivotY) {
			if (pivotY != mPivotY) {
				mPivotY = pivotY;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getScaleX() {
			return mScaleX;
		}

		@SuppressWarnings("unused")
		public void setScaleX(float scaleX) {
			if (scaleX != mScaleX) {
				mScaleX = scaleX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getScaleY() {
			return mScaleY;
		}

		@SuppressWarnings("unused")
		public void setScaleY(float scaleY) {
			if (scaleY != mScaleY) {
				mScaleY = scaleY;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getTranslateX() {
			return mTranslateX;
		}

		@SuppressWarnings("unused")
		public void setTranslateX(float translateX) {
			if (translateX != mTranslateX) {
				mTranslateX = translateX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getTranslateY() {
			return mTranslateY;
		}

		@SuppressWarnings("unused")
		public void setTranslateY(float translateY) {
			if (translateY != mTranslateY) {
				mTranslateY = translateY;
				updateLocalMatrix();
			}
		}
	}

	/**
	 * Common Path information for clip path and normal path.
	 */
	private static class VPath {
		protected PathDataNode[] mNodes = null;
		String mPathName;
		int mChangingConfigurations;


		public VPath() {
			// Empty constructor.
		}

		public void printVPath(int level) {
			String indent = "";
			for (int i = 0; i < level; i++) {
				indent += "    ";
			}
//			Log.v(LOGTAG, indent + "current path is :" + mPathName +
//					" pathData is " + NodesToString(mNodes));

		}

		public String NodesToString(PathDataNode[] nodes) {
			String result = " ";
			for (int i = 0; i < nodes.length; i++) {
				result += nodes[i].type + ":";
				float[] params = nodes[i].params;
				for (int j = 0; j < params.length; j++) {
					result += params[j] + ",";
				}
			}
			return result;
		}

		public VPath(VPath copy) {
			mPathName = copy.mPathName;
			mChangingConfigurations = copy.mChangingConfigurations;
			mNodes = PathParser.deepCopyNodes(copy.mNodes);
		}

		public void toPath(Path path) {
			path.reset();
			if (mNodes != null) {
				PathDataNode.nodesToPath(mNodes, path);
			}
		}

		public String getPathName() {
			return mPathName;
		}

		public boolean canApplyTheme() {
			return false;
		}

		public void applyTheme(Resources.Theme t) {
		}

		public boolean isClipPath() {
			return false;
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		public PathDataNode[] getPathData() {
			return mNodes;
		}

		@SuppressWarnings("unused")
		public void setPathData(PathDataNode[] nodes) {
			if (!PathParser.canMorph(mNodes, nodes)) {
				// This should not happen in the middle of animation.
				mNodes = PathParser.deepCopyNodes(nodes);
			} else {
				PathParser.updateNodes(mNodes, nodes);
			}
		}
	}

	/**
	 * Clip path, which only has name and pathData.
	 */
	@SuppressWarnings("ResourceType")
	private static class VClipPath extends VPath {
		public VClipPath() {
			// Empty constructor.
		}

		public VClipPath(VClipPath copy) {
			super(copy);
		}

		public void inflate(XmlPullParser parser) {
			// TODO TINT THEME Not supported yet
			final boolean hasPathData = TypedArrayUtils.hasAttribute(parser, "pathData");

			if (!hasPathData) {
				return;
			}

			final String pathName = TypedArrayUtils.getNamedString(parser, "name", null);
			if (pathName != null) {
				mPathName = pathName;
			}


			final String pathData = TypedArrayUtils.getNamedString(parser, "pathData", null);

			if (pathData != null) {
				mNodes = PathParser.createNodesFromPathData(pathData);
			}
		}


		@Override
		public boolean isClipPath() {
			return true;
		}
	}

	/**
	 * Normal path, which contains all the fill / paint information.
	 */
	private static class VFullPath extends VPath {
		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		private int[] mThemeAttrs;

		int mStrokeColor = Color.TRANSPARENT;
		float mStrokeWidth = 0;

		int mFillColor = Color.TRANSPARENT;
		float mStrokeAlpha = 1.0f;
		int mFillRule;
		float mFillAlpha = 1.0f;
		float mTrimPathStart = 0;
		float mTrimPathEnd = 1;
		float mTrimPathOffset = 0;

		Paint.Cap mStrokeLineCap = Paint.Cap.BUTT;
		Paint.Join mStrokeLineJoin = Paint.Join.MITER;
		float mStrokeMiterlimit = 4;

		public VFullPath() {
			// Empty constructor.
		}

		public VFullPath(VFullPath copy) {
			super(copy);
			mThemeAttrs = copy.mThemeAttrs;

			mStrokeColor = copy.mStrokeColor;
			mStrokeWidth = copy.mStrokeWidth;
			mStrokeAlpha = copy.mStrokeAlpha;
			mFillColor = copy.mFillColor;
			mFillRule = copy.mFillRule;
			mFillAlpha = copy.mFillAlpha;
			mTrimPathStart = copy.mTrimPathStart;
			mTrimPathEnd = copy.mTrimPathEnd;
			mTrimPathOffset = copy.mTrimPathOffset;

			mStrokeLineCap = copy.mStrokeLineCap;
			mStrokeLineJoin = copy.mStrokeLineJoin;
			mStrokeMiterlimit = copy.mStrokeMiterlimit;
		}

		private Paint.Cap getStrokeLineCap(int id, Paint.Cap defValue) {
			switch (id) {
				case TypedArrayUtils.LINECAP_BUTT:
					return Paint.Cap.BUTT;
				case TypedArrayUtils.LINECAP_ROUND:
					return Paint.Cap.ROUND;
				case TypedArrayUtils.LINECAP_SQUARE:
					return Paint.Cap.SQUARE;
				default:
					return defValue;
			}
		}

		private Paint.Join getStrokeLineJoin(int id, Paint.Join defValue) {
			switch (id) {
				case TypedArrayUtils.LINEJOIN_MITER:
					return Paint.Join.MITER;
				case TypedArrayUtils.LINEJOIN_ROUND:
					return Paint.Join.ROUND;
				case TypedArrayUtils.LINEJOIN_BEVEL:
					return Paint.Join.BEVEL;
				default:
					return defValue;
			}
		}

		@Override
		public boolean canApplyTheme() {
			return mThemeAttrs != null;
		}

		public void inflate(XmlPullParser parser) {

			updateStateFromTypedArray(parser);

		}

		@SuppressWarnings("ResourceType")
		private void updateStateFromTypedArray(XmlPullParser parser) {
			// Account for any configuration changes.
			// mChangingConfigurations |= Utils.getChangingConfigurations(a);

			// Extract the theme attributes, if any.
			mThemeAttrs = null; // TODO TINT THEME Not supported yet a.extractThemeAttrs();

			// In order to work around the conflicting id issue, we need to double check the
			// existence of the attribute.
			// B/c if the attribute existed in the compiled XML, then calling TypedArray will be
			// safe since the framework will look up in the XML first.
			// Note that each getAttributeValue take roughly 0.03ms, it is a price we have to pay.
			final boolean hasPathData = TypedArrayUtils.hasAttribute(parser, "pathData");
			if (!hasPathData) {
				// If there is no pathData in the <path> tag, then this is an empty path,
				// nothing need to be drawn.
				return;
			}

			final String pathName = TypedArrayUtils.getNamedString(parser, "name", null);
			if (pathName != null) {
				mPathName = pathName;
			}
			final String pathData = TypedArrayUtils.getNamedString(parser, "pathData", null);

			if (pathData != null) {
				mNodes = PathParser.createNodesFromPathData(pathData);
			}

			mFillColor = TypedArrayUtils.getNamedColor(parser, "fillColor", mFillColor);
			mFillAlpha = TypedArrayUtils.getNamedFloat(parser, "fillAlpha", mFillAlpha);
			final int lineCap = TypedArrayUtils.getNamedLineCap(parser, "strokeLineCap", -1);
			mStrokeLineCap = getStrokeLineCap(lineCap, mStrokeLineCap);
			final int lineJoin = TypedArrayUtils.getNamedLineJoin(parser, "strokeLineJoin", -1);
			mStrokeLineJoin = getStrokeLineJoin(lineJoin, mStrokeLineJoin);
			mStrokeMiterlimit = TypedArrayUtils.getNamedFloat(parser, "strokeMiterLimit", mStrokeMiterlimit);
			mStrokeColor = TypedArrayUtils.getNamedColor(parser, "strokeColor", mStrokeColor);
			mStrokeAlpha = TypedArrayUtils.getNamedFloat(parser, "strokeAlpha", mStrokeAlpha);
			mStrokeWidth = TypedArrayUtils.getNamedFloat(parser, "strokeWidth", mStrokeWidth);
			mTrimPathEnd = TypedArrayUtils.getNamedFloat(parser, "trimPathEnd", mTrimPathEnd);
			mTrimPathOffset = TypedArrayUtils.getNamedFloat(parser, "trimPathOffset", mTrimPathOffset);
			mTrimPathStart = TypedArrayUtils.getNamedFloat(parser, "trimPathStart", mTrimPathStart);
		}

		@Override
		public void applyTheme(Resources.Theme t) {
			if (mThemeAttrs == null) {
				return;
			}

            /*
             * TODO TINT THEME Not supported yet final TypedArray a =
             * t.resolveAttributes(mThemeAttrs, styleable_VectorDrawablePath);
             * updateStateFromTypedArray(a); a.recycle();
             */
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		int getStrokeColor() {
			return mStrokeColor;
		}

		@SuppressWarnings("unused")
		void setStrokeColor(int strokeColor) {
			mStrokeColor = strokeColor;
		}

		@SuppressWarnings("unused")
		float getStrokeWidth() {
			return mStrokeWidth;
		}

		@SuppressWarnings("unused")
		void setStrokeWidth(float strokeWidth) {
			mStrokeWidth = strokeWidth;
		}

		@SuppressWarnings("unused")
		float getStrokeAlpha() {
			return mStrokeAlpha;
		}

		@SuppressWarnings("unused")
		void setStrokeAlpha(float strokeAlpha) {
			mStrokeAlpha = strokeAlpha;
		}

		@SuppressWarnings("unused")
		int getFillColor() {
			return mFillColor;
		}

		@SuppressWarnings("unused")
		void setFillColor(int fillColor) {
			mFillColor = fillColor;
		}

		@SuppressWarnings("unused")
		float getFillAlpha() {
			return mFillAlpha;
		}

		@SuppressWarnings("unused")
		void setFillAlpha(float fillAlpha) {
			mFillAlpha = fillAlpha;
		}

		@SuppressWarnings("unused")
		float getTrimPathStart() {
			return mTrimPathStart;
		}

		@SuppressWarnings("unused")
		void setTrimPathStart(float trimPathStart) {
			mTrimPathStart = trimPathStart;
		}

		@SuppressWarnings("unused")
		float getTrimPathEnd() {
			return mTrimPathEnd;
		}

		@SuppressWarnings("unused")
		void setTrimPathEnd(float trimPathEnd) {
			mTrimPathEnd = trimPathEnd;
		}

		@SuppressWarnings("unused")
		float getTrimPathOffset() {
			return mTrimPathOffset;
		}

		@SuppressWarnings("unused")
		void setTrimPathOffset(float trimPathOffset) {
			mTrimPathOffset = trimPathOffset;
		}
	}

	public static class VectorDrawableCompatState {
		int mChangingConfigurations;
		VPathRenderer mVPathRenderer;
		ColorStateList mTint = null;
		PorterDuff.Mode mTintMode = DEFAULT_TINT_MODE;
		boolean mAutoMirrored;

		Bitmap mCachedBitmap;
		int[] mCachedThemeAttrs;
		ColorStateList mCachedTint;
		PorterDuff.Mode mCachedTintMode;
		int mCachedRootAlpha;
		boolean mCachedAutoMirrored;
		boolean mCacheDirty;


		/**
		 * Temporary paint object used to draw cached bitmaps.
		 */
		Paint mTempPaint;

		// Deep copy for mutate() or implicitly mutate.
		public VectorDrawableCompatState(VectorDrawableCompatState copy) {
			if (copy != null) {
				mChangingConfigurations = copy.mChangingConfigurations;
				mVPathRenderer = new VPathRenderer(copy.mVPathRenderer);
				if (copy.mVPathRenderer.mFillPaint != null) {
					mVPathRenderer.mFillPaint = new Paint(copy.mVPathRenderer.mFillPaint);
				}
				if (copy.mVPathRenderer.mStrokePaint != null) {
					mVPathRenderer.mStrokePaint = new Paint(copy.mVPathRenderer.mStrokePaint);
				}
				mTint = copy.mTint;
				mTintMode = copy.mTintMode;
				mAutoMirrored = copy.mAutoMirrored;
			}

		}

		public void drawCachedBitmapWithRootAlpha(Canvas canvas, ColorFilter filter,
												  Rect originalBounds) {
			// The bitmap's size is the same as the bounds.
			if (mCachedBitmap != null) {
				final Paint p = getPaint(filter);
				canvas.drawBitmap(mCachedBitmap, null, originalBounds, p);

			}
		}

		public boolean hasTranslucentRoot() {
			return mVPathRenderer.getRootAlpha() < 255;
		}

		public boolean hasImage() {
			return mVPathRenderer.mViewportWidth > 0 && mVPathRenderer.mViewportHeight > 0;
		}

		/**
		 * @return null when there is no need for alpha paint.
		 */
		public Paint getPaint(ColorFilter filter) {
			if (!hasTranslucentRoot() && filter == null) {
				return null;
			}

			if (mTempPaint == null) {
				mTempPaint = new Paint();
				mTempPaint.setFilterBitmap(true);
			}
			mTempPaint.setAlpha(mVPathRenderer.getRootAlpha());
			mTempPaint.setColorFilter(filter);
			return mTempPaint;
		}

		public void updateCachedBitmap(int width, int height) {
			if (mCachedBitmap != null) {
				mCachedBitmap.eraseColor(Color.TRANSPARENT);
				Canvas tmpCanvas = new Canvas(mCachedBitmap);
				mVPathRenderer.draw(tmpCanvas, width, height, null);

			}
		}

		public boolean createCachedBitmapIfNeeded(int width, int height) {
			try {
				if (mCachedBitmap == null || !canReuseBitmap(width, height)) {
					mCachedBitmap = Bitmap.createBitmap(width, height,
							Bitmap.Config.ARGB_8888);
					mCacheDirty = true;
					return true;
				} else {
					return false;
				}
			} catch (OutOfMemoryError e) {
				System.gc();
				return false;
			} catch (Throwable e) {
				return false;
			}
		}

		public boolean canReuseBitmap(int width, int height) {
			if (mCachedBitmap != null) {
				if (width == mCachedBitmap.getWidth()
						&& height == mCachedBitmap.getHeight()) {
					return true;
				}
			}
			return false;
		}

		public boolean canReuseCache() {
			if (!mCacheDirty
					&& mCachedTint == mTint
					&& mCachedTintMode == mTintMode
					&& mCachedAutoMirrored == mAutoMirrored
					&& mCachedRootAlpha == mVPathRenderer.getRootAlpha()) {
				return true;
			}
			return false;
		}

		public void updateCacheStates() {
			// Use shallow copy here and shallow comparison in canReuseCache(),
			// likely hit cache miss more, but practically not much difference.
			mCachedTint = mTint;
			mCachedTintMode = mTintMode;
			mCachedRootAlpha = mVPathRenderer.getRootAlpha();
			mCachedAutoMirrored = mAutoMirrored;
			mCacheDirty = false;
		}

		public VectorDrawableCompatState() {
			mVPathRenderer = new VPathRenderer();
		}

		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}
	}

	//============================================


	//=============================

	private static int applyAlpha(int color, float alpha) {
		int alphaBytes = Color.alpha(color);
		color &= 0x00FFFFFF;
		color |= ((int) (alphaBytes * alpha)) << 24;
		return color;
	}

	private static PorterDuff.Mode parseTintModeCompat(int value, PorterDuff.Mode defaultMode) {
		switch (value) {
			case 3:
				return PorterDuff.Mode.SRC_OVER;
			case 5:
				return PorterDuff.Mode.SRC_IN;
			case 9:
				return PorterDuff.Mode.SRC_ATOP;
			case 14:
				return PorterDuff.Mode.MULTIPLY;
			case 15:
				return PorterDuff.Mode.SCREEN;
			case 16:
				return PorterDuff.Mode.ADD;
			default:
				return defaultMode;
		}
	}

	private static void updateStateFromTypedArray(VectorDrawableCompatState state, XmlPullParser parser)
			throws XmlPullParserException {

		final VPathRenderer pathRenderer = state.mVPathRenderer;

		// Account for any configuration changes.
		// state.mChangingConfigurations |= Utils.getChangingConfigurations(a);

		final int mode = TypedArrayUtils.getNamedInt(parser, "tintMode", -1);
		state.mTintMode = parseTintModeCompat(mode, PorterDuff.Mode.SRC_IN);

		final ColorStateList tint = null;

		if (tint != null) {
			state.mTint = tint;
		}

		state.mAutoMirrored = TypedArrayUtils.getNamedBoolean(parser, "autoMirrored", state.mAutoMirrored);

		pathRenderer.mViewportWidth = TypedArrayUtils.getNamedFloat(parser, "viewportWidth",
				pathRenderer.mViewportWidth);

		pathRenderer.mViewportHeight = TypedArrayUtils.getNamedFloat(parser, "viewportHeight",
				pathRenderer.mViewportHeight);

		if (pathRenderer.mViewportWidth <= 0) {
			pathRenderer.mViewportWidth = 24;
//            throw new XmlPullParserException(a.getPositionDescription() +
//                    "<vector> tag requires viewportWidth > 0");
		}
		if (pathRenderer.mViewportHeight <= 0) {
			pathRenderer.mViewportHeight = 24;
//            throw new XmlPullParserException(a.getPositionDescription() +
//                    "<vector> tag requires viewportHeight > 0");
		}

		pathRenderer.mBaseWidth = 24;//todo:a.getDimension(
		//AndroidResources.styleable_VectorDrawable_width, pathRenderer.mBaseWidth);
		pathRenderer.mBaseHeight = 24;//todo:a.getDimension(
		//AndroidResources.styleable_VectorDrawable_height, pathRenderer.mBaseHeight);
		if (pathRenderer.mBaseWidth <= 0) {
			pathRenderer.mBaseWidth = 24;
//            throw new XmlPullParserException(a.getPositionDescription() +
//                    "<vector> tag requires width > 0");
		}
		if (pathRenderer.mBaseHeight <= 0) {
			pathRenderer.mBaseHeight = 24;
//            throw new XmlPullParserException(a.getPositionDescription() +
//                    "<vector> tag requires height > 0");
		}

		// shown up from API 11.
		final float alphaInFloat = TypedArrayUtils.getNamedFloat(parser, "alpha", pathRenderer.getAlpha());

		pathRenderer.setAlpha(alphaInFloat);

		final String name = TypedArrayUtils.getNamedString(parser, "name", null);
		if (name != null) {
			pathRenderer.mRootName = name;
			pathRenderer.mVGTargetsMap.put(name, pathRenderer);
		}
	}

	private static void inflateInternal(VectorDrawableCompatState state, XmlPullParser parser) throws XmlPullParserException, IOException {
		final VPathRenderer pathRenderer = state.mVPathRenderer;
		boolean noPathTag = true;

		// Use a stack to help to build the group tree.
		// The top of the stack is always the current group.
		final Stack<VGroup> groupStack = new Stack<VGroup>();
		groupStack.push(pathRenderer.mRootGroup);

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			//	Log.d(LOGTAG,""+parser.getEventType());
			if (eventType == XmlPullParser.START_TAG) {
				final String tagName = parser.getName();
				final VGroup currentGroup = groupStack.peek();
				//	Log.d(LOGTAG,""+tagName+" "+currentGroup.mGroupName);
				if (SHAPE_PATH.equals(tagName)) {

					final VFullPath path = new VFullPath();
					path.inflate(parser);
					currentGroup.mChildren.add(path);
					if (path.getPathName() != null) {
						//			Log.d(LOGTAG,"path: "+tagName);
						pathRenderer.mVGTargetsMap.put(path.getPathName(), path);
					}
					noPathTag = false;
					state.mChangingConfigurations |= path.mChangingConfigurations;
				} else if (SHAPE_CLIP_PATH.equals(tagName)) {
					final VClipPath path = new VClipPath();
					path.inflate(parser);
					currentGroup.mChildren.add(path);
					if (path.getPathName() != null) {
						//		Log.d(LOGTAG,"path: "+tagName);
						pathRenderer.mVGTargetsMap.put(path.getPathName(), path);
					}
					state.mChangingConfigurations |= path.mChangingConfigurations;
				} else if (SHAPE_GROUP.equals(tagName)) {
					VGroup newChildGroup = new VGroup();
					newChildGroup.inflate(parser);
					currentGroup.mChildren.add(newChildGroup);
					groupStack.push(newChildGroup);
					if (newChildGroup.getGroupName() != null) {
						//		Log.d(LOGTAG,"path: "+tagName);
						pathRenderer.mVGTargetsMap.put(newChildGroup.getGroupName(),
								newChildGroup);
					}
					state.mChangingConfigurations |= newChildGroup.mChangingConfigurations;
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				final String tagName = parser.getName();
				if (SHAPE_GROUP.equals(tagName)) {
					groupStack.pop();
				}
			}
			eventType = parser.next();
		}

		// Print the tree out for debug.
		//	printGroupTree(pathRenderer.mRootGroup, 0);


		if (noPathTag) {
			final StringBuffer tag = new StringBuffer();

			if (tag.length() > 0) {
				tag.append(" or ");
			}
			tag.append(SHAPE_PATH);

			throw new XmlPullParserException("no " + tag + " defined");
		}
	}

	public void printGroupTree(VGroup currentGroup, int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "    ";
		}
		// Print the current node
//		Log.v(LOGTAG, indent + "current group is :" + currentGroup.getGroupName()
//				+ " rotation is " + currentGroup.mRotate);
//		Log.v(LOGTAG, indent + "matrix is :" + currentGroup.getLocalMatrix().toString());
		// Then print all the children groups
		for (int i = 0; i < currentGroup.mChildren.size(); i++) {
			Object child = currentGroup.mChildren.get(i);
			if (child instanceof VGroup) {
				printGroupTree((VGroup) child, level + 1);
			} else {
				((VPath) child).printVPath(level + 1);
			}
		}
	}

	/**
	 * Utility to reconcile a desired size and state, with constraints imposed
	 * by a MeasureSpec. Will take the desired size, unless a different size
	 * is imposed by the constraints. The returned value is a compound integer,
	 * with the resolved size in the {@link #MEASURED_SIZE_MASK} bits and
	 * optionally the bit {@link #MEASURED_STATE_TOO_SMALL} set if the
	 * resulting size is smaller than the size the view wants to be.
	 *
	 * @param size               How big the view wants to be.
	 * @param measureSpec        Constraints imposed by the parent.
	 * @param childMeasuredState Size information bit mask for the view's
	 *                           children.
	 * @return Size information bit mask as defined by
	 * {@link #MEASURED_SIZE_MASK} and
	 * {@link #MEASURED_STATE_TOO_SMALL}.
	 */
	public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);
		final int result;
		switch (specMode) {
			case MeasureSpec.AT_MOST:
				if (specSize < size) {
					result = specSize | MEASURED_STATE_TOO_SMALL;
				} else {
					result = size;
				}
				break;
			case MeasureSpec.EXACTLY:
				result = specSize;
				break;
			case MeasureSpec.UNSPECIFIED:
			default:
				result = size;
		}
		return result | (childMeasuredState & MEASURED_STATE_MASK);
	}

	private int resolveAdjustedSize(int desiredSize, int maxSize,
									int measureSpec) {
		int result = desiredSize;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
			case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
				result = Math.min(desiredSize, maxSize);
				break;
			case MeasureSpec.AT_MOST:
				// Parent says we can be as big as we want, up to specSize.
				// Don't be larger than specSize, and don't be larger than
				// the max size imposed on ourselves.
				result = Math.min(Math.min(desiredSize, specSize), maxSize);
				break;
			case MeasureSpec.EXACTLY:
				// No choice. Do what we are told.
				result = specSize;
				break;
		}
		return result;
	}
}
