package com.rlk.scene;

import java.util.ArrayList;
import java.util.List; 

import com.rlk.scene.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FolderLinearLayout extends LinearLayout implements DropTarget,DragSource{
	private static boolean DEBUG = true;
	private static String TAG = "FolderLinearLayout";
	private static String TAG_MEASURE = "IphoneCellLayout";
	
	private IphoneClearEditText mTitleEdit;
	private TextView mTitle;
	private CellLayout mCellLayout;
	private int mTitleEditHeight;
	private int mTitleHeight;
	private int mCellLayoutHeight;
	
	private List<DropTarget> mFolderDropTargets;
	
	public CellLayout getCellLayout() {
		return mCellLayout;
	}
	
	public FolderLinearLayout(Context context) {
		this(context,null);
	}
	
	public FolderLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void showTitleEditText(){
		mTitle.setVisibility(View.GONE);
		mTitleEdit.setVisibility(View.VISIBLE);
		IphoneBubbleTextView folder = Launcher.getInstance().getFolderTarget();
		if(folder != null){
			ApplicationInfo info = (ApplicationInfo) folder.getTag();
			mTitleEdit.setText(info.title.toString());
		}
	}
	
	public void hideTitleEditText(){
		IphoneBubbleTextView folder = Launcher.getInstance().getFolderTarget();
		ApplicationInfo info = (ApplicationInfo) folder.getTag();
		mTitle.setVisibility(View.VISIBLE);
		String newTitle = mTitleEdit.getText().toString();
		if(!newTitle.equals("")){
			mTitle.setText(newTitle);
		}else{
			mTitle.setText(info.title);
		}
		mTitleEdit.setVisibility(View.GONE);
		if(folder != null && !info.title.equals(newTitle)
				&& !newTitle.equals("")){
			info.title = newTitle;
				LauncherModel.updateItemInDatabase(getContext(), info);
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitleEdit = (IphoneClearEditText)findViewById(R.id.edit_title);
		mTitle = (TextView)findViewById(R.id.text_title);
		IphoneBubbleTextView textView = Launcher.getInstance().getFolderTarget();
		if(textView != null){
			mTitle.setText(((ApplicationInfo)textView.getTag()).title);
		}else{
			mTitle.setText("");
		}
		mCellLayout = (CellLayout) findViewById(R.id.cell_layout);
		mFolderDropTargets = new ArrayList<DropTarget>();
		mFolderDropTargets.add((DropTarget) this);
		mFolderDropTargets.add((DropTarget) Launcher.getInstance().getWorkspace());
	}
	
	public List<DropTarget> getDropTargets(){
		return mFolderDropTargets;
	}

	public boolean acceptDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		return true;
	}

	public void onDragEnter(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
	}
	
	private void revertAppFromFolder(FolderInfo folderInfo, ApplicationInfo appInfo){
		appInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		appInfo.cellX = folderInfo.cellX;
		appInfo.cellY = folderInfo.cellY;
		appInfo.screen = folderInfo.screen;
		appInfo.iphoneBubbleTextView = folderInfo.iphoneBubbleTextView;
		appInfo.folderInfo = null;
		appInfo.isFolderItem = false;
	}
	
	public void onDragExit(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		
		CellLayout cellLayout = Launcher.getInstance().getCurrentCellLayout();
		IphoneBubbleTextView folderTarget = Launcher.getInstance().getFolderTarget();
		TextView textTitle = folderTarget.getTextTtile();
		LauncherValues.dragIconStatu = LauncherValues.DRAG_NO_STATU;
		
		ApplicationInfo appInfo = (ApplicationInfo) folderTarget.getTag();
		ApplicationInfo appDragInfo = (ApplicationInfo)dragInfo;
		textTitle.setText(appInfo.title);
		appInfo.icon.setAlpha(255);
		
		if(appInfo.isFolder){
			FolderInfo folderInfo = (FolderInfo)appInfo;
			
			if(LauncherValues.TRY_TO_CREATE_FOLDER){
				
				LauncherValues.TRY_TO_CREATE_FOLDER = false;
				folderInfo.items.remove(appDragInfo);
				folderInfo.itemsId.remove(appDragInfo.id);
				appDragInfo.folderInfo = null;
				appDragInfo.isFolderItem = false;
				//Modify GWLLSW-946 ningyaoyun 20121023(on) 
				if(folderInfo.items.size() > 0){
					ApplicationInfo info = folderInfo.items.get(0);
					revertAppFromFolder(folderInfo, info);

					LauncherModel.deleteItemFromDatabase(getContext(), folderInfo);
					LauncherModel.updateItemInDatabase(getContext(), info);
					folderTarget.setTag(info);
					folderTarget.updateNotificationCount(info.launcherCount);
					textTitle.setText(info.title);
					
					folderTarget.clearMirro(); 
					textTitle.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
				}
				//Modify GWLLSW-946 ningyaoyun 20121023(off)				
				folderTarget.setDrawFolderBg(false);
				folderTarget.requestLayout();
				LauncherSettings.Favorites.ON_FOLDER_TARGET = false;
				Launcher.getInstance().closeFolder(cellLayout, folderTarget);
				return;
			}else if(folderInfo.itemsId.size() == 1){
				
				ApplicationInfo info = folderInfo.items.get(0);
				if(info.id == appDragInfo.id){
//					if(folderInfo.isInTransparentPanel){
//						Launcher.getModel().removePanelItem(folderInfo);
//						LauncherModel.deleteItemFromPanelDatabase(getContext(), folderInfo);
//						Launcher.getInstance().getTransparentPanel().removeView(folderTarget);
//						Launcher.getInstance().getTransparentPanel().getDropTargets().remove((DropTarget) folderTarget);
//					}else{
						Launcher.getModel().removeDesktopItem(info);
						LauncherModel.deleteItemFromDatabase(getContext(), folderInfo);
						Launcher.getInstance().getCurrentCellLayout().removeView(folderTarget);
						Launcher.getInstance().getWorkspace().getDropTargets().remove((DropTarget) folderTarget);
//					}
					folderInfo = null;
					appDragInfo.folderInfo = null;
					Launcher.getInstance().closeFolder(cellLayout, folderTarget);
					return;
				}
			}else{
				
				folderInfo.items.remove(appDragInfo);
				folderInfo.itemsId.remove(appDragInfo.id);
				folderInfo.launcherCount -= appDragInfo.launcherCount;
				appDragInfo.folderInfo = null;
				folderTarget.updateNotificationCount(folderInfo.launcherCount);
				
					LauncherModel.updateItemInDatabase(getContext(), folderInfo);

				folderInfo.icon = Utilities.createFolderIcon(getContext(), folderInfo.items, Utilities.GRAY_BG);
				folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
			}
		}
		
		folderTarget.clearMirro(); 
		textTitle.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);
		folderTarget.setDrawFolderBg(false);
		folderTarget.requestLayout();
		LauncherSettings.Favorites.ON_FOLDER_TARGET = false;
		Launcher.getInstance().closeFolder(cellLayout, folderTarget);
	}
	
    private int[] mStartMoveLocation = new int[]{-1,-1};
    private int[] mFirstVacantLocation = new int[]{-1,-1};
    private int[] mLastVacantLocation = new int[]{-1,-1};
    private int[] mCellXY = new int[]{-1,-1};
    private int[] mTemp = new int[]{-1,-1};
    
    public void resetParameter(){
    	mStartMoveLocation[0] = -1;
    	mStartMoveLocation[1] = -1;
    	mCellXY[0] = -1;
    	mCellXY[1] = -1;
    	mTemp[0] = -1;
    	mTemp[1] = -1;
    }
    
	public void onDragOver(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo, boolean isDrop) {
		
		Workspace workspace = Launcher.getInstance().getWorkspace();
		ApplicationInfo info = (ApplicationInfo) dragInfo;
		int[] cellXY = mCellXY;
    	int[] firstVacantLoacation = mFirstVacantLocation;
    	mCellLayout.pointToCellExact(x, y - mTitleHeight - mTitleEditHeight, cellXY);
    	
    	int count = mCellLayout.getChildCount();
    	int maxY = count % 4 == 0 ? count / 4 - 1 : count / 4;
    	int maxX = (count - 1) % 4;
    	
    	if(cellXY[1] > maxY){
    		cellXY[1] = maxY;
    	} else if (cellXY[1] == maxY){
    		if(source == this){
    			if(cellXY[0] > maxX){
        			cellXY[0] = maxX;
        		}
    		}else{
    			if(cellXY[0] > maxX){
        			cellXY[0] = maxX + 1;
        		}
    		}
    	}
    	
    	if(mStartMoveLocation[0] == -1 && mStartMoveLocation[1] == -1){
    		mTemp[0] = info.cellX;
    		mTemp[1] = info.cellY;
    	}
    	if(mStartMoveLocation[0] == cellXY[0] && mStartMoveLocation[1] == cellXY[1]){
    		return;
    	}
    	
    	info.hasMoveOnFolderLinearLayout = true;
    	mStartMoveLocation[0] = cellXY[0];
    	mStartMoveLocation[1] = cellXY[1];
    	info.cellX = cellXY[0];
    	info.cellY = cellXY[1];
    	
    	if(DEBUG) 
    		Log.d(TAG, "cellXY[0] = " + cellXY[0] + " cellXY[1] = " + cellXY[1]);

		if(mCellLayout.getVacantCell(firstVacantLoacation, 1, 1, info.iphoneBubbleTextView)){
			mCellLayout.getLastCellLocation(mLastVacantLocation, 1);
			
			if(workspace.checkMoveLocation(firstVacantLoacation, mStartMoveLocation) < 0 
					&& workspace.checkMoveLocation(mLastVacantLocation, firstVacantLoacation) > 0){
				mCellLayout.cellMoveLeft(firstVacantLoacation, mStartMoveLocation, info.iphoneBubbleTextView, false, null);
			}else if(workspace.checkMoveLocation(firstVacantLoacation, mStartMoveLocation) > 0){
				mCellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, info.iphoneBubbleTextView, false, null);
			}
		}
	}

	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		
		if(DEBUG) Log.d(TAG, "FolderLinearLayout onDrop");
		ApplicationInfo info = (ApplicationInfo)dragInfo;
		
		if(source != this){
			//Modify GWLLSW-898 ningyaoyun 20121020(on)
//			if(info.hasMoveOnFolderLinearLayout){
//				mCellLayout.addFolderItems(info, true);
//				info.hasMoveOnFolderLinearLayout = false;
//			}else{
				mCellLayout.addFolderItems(info, false);
//			}
			//Modify GWLLSW-898 ningyaoyun 20121020(off)			
			Workspace workspace = Launcher.getInstance().getWorkspace();
			workspace.onDropCompleted(this, true, dragInfo);
			IphoneBubbleTextView folderTarget = Launcher.getInstance().getFolderTarget();
			folderTarget.onDrop(source, x, y, xOffset, yOffset, dragInfo);
			
			FolderInfo folderInfo = (FolderInfo) folderTarget.getTag();
			mCellLayout.getOrderList(folderInfo.itemsId, folderInfo.items); 
			
				LauncherModel.updateItemInDatabase(getContext(), folderInfo);
			
			folderInfo.icon = Utilities.createFolderIcon(getContext(), folderInfo.items, Utilities.GRAY_BG);
			folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
			folderTarget.getTextTtile().setCompoundDrawablesWithIntrinsicBounds(null, folderInfo.icon, null, null);
		}else{
			if(info.isFolderItem){
				FolderInfo folderInfo = info.folderInfo;
				IphoneBubbleTextView bubbleTextView = folderInfo.iphoneBubbleTextView;
				TextView textTitle = bubbleTextView.getTextTtile();
				orderFolderList(folderInfo, info);
				folderInfo.icon = Utilities.createFolderIcon(getContext(), folderInfo.items, Utilities.GRAY_BG);
				folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
				textTitle.setCompoundDrawablesWithIntrinsicBounds(null, folderInfo.icon, null, null);
			}
		}
	}
	
	private void orderFolderList(FolderInfo folderInfo, ApplicationInfo applicationInfo){
		
		if(DEBUG) Log.d(TAG, "mTemp[0]=" + mTemp[0] + " mTemp[1]=" + mTemp[1]);
		int startIndex =  mTemp[0] + mTemp[1] * 4;
		int endIndex = applicationInfo.cellX + applicationInfo.cellY * 4;
		ArrayList<ApplicationInfo> items = folderInfo.items;
		
		if(DEBUG) Log.d(TAG, "startIndex = " + startIndex + "  endIndex = " + endIndex);
		
		if(startIndex > items.size() - 1){
			startIndex = items.size() - 1;
		}
		if(endIndex > items.size() - 1){
			endIndex = items.size() - 1;
		}
		ApplicationInfo temp = items.get(startIndex);
		
		if(endIndex > startIndex){
			for (int i = startIndex; i < endIndex; i++) {
				items.set(i, items.get(i + 1));
			}
		}else if(endIndex < startIndex) {
			for (int i = startIndex; i > endIndex; i--) {
				items.set(i, items.get(i - 1));
			}
		}
		items.set(endIndex, temp);
		
		folderInfo.itemsId.clear();
		for(ApplicationInfo info : items){
			folderInfo.itemsId.add(info.id);
		}
		
			LauncherModel.updateItemInDatabase(getContext(), folderInfo);
	}
	
	public void onDropCompleted(View target, boolean success, Object dragInfo) {
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(DEBUG) Log.d(TAG_MEASURE, "FolderLinearLayout measuer");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mTitleEditHeight = 0;
		mTitleHeight = 0;
		if(mTitle.getVisibility() != GONE){
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mTitle.getLayoutParams();
//			mTitle.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			mTitleHeight = params.height + params.topMargin + params.bottomMargin;
		}
		if(mTitleEdit.getVisibility() != GONE){
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mTitleEdit.getLayoutParams();
//			mTitleEdit.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			mTitleEditHeight = params.height+ params.topMargin + params.bottomMargin;
		}
		mCellLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mCellLayoutHeight = mCellLayout.getMeasuredHeight();
		int heightMeasure = mTitleEditHeight + mTitleHeight + mCellLayoutHeight;
		
//		if(DEBUG) Log.d(TAG, "widthMeasure " + widthMeasure);
//		if(DEBUG) Log.d(TAG, "heightMeasure " + heightMeasure);
//		if(DEBUG) Log.d(TAG, "mTitleEditHeight " + mTitleEditHeight);
//		if(DEBUG) Log.d(TAG, "mTitleHeight " + mTitleHeight);
//		if(DEBUG) Log.d(TAG, "mCellLayoutHeight " + mCellLayoutHeight);
		setMeasuredDimension(LauncherValues.mScreenWidth, heightMeasure);
	}

	public void onCompleteTranslate(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
	}
}
