package com.mediatek.gallery3d.util;

import com.mediatek.mmprofile.MMProfile;

// This class is actually a wrapper class for MMProfile.
// It providers additional function as feature options
public class MediatekMMProfile {
    //events used by GALLERY
    private static int EVENT_GALLERY_ROOT;
    private static String NAME_GALLERY_ROOT = "GALLERYApp";

    // Frame
    private static int EVENT_GALLERY_VTNP_ONDRAWFRAME;
    private static int EVENT_FRAME_AVAILABLE;
    private static int EVENT_FRAME_DRAW_AVAILABLE;
    private static int EVENT_DRAW_SCREEN_NAIL;

    private static String NAME_GALLERY_VTNP_ONDRAWFRAME = "Gallery2VideoThumbnailPlayback";
    private static String NAME_FRAME_AVAILABLE = "FrameAvailable";
    private static String NAME_FIRST_FRAME_AVAILABLE = "FirstFrameAvailable";
    private static String NAME_DRAW_SCREEN_NAIL = "DrawScreenNail";

    // Render
    private static int EVENT_GLROOTVIEW;
    private static int EVENT_GLROOTVIEW_REQUEST_RENDER;
    private static int EVENT_GLROOTVIEW_ONDRAWFRAME;

    private static String NAME_GLROOTVIEW = "GLRootView";
    private static String NAME_GLROOTVIEW_REQUEST_RENDER = "GLRootViewRequestRender";
    private static String NAME_GLROOTVIEW_ONDRAWFRAME = "GLRootViewOnDrawFrame";

    // UI related
    private static int EVENT_UI_RELATED;
    private static int EVENT_UI_POS_CTRL_START_ANIM;
    private static int EVENT_UI_POS_START_SNAPBACK;
    private static int EVENT_UI_POS_SET_VIEW_SIZE;
    private static int EVENT_UI_POS_SET_IMAGE_SIZE;
    private static int EVENT_UI_POS_BOX_INFO;
    private static int EVENT_UI_POS_BOX_ANIM;

    private static String NAME_UI_RELATED = "UiRelated";
    private static String NAME_UI_POS_CTRL_START_ANIM = "PosStartAnimation";
    private static String NAME_UI_POS_START_SNAPBACK = "PosStartSnapback";
    private static String NAME_UI_POS_SET_VIEW_SIZE = "PosSetViewSize";
    private static String NAME_UI_POS_SET_IMAGE_SIZE = "PosSetImageSize";
    private static String NAME_UI_POS_BOX_INFO = "PosBoxInfo";
    private static String NAME_UI_POS_BOX_ANIM = "PosBoxAnim";

    // Activity
    private static int EVENT_GALLERY_ACTIVITY;
    private static int EVENT_GALLERY_ON_CREATE;
    private static int EVENT_GALLERY_ON_RESUME;
    private static int EVENT_GALLERY_ON_PAUSE;
    private static int EVENT_GALLERY_ON_DESTROY;
    private static int EVENT_GALLERY_START_UP;

    private static String NAME_GALLERY_ACTIVITY = "GalleryActivity";
    private static String NAME_GALLERY_ON_CREATE = "GalleryOnCreate";
    private static String NAME_GALLERY_ON_RESUME = "GalleryOnResume";
    private static String NAME_GALLERY_ON_PAUSE = "GalleryOnPause";
    private static String NAME_GALLERY_ON_DESTROY = "GalleryOnDestroy";
    private static String NAME_GALLERY_START_UP = "GalleryStartUp";

    // AlbumSetPage
    private static int EVENT_ALBUMSETPAGE_ACTIVITY;
    private static int EVENT_ALBUMSETPAGE_ON_CREATE;
    private static int EVENT_ALBUMSETPAGE_ON_RESUME;

    private static String NAME_ALBUMSETPAGE_ACTIVITY = "AlbumSetPageActivity";
    private static String NAME_ALBUMSETPAGE_ON_CREATE = "AlbumSetPageOnCreate";
    private static String NAME_ALBUMSETPAGE_ON_RESUME = "AlbumSetPageOnResume";

    // PhotoPage
    private static int EVENT_PHOTOPAGE_ACTIVITY;
    private static int EVENT_PHOTOPAGE_ON_CREATE;
    private static int EVENT_PHOTOPAGE_ON_RESUME;

    private static String NAME_PHOTOPAGE_ACTIVITY = "PhotoPageActivity";
    private static String NAME_PHOTOPAGE_ON_CREATE = "PhotoPageOnCreate";
    private static String NAME_PHOTOPAGE_ON_RESUME = "PhotoPageOnResume";

    // PhotoPage DataLoader
    private static int EVENT_PHOTOPAGE_DATALOADER;
    private static int EVENT_PHOTOPAGE_RELOAD_DATA;
    private static int EVENT_PHOTOPAGE_GET_UPDATE_INFO;
    private static int EVENT_PHOTOPAGE_UPDATE_CONTENT;
    private static int EVENT_PHOTOPAGE_UPDATE_SLIDING_WINDOW;
    private static int EVENT_PHOTOPAGE_UPDATE_IMAGE_CACHE;
    private static int EVENT_PHOTOPAGE_UPDATE_TILE_PROVIDER;
    private static int EVENT_PHOTOPAGE_UPDATE_IMAGE_REQUEST;
    private static int EVENT_PHOTOPAGE_FIRE_DATA_CHANGE;
    private static int EVENT_PHOTOPAGE_DECODE_SCREENNAIL_SUBMIT;
    private static int EVENT_PHOTOPAGE_DECODE_SCREENNAIL_JOB;
    private static int EVENT_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER;

    private static String NAME_PHOTOPAGE_DATALOADER = "Gallery2PhotoPageDataLoader";
    private static String NAME_PHOTOPAGE_RELOAD_DATA = "PhotoPageReloadData";
    private static String NAME_PHOTOPAGE_GET_UPDATE_INFO = "PhotoPageGetUpdateInfo";
    private static String NAME_PHOTOPAGE_UPDATE_CONTENT = "PhotoPageUpdateContent";
    private static String NAME_PHOTOPAGE_UPDATE_SLIDING_WINDOW = "PhotoPageUpdateSlidingWindow";
    private static String NAME_PHOTOPAGE_UPDATE_IMAGE_CACHE = "PhotoPageUpdateImageCache";
    private static String NAME_PHOTOPAGE_UPDATE_TILE_PROVIDER = "PhotoPageUpdateTileProvider";
    private static String NAME_PHOTOPAGE_UPDATE_IMAGE_REQUEST = "PhotoPageUpdateImageRequest";
    private static String NAME_PHOTOPAGE_FIRE_DATA_CHANGE = "PhotoPageFireDataChange";
    private static String NAME_PHOTOPAGE_DECODE_SCREENNAIL_SUBMIT = "PhotoPageDecodeScreenNailSubmit";
    private static String NAME_PHOTOPAGE_DECODE_SCREENNAIL_JOB = "PhotoPageDecodeScreenNailJob";
    private static String NAME_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER = "PhotoPageDecodeScreenNailListener";

    // AlbumSetPage DataLoader
    private static int EVENT_ALBUMSETPAGE_DATALOADER;
    private static int EVENT_ALBUMSETPAGE_RELOAD_DATA;
    private static int EVENT_ALBUMSETPAGE_DECODE_SCREENNAIL_SUBMIT;
    private static int EVENT_ALBUMSETPAGE_UPDATE_COVER_ITEM;

    private static String NAME_ALBUMSETPAGE_DATALOADER = "Gallery2AlbumSetPageDataLoader";
    private static String NAME_ALBUMSETPAGE_RELOAD_DATA = "AlbumSetPageReloadData";
    private static String NAME_ALBUMSETPAGE_DECODE_SCREENNAIL_SUBMIT = "AlbumSetPageDecodeScreenNailSubmit";
    private static String NAME_ALBUMSETPAGE_UPDATE_COVER_ITEM = "AlbumSetPageUPdateCoverItem";
    private static String NAME_ALBUMSETPAGE_UPDATE_ENTRY = "AlbumSetPageUpdateEntry";
    private static String NAME_ALBUMSETPAGE_COVER_UPDATE = "AlbumSetPageCoverUpdate";

    static {
        EVENT_GALLERY_ROOT = MMProfileWrapper.MMProfileRegisterEvent(
            MMProfileWrapper.MMP_RootEvent, NAME_GALLERY_ROOT);

        // for preview frame
        EVENT_GALLERY_VTNP_ONDRAWFRAME = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ROOT, NAME_GALLERY_VTNP_ONDRAWFRAME);
        EVENT_FRAME_AVAILABLE = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_VTNP_ONDRAWFRAME, NAME_FRAME_AVAILABLE);
        EVENT_FRAME_DRAW_AVAILABLE = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_VTNP_ONDRAWFRAME, NAME_FIRST_FRAME_AVAILABLE);
        EVENT_DRAW_SCREEN_NAIL = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_VTNP_ONDRAWFRAME, NAME_DRAW_SCREEN_NAIL);

        // for Gallery Render
        EVENT_GLROOTVIEW = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_GLROOTVIEW);
        EVENT_GLROOTVIEW_REQUEST_RENDER = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GLROOTVIEW, NAME_GLROOTVIEW_REQUEST_RENDER);
        EVENT_GLROOTVIEW_ONDRAWFRAME = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GLROOTVIEW, NAME_GLROOTVIEW_ONDRAWFRAME);
        // for ui related
        EVENT_UI_RELATED = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_UI_RELATED);
        EVENT_UI_POS_CTRL_START_ANIM = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_CTRL_START_ANIM);
        EVENT_UI_POS_START_SNAPBACK = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_START_SNAPBACK);
        EVENT_UI_POS_SET_VIEW_SIZE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_SET_VIEW_SIZE);
        EVENT_UI_POS_SET_IMAGE_SIZE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_SET_IMAGE_SIZE);
        EVENT_UI_POS_BOX_INFO = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_BOX_INFO);
        EVENT_UI_POS_BOX_ANIM = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_UI_RELATED, NAME_UI_POS_BOX_ANIM);


        // for Gallery activity
        EVENT_GALLERY_ACTIVITY = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ROOT, NAME_GALLERY_ACTIVITY);
        EVENT_GALLERY_ON_CREATE = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ACTIVITY, NAME_GALLERY_ON_CREATE);
        EVENT_GALLERY_ON_RESUME = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ACTIVITY, NAME_GALLERY_ON_RESUME);
        EVENT_GALLERY_ON_PAUSE = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ACTIVITY, NAME_GALLERY_ON_PAUSE);
        EVENT_GALLERY_ON_DESTROY = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ACTIVITY, NAME_GALLERY_ON_DESTROY);
        EVENT_GALLERY_START_UP = MMProfileWrapper.MMProfileRegisterEvent(
            EVENT_GALLERY_ACTIVITY, NAME_GALLERY_START_UP);

        //for Gallery AlbumSetPage
        EVENT_ALBUMSETPAGE_ACTIVITY = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_ALBUMSETPAGE_ACTIVITY);
        EVENT_ALBUMSETPAGE_ON_CREATE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_ALBUMSETPAGE_ACTIVITY, NAME_ALBUMSETPAGE_ON_CREATE);
        EVENT_ALBUMSETPAGE_ON_RESUME = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_ALBUMSETPAGE_ACTIVITY, NAME_ALBUMSETPAGE_ON_RESUME);

        EVENT_ALBUMSETPAGE_DATALOADER = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_ALBUMSETPAGE_DATALOADER);
        EVENT_ALBUMSETPAGE_RELOAD_DATA = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_ALBUMSETPAGE_DATALOADER, NAME_ALBUMSETPAGE_RELOAD_DATA);
        EVENT_ALBUMSETPAGE_DECODE_SCREENNAIL_SUBMIT = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_ALBUMSETPAGE_DATALOADER, NAME_ALBUMSETPAGE_DECODE_SCREENNAIL_SUBMIT);
        EVENT_ALBUMSETPAGE_UPDATE_COVER_ITEM = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_ALBUMSETPAGE_DATALOADER, NAME_ALBUMSETPAGE_UPDATE_COVER_ITEM);

        //for Gallery PhotoPage
        EVENT_PHOTOPAGE_ACTIVITY = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_PHOTOPAGE_ACTIVITY);
        EVENT_PHOTOPAGE_ON_CREATE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_ACTIVITY, NAME_PHOTOPAGE_ON_CREATE);
        EVENT_PHOTOPAGE_ON_RESUME = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_ACTIVITY, NAME_PHOTOPAGE_ON_RESUME);

        //for Gallery PhotoPage Dataloader
        EVENT_PHOTOPAGE_DATALOADER = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_GALLERY_ROOT, NAME_PHOTOPAGE_DATALOADER);
        EVENT_PHOTOPAGE_RELOAD_DATA = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_RELOAD_DATA);
        EVENT_PHOTOPAGE_GET_UPDATE_INFO = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_GET_UPDATE_INFO);
        EVENT_PHOTOPAGE_UPDATE_CONTENT = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_UPDATE_CONTENT);
        EVENT_PHOTOPAGE_UPDATE_SLIDING_WINDOW = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_UPDATE_SLIDING_WINDOW);
        EVENT_PHOTOPAGE_UPDATE_IMAGE_CACHE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_UPDATE_IMAGE_CACHE);
        EVENT_PHOTOPAGE_UPDATE_TILE_PROVIDER = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_UPDATE_TILE_PROVIDER);
        EVENT_PHOTOPAGE_UPDATE_IMAGE_REQUEST = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_UPDATE_IMAGE_REQUEST);
        EVENT_PHOTOPAGE_FIRE_DATA_CHANGE = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_FIRE_DATA_CHANGE);
        EVENT_PHOTOPAGE_DECODE_SCREENNAIL_SUBMIT = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_DECODE_SCREENNAIL_SUBMIT);
        EVENT_PHOTOPAGE_DECODE_SCREENNAIL_JOB = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_DECODE_SCREENNAIL_JOB);
        EVENT_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER = MMProfileWrapper.MMProfileRegisterEvent(
                EVENT_PHOTOPAGE_DATALOADER, NAME_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER);
    }

    /*************Frame Start*********************/
    public static void triggerFrameAvailable() {
        MMProfileWrapper.MMProfileLog(
            EVENT_FRAME_AVAILABLE,
            MMProfileWrapper.MMProfileFlagPulse);
    }

    public static void triggerFirstFrameAvailable() {
        MMProfileWrapper.MMProfileLog(
            EVENT_FRAME_DRAW_AVAILABLE,
            MMProfileWrapper.MMProfileFlagPulse);
    }

    public static void startProfileFirstFrameAvailable() {
        MMProfileWrapper.MMProfileLog(
            EVENT_FRAME_DRAW_AVAILABLE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileFirstFrameAvailable() {
        MMProfileWrapper.MMProfileLog(
            EVENT_FRAME_DRAW_AVAILABLE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileDrawScreenNail() {
        MMProfileWrapper.MMProfileLog(
            EVENT_DRAW_SCREEN_NAIL,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileDrawScreenNail() {
        MMProfileWrapper.MMProfileLog(
            EVENT_DRAW_SCREEN_NAIL,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************Frame End*********************/

    /*************GLRootView Start*********************/
    public static void triggerGLRootViewRequest() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GLROOTVIEW_REQUEST_RENDER,
            MMProfileWrapper.MMProfileFlagPulse);
    }

    public static void startProfileOnDrawFrame() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GLROOTVIEW_ONDRAWFRAME,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileOnDrawFrame() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GLROOTVIEW_ONDRAWFRAME,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************GLRootView End*********************/

    /*************UI related Start*********************/
    public static void triggerProfileStartAnimation(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_CTRL_START_ANIM,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }

    public static void triggerProfileStartSnapback(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_START_SNAPBACK,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }

    public static void triggerProfileSetView(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_SET_VIEW_SIZE,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }

    public static void triggerProfileSetImageSize(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_SET_IMAGE_SIZE,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }

    public static void triggerProfileBoxInfo(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_BOX_INFO,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }

    public static void triggerProfileBoxAnim(String str) {
        MMProfileWrapper.MMProfileLogMetaString(
            EVENT_UI_POS_BOX_ANIM,
            MMProfileWrapper.MMProfileFlagPulse, str);
    }



    /*************UI related End*********************/

    /*************AblumSetPage Start*********************/
    public static void startProfileAlbumSetPageOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_ON_CREATE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileAlbumSetPageOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_ON_CREATE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileAlbumSetPageOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_ON_RESUME,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileAlbumSetPageOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_ON_RESUME,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************AblumSetPage End*********************/

    /*************Gallery AblumSetPage DataLoader Start*********************/
    public static void startProfileAlbumSetPageReloadData() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_RELOAD_DATA,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileAlbumSetPageReloadData() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_RELOAD_DATA,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void triggerAlbumSetPageDecodeScreenNail() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_DECODE_SCREENNAIL_SUBMIT,
            MMProfileWrapper.MMProfileFlagPulse);
    }

    public static void startProfileAlbumSetPageUpdateCoverItem() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_UPDATE_COVER_ITEM,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileAlbumSetPageUpdateCoverItem() {
        MMProfileWrapper.MMProfileLog(
            EVENT_ALBUMSETPAGE_UPDATE_COVER_ITEM,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************Gallery AblumSetPage DataLoader Stop*********************/

    /*************PhotoPage Start*********************/
    public static void startProfilePhotoPageOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_ON_CREATE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_ON_CREATE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfilePhotoPageOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_ON_RESUME,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_ON_RESUME,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************PhotoPage End*********************/

    /*************Gallery PhotoPage DataLoader Start*********************/
    public static void startProfilePhotoPageReloadData() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_RELOAD_DATA,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageReloadData() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_RELOAD_DATA,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageGetUpdateInfo() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_GET_UPDATE_INFO,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageGetUpdateInfo() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_GET_UPDATE_INFO,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageUpdateContent() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_CONTENT,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageUpdateContent() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_CONTENT,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageUpdateSlidingWindow() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_SLIDING_WINDOW,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageUpdateSlidingWindow() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_SLIDING_WINDOW,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageUpdateImageCache() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_IMAGE_CACHE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageUpdateImageCache() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_IMAGE_CACHE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfilePhotoPageUpdateTileProvider() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_TILE_PROVIDER,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageUpdateTileProvider() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_TILE_PROVIDER,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageUpdateImageRequest() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_IMAGE_REQUEST,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageUpdateImageRequest() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_UPDATE_IMAGE_REQUEST,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageFireDataChange() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_FIRE_DATA_CHANGE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageFireDataChange() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_FIRE_DATA_CHANGE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void triggerPhotoPageDecodeScreenNail() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_DECODE_SCREENNAIL_SUBMIT,
            MMProfileWrapper.MMProfileFlagPulse);
    }

    public static void startProfilePhotoPageDecodeScreenNailJob() {
        MMProfileWrapper.MMProfileLog(
           EVENT_PHOTOPAGE_DECODE_SCREENNAIL_JOB,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageDecodeScreenNailJob() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_DECODE_SCREENNAIL_JOB,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    
    public static void startProfilePhotoPageDecodeScreenNailListener() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfilePhotoPageDecodeScreenNailListener() {
        MMProfileWrapper.MMProfileLog(
            EVENT_PHOTOPAGE_DECODE_SCREENNAIL_LISTENER,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************Gallery PhotoPage DataLoader Stop*********************/

    /*************GalleryActivity Start*********************/
    public static void startProfileGalleryOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_CREATE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileGalleryOnCreate() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_CREATE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileGalleryOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_RESUME,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileGalleryOnResume() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_RESUME,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileGALLERYOnPause() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_PAUSE,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileGALLERYOnPause() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_PAUSE,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileGALLERYOnDestroy() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_DESTROY,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileGALLERYOnDestroy() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_ON_DESTROY,
            MMProfileWrapper.MMProfileFlagEnd);
    }

    public static void startProfileGalleryStartUp() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_START_UP,
            MMProfileWrapper.MMProfileFlagStart);
    }

    public static void stopProfileGalleryStartUp() {
        MMProfileWrapper.MMProfileLog(
            EVENT_GALLERY_START_UP,
            MMProfileWrapper.MMProfileFlagEnd);
    }
    /*************GalleryActivity End*********************/


    // wrapper class for MMProfile.
    // this class will be most useful during migration
    private static class MMProfileWrapper {
        private static final int MMP_RootEvent = MMProfile.MMP_RootEvent;
        private static final int MMProfileFlagStart = MMProfile.MMProfileFlagStart;
        private static final int MMProfileFlagEnd = MMProfile.MMProfileFlagEnd;
        private static final int MMProfileFlagPulse = MMProfile.MMProfileFlagPulse;
        private static final int MMProfileFlagEventSeparator = MMProfile.MMProfileFlagEventSeparator;

        public static int MMProfileRegisterEvent(int parent, String name) {
            return MMProfile.MMProfileRegisterEvent(parent, name);
        }
        public static int MMProfileFindEvent(int parent, String name) {
            return MMProfile.MMProfileFindEvent(parent, name);
        }
        public static void MMProfileEnableEvent(int event, int enable) {
            MMProfile.MMProfileEnableEvent(event, enable);
        }
        public static void MMProfileEnableEventRecursive(int event, int enable) {
            MMProfile.MMProfileEnableEventRecursive(event, enable);
        }
        public static int MMProfileQueryEnable(int event) {
            return MMProfile.MMProfileQueryEnable(event);
        }
        public static void MMProfileLog(int event, int type) {
            MMProfile.MMProfileLog(event, type);
        }
        public static void MMProfileLogEx(int event, int type, int data1, int data2) {
            MMProfile.MMProfileLogEx(event, type, data1, data2);
        }
        public static int MMProfileLogMetaString(int event, int type, String str) {
            return MMProfile.MMProfileLogMetaString(event, type, str);
        }
        public static int MMProfileLogMetaStringEx(int event, int type, int data1, int data2, String str) {
            return MMProfile.MMProfileLogMetaStringEx(event, type, data1, data2, str);
        }
    }
}
