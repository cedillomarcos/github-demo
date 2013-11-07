package com.rlk.scene;

import java.util.HashMap;

import com.rlk.scene.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup; 

public class LauncherValues {
	
	private static LauncherValues oneInstance;
	
	public static int mLongAxisEndPadding;
	public static int mLowestCellLocation;
    public static float mBubbleTextViewTranslateXY;
    public static int mScreenHeight;
    public static int mScreenWidth;
    public static float mScreenScale;
    public static float mMinimumStartDropDistance;
    
    public static int mGrayBgDrawLeft;
    public static int mGrayBgDrawTop;
    
    public static int mIconWdith;
    public static int mIconHeiht;
    public static int mMiniIconWidth;
    public static int mMiniIconHeight;
    public static int mMiniIconPaddingLeft;
    public static int mMiniIconPaddingTop;
    
    public static int mIconMirroPaddingLeft;
    public static int mIconMirroPaddingTop;
    
    public static int mFolderTopCurrowLeft;
    public static int mFolderBottomCurrowLeft;
    
    public static int mCalendarWeakTextSize;
    public static int mCalendarWeakTextTop;
    public static int mCalendarDateTextSize;
    public static int mCalendarDateTextTop;
    
    public static int mScrollZone;
	
	static int mMaxScreenCount = 12;	//最大的屏幕数
	static int mMaxScreenChildCount = 20;
	static int mMaxFolderChildCount = 9;
	static int mPanelScreen = -1;
    
	static boolean SHOW_TRANSPARENT_PANEL_MIRRO = true; //控制是否打开显示倒影的开关，可根据需求决定是否打开开关
	static final int EXPEND_FOLDER_VIEW = 1;
	static final int CLOSE_FOLDER_VIEW = 0;
	
	static final int DRAG_NO_STATU = -1;
	static final int DRAG_ON_CLOSE_FOLDER = 1;		
	static final int DRAG_ON_EXPEND_FOLDER = 2; //拖动图标在另一个图标上面，形成展开文件夹

	static int dragIconStatu = DRAG_NO_STATU;

	static boolean TRY_TO_CREATE_FOLDER = false; 				//第一次创建文件夹
	static boolean IGNORE_IPHONE_BUBBLE_TEXTVIEW = false;		//屏闭IphoneBubbleTextView上的DropTarget接口
	static boolean IGNORE_WORKSPACE_TARGET = false;  			//屏闭workspace上的DropTarget接口
	static boolean IGNORE_TRANSPARENTPANEL_TARGET = false;	//屏闭transparentPanel上的DropTarget接口
	public static boolean SCENE_BOOKCASE_TEXTVIEW = false;
	
	static boolean DO_NOT_REQUEST_TRANSPARENTPANEL_LAYOUT = false;

	private boolean mHasFolderOpen = false;
	
	private boolean anim;
	private HashMap<Integer,Boolean> beforeAnim ;
	private Launcher mLauncher;
	
	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	} 
	public Launcher getLauncher() {
		return mLauncher;
	}
	public boolean isAnim() {
		return anim;
	}
	 
	
	public void setAnim(boolean anim, DragLayer v) {
		if (anim == this.anim) {
			return;
		}
		this.anim = anim;
		if (anim == false || v == null) {
			return;
		}
		if(LauncherValues.getInstance().mHasFolderOpen){
			ExpendFolder expendFolder = Launcher.getInstance().getExpendFolderView();
			if(expendFolder != null){
				CellLayout cellLayout = expendFolder.getBody().getCellLayout();
				int cellLayoutCount = cellLayout.getChildCount();
				for (int i = 0; i < cellLayoutCount; i++) {
					View child = cellLayout.getChildAt(i);
					if (child instanceof IphoneBubbleTextView) {
						((IphoneBubbleTextView)child).startRotateAnimation();
					}
				}
			}
		}
		Workspace workspace = (Workspace) v.findViewById(R.id.workspace);

		int count = workspace.getChildCount();
		for (int i = 0; i < count; i++) {
			CellLayout cl = (CellLayout)workspace.getChildAt(i);
			int childCount = cl.getChildCount();
			for (int j = 0; j < childCount; j++) {
				View child = cl.getChildAt(j);
				if (child instanceof IphoneBubbleTextView) {
					((IphoneBubbleTextView)child).startRotateAnimation();
				}
			}
		} 
	}
	private LauncherValues(Context context) {
		beforeAnim = new HashMap<Integer,Boolean>();
	} 
	
	public static void init(Context context) {
		oneInstance = new LauncherValues(context);
	}
	public static LauncherValues getInstance() {
		return oneInstance;
	}
	public void initBeforeAnim(int cellLayoutId,boolean state) {
		beforeAnim.put(cellLayoutId, state);
	}
	public void changeBeforeAnim(int cellLayoutId,boolean state) {
		beforeAnim.put(cellLayoutId,state);
	}
	public boolean getBeforeAnim(int cellLayoutId) {
		return beforeAnim.get(cellLayoutId);
	}
	public boolean isChangedAnim(int cellLayoutId) {
		if (beforeAnim.get(cellLayoutId) == null)
			return false;
		if (beforeAnim.get(cellLayoutId) == anim) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isHasFolderOpen() {
		return mHasFolderOpen;
	}
	public void setHasFolderOpen(boolean hasFolderOpen) {
		mHasFolderOpen = hasFolderOpen;
	}
}
