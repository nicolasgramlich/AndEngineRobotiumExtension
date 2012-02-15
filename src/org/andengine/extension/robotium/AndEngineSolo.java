package org.andengine.extension.robotium;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.IEntity.IEntityMatcher;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.ITouchController;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.Constants;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.jayway.android.robotium.solo.Solo;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 13:20:29 - 14.02.2012
 */
public class AndEngineSolo extends Solo {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public AndEngineSolo(final Instrumentation pInstrumentation) {
		super(pInstrumentation);
	}

	public AndEngineSolo(final Instrumentation pInstrumentation, final Activity pActivity) {
		super(pInstrumentation, pActivity);

		if(!(pActivity instanceof BaseGameActivity)) {
			Assert.fail("The supplied " + Activity.class.getSimpleName() + " does not subclass '" + BaseGameActivity.class.getSimpleName() + "' but is a '" + pActivity.getClass().getSimpleName() + "'.");
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public BaseGameActivity getGameActivity() {
		return (BaseGameActivity)this.getCurrentActivity();
	}

	public Engine getEngine() {
		return this.getGameActivity().getEngine();
	}

	public void clickOnEntity(final Object pTag) {
		this.clickOnEntity(IEntity.class, pTag);
	}

	public void clickOnEntity(final Class<? extends IEntity> pClass, final Object pTag) {
		final IEntity result = this.getUniqueEntityByTag(pTag);

		final float[] sceneCenterCoordinate = result.getSceneCenterCoordinates();
		final float sceneX = sceneCenterCoordinate[Constants.VERTEX_INDEX_X];
		final float sceneY = sceneCenterCoordinate[Constants.VERTEX_INDEX_Y];

		this.clickOnScene(sceneX, sceneY);
	}

	public boolean isEntityVisible(final Object pTag) {
		return this.isEntityVisible(IEntity.class, pTag);
	}

	public boolean isEntityVisible(final Class<? extends IEntity> pClass, final Object pTag) {
		final IEntity result = this.getUniqueEntityByTag(pClass, pTag);

		return result.isVisible();
	}

	public void clickOnScene(final float pSceneX, final float pSceneY) {
		final TouchEvent sceneTouchEvent = TouchEvent.obtain(pSceneX, pSceneY, TouchEvent.ACTION_DOWN, 0, null);

		final Camera camera = this.getEngine().getCamera();
		camera.convertSceneToSurfaceTouchEvent(sceneTouchEvent, camera.getSurfaceWidth(), camera.getSurfaceHeight());

		final float surfaceX = sceneTouchEvent.getX();
		final float surfaceY = sceneTouchEvent.getY();

		sceneTouchEvent.recycle();

		this.clickOnSurface(surfaceX, surfaceY);
	}

	public void clickOnSurface(final float pSurfaceX, final float pSurfaceY) {
		final long downTime = SystemClock.uptimeMillis();
		final long eventTime = SystemClock.uptimeMillis();

		final MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, pSurfaceX, pSurfaceY, 0);
		final MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, pSurfaceX, pSurfaceY, 0);

		final ITouchController touchController = this.getEngine().getTouchController();

		touchController.onHandleMotionEvent(downEvent);
		touchController.onHandleMotionEvent(upEvent);
	}

	private IEntity getUniqueEntityByTag(final Object pTag) {
		final ArrayList<IEntity> result = this.querySceneByTag(pTag);
		this.assertListSize(1, result);
		return result.get(0);
	}

	private IEntity getUniqueEntityByTag(final Class<? extends IEntity> pClass, final Object pTag) {
		final ArrayList<IEntity> result = this.querySceneByTag(pClass, pTag);
		this.assertListSize(1, result);
		return result.get(0);
	}

	private void assertListSize(final int pSize, final List<IEntity> pEntityList) {
		Assert.assertEquals(pSize, pEntityList.size());
	}

	private ArrayList<IEntity> querySceneByTag(final Class<? extends IEntity> pClass, final Object pTag) {
		return this.getEngine().getScene().query(new IEntityMatcher() {
			@Override
			public boolean matches(final IEntity pEntity) {
				return pClass.isInstance(pEntity) && pTag.equals(pEntity.getUserData());
			}
		});
	}

	private ArrayList<IEntity> querySceneByTag(final Object pTag) {
		return this.getEngine().getScene().query(new IEntityMatcher() {
			@Override
			public boolean matches(final IEntity pEntity) {
				return pTag.equals(pEntity.getUserData());
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
