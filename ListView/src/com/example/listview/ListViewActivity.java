
package com.example.listview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListViewActivity extends Activity {
    Button show;
    ListView lv;
    List<Person> persons = new ArrayList<Person>();
    Context mContext;
    MyListAdapter adapter;
    List<Integer> listItemID = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        mContext = getApplicationContext();
        show = (Button)findViewById(R.id.show);
        lv = (ListView)findViewById(R.id.lvperson);

        initPersonData();
        adapter = new MyListAdapter(persons);
        lv.setAdapter(adapter);
        
        show.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listItemID.clear();
                for (int i = 0; i <adapter.mChecked.size() ; i++) {
                    if (adapter.mChecked.get(i)) {
                        listItemID.add(i);
                    }
                }
                
                if (listItemID.size() == 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ListViewActivity.this);
                    builder1.setMessage("没有选中任何记录");
                    builder1.show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    
                    for (int i = 0; i < listItemID.size(); i++) {
                        sb.append("ItemID=" + listItemID.get(i) + " . ");
                    }
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(ListViewActivity.this);
                    builder2.setMessage(sb.toString());
                    builder2.show();
                }
            }
        });
    }
    
    private void initPersonData(){
        Person mPerson;
        for (int i = 1; i <= 100; i++) {
            mPerson = new Person();
            mPerson.setName("Andy" + i);
            mPerson.setAddress("ShangHai" + i);
            persons.add(mPerson);
        }
    }
    
    class MyListAdapter extends BaseAdapter{
        List<Boolean> mChecked;
        List<Person> listPerson;
        HashMap<Integer, View> map = new HashMap<Integer, View>();
        
        public MyListAdapter(List<Person> list){
            listPerson = new ArrayList<Person>();
            listPerson = list;
            
            mChecked = new ArrayList<Boolean>();
            for (int i = 0; i < list.size(); i++) {
                mChecked.add(false);
            }
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listPerson.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return listPerson.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view;
            ViewHolder holder = null;
            
            if (map.get(position) == null) {
                Log.d("ListViewActivity", "position1 = " + position);
                LayoutInflater mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = mInflater.inflate(R.layout.listitem, null);
                holder = new ViewHolder();
                holder.selected = (CheckBox)view.findViewById(R.id.list_select);
                holder.name = (TextView)view.findViewById(R.id.list_name);
                holder.address = (TextView)view.findViewById(R.id.list_address);
                final int p = position;
                map.put(position, view);
                holder.selected.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        CheckBox cb = (CheckBox)v;
                        mChecked.set(p, cb.isChecked());
                    }
                });
                view.setTag(holder);
            }else {
                Log.d("ListViewActivity", "position2 = " + position);
                view = map.get(position);
                holder = (ViewHolder)view.getTag();
            }
            
            holder.selected.setChecked(mChecked.get(position));
            holder.name.setText(listPerson.get(position).getName());
            holder.address.setText(listPerson.get(position).getAddress());
            
            return view;
        }
        
    }
    
    static class ViewHolder{
        CheckBox selected;
        TextView name;
        TextView address;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view, menu);
        return true;
    }

}
