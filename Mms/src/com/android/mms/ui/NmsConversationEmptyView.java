package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mms.R;
import com.mediatek.mms.ipmessage.IpMessageConsts.RemoteActivities;

public class NmsConversationEmptyView extends LinearLayout {

    private LinearLayout mBackground;
    private Context mContext;
    private View convertView;
    private TextView tvContent;
    private LinearLayout llActivate;
    private LinearLayout llImportant;
    private RelativeLayout llSpam;
    private RelativeLayout llGroupChat;
    private Button btnActivate;

    public NmsConversationEmptyView(Context context) {
        super(context);
    }

    public NmsConversationEmptyView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.conversation_empty, this, true);
        mBackground = (LinearLayout)convertView.findViewById(R.id.background);
        tvContent = (TextView)convertView.findViewById(R.id.tv_empty_content);
        llImportant = (LinearLayout)convertView.findViewById(R.id.ll_empty_important);
        llSpam = (RelativeLayout)convertView.findViewById(R.id.ll_empty_spam);
        llGroupChat = (RelativeLayout)convertView.findViewById(R.id.ll_empty_groupchat);
        llActivate = (LinearLayout)convertView.findViewById(R.id.ll_empty_activate);
        btnActivate = (Button)convertView.findViewById(R.id.btn_activate);
        btnActivate.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(RemoteActivities.ACTIVITION);
                // do to sim_id update.
                intent.putExtra(RemoteActivities.KEY_SIM_ID, 0); // need put int type SIM id
                MessageUtils.startRemoteActivity(context, intent);
            }});
    }

    public void setSpamEmpty(boolean isActivate){
        llSpam.setVisibility(View.VISIBLE);
        llImportant.setVisibility(View.GONE);
        llGroupChat.setVisibility(View.GONE);
        mBackground.setBackgroundResource(R.color.empty_background);
        tvContent.setText(R.string.str_ipmsg_spam_empty);
        setActivate(isActivate);

    }

    public void setImportantEmpty(boolean isActivate){
        llSpam.setVisibility(View.GONE);
        llImportant.setVisibility(View.VISIBLE);
        llGroupChat.setVisibility(View.GONE);
        mBackground.setBackgroundResource(R.color.empty_background);
        tvContent.setText(R.string.str_ipmsg_important_empty);
        setActivate(isActivate);
    }
    public void setGroupChatEmpty(boolean isActivate){
        llSpam.setVisibility(View.GONE);
        llImportant.setVisibility(View.GONE);
        llGroupChat.setVisibility(View.VISIBLE);
        mBackground.setBackgroundResource(R.color.empty_background);
        tvContent.setText(R.string.str_ipmsg_groupchat_empty);
        setActivate(isActivate);
    }

    public void setAllChatEmpty(){
        mBackground.setBackgroundResource(R.color.transparent);
        tvContent.setText(R.string.str_ipmsg_allchat_empty);
        llSpam.setVisibility(View.GONE);
        llImportant.setVisibility(View.GONE);
        llGroupChat.setVisibility(View.GONE);
    }

    private void setActivate(boolean isActivate){
        if(isActivate){
            llActivate.setVisibility(View.GONE);
        }else{
            llActivate.setVisibility(View.VISIBLE);
        }
    }
}
