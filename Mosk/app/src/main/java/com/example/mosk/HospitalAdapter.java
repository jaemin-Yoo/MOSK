package com.example.mosk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HospitalAdapter extends BaseAdapter {
    private static final String TAG = "moskLog";

    private ViewHolder mViewHolder;

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<HospitalViewItem> array_hospital = new ArrayList<>();

    private TextView nameTextView;
    private TextView phnumTextView;
    private TextView adressTextView;

    int total=0;

    public HospitalAdapter() { }

    @Override
    public int getCount() {
        total=array_hospital.size();
        return total;
    }


    @Override

    public Object getItem(int position) {

        return array_hospital.get(position);

    }


    @Override

    public long getItemId(int position) {

        return position;

    }


    @Override

    public View getView(int i, View convertView, ViewGroup parent) {
        final int pos=i;
        final Context context=parent.getContext();

        // ViewHoldr 패턴
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_hospital_item, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        //화면에 표시된 view로 부터 위젯에 대한 참조 획득
        nameTextView=(TextView)convertView.findViewById(R.id.txtname);
        phnumTextView=(TextView) convertView.findViewById(R.id.txtphnum);
        adressTextView=(TextView)convertView.findViewById(R.id.txtadress);

        HospitalViewItem hospitalViewItem=array_hospital.get(i);

        nameTextView.setText(hospitalViewItem.getName());
        phnumTextView.setText(hospitalViewItem.getPhnum());
        adressTextView.setText(hospitalViewItem.getAdress());

        return convertView;

    }

    /* 아이템 데이터 추가를 위한 함수. 자신이 원하는대로 작성 */
    public void addItem(String name, String phnum, String adress) {

        HospitalViewItem mItem = new HospitalViewItem();

        /* MyItem에 아이템을 setting한다. */
        mItem.setName(name);
        mItem.setPhnum(phnum);
        mItem.setAdress(adress);

        /* mItems에 MyItem을 추가한다. */
        array_hospital.add(mItem);

    }

    public void removeAll(){
        array_hospital.clear();
        notifyDataSetChanged();
    }
    public class ViewHolder {

        TextView txt_name,txt_phnum,txt_adress;

        public ViewHolder(View convertView) {

            txt_name= (TextView) convertView.findViewById(R.id.txtname);
            txt_phnum=(TextView) convertView.findViewById(R.id.txtphnum);
            txt_adress=(TextView) convertView.findViewById(R.id.txtadress);


        }

    }
}