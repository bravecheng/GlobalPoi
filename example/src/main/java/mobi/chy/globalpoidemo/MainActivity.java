package mobi.chy.globalpoidemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mobi.chy.map.globalpoi.entity.GlobalPoi;
import mobi.chy.map.globalpoi.GlobalPoiSearch;

public class MainActivity extends AppCompatActivity implements GlobalPoiSearch.PoiSearchListener, GlobalPoiSearch.PoiDetailListener {

    private EditText etLat;
    private EditText etLng;
    private Button btnRegeo,btnSearch;
    private EditText etKeyword;
    private EditText etCity;
    private Button btnInside, btnOutside;
    private TextView tvRegeo;
    private ListView lvPoi;
    GlobalPoiSearch globalPoiSearch;
    PoiListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etLat = findViewById(R.id.lat);
        etLng = findViewById(R.id.lng);
        btnRegeo = findViewById(R.id.btn_regeo);
        btnSearch = findViewById(R.id.btn_search);

        etKeyword = findViewById(R.id.et_keyword);
        etCity = findViewById(R.id.et_city);
        btnInside = findViewById(R.id.btn_inside);
        btnOutside = findViewById(R.id.btn_outside);

        tvRegeo= findViewById(R.id.tv_regeo);
        lvPoi = findViewById(R.id.lv_poi);

        mAdapter = new PoiListAdapter();
        lvPoi.setAdapter(mAdapter);

        globalPoiSearch = new GlobalPoiSearch(this);
        globalPoiSearch.setOnPoiSearchListener(this);
        globalPoiSearch.setOnPoiDetailListener(this);

        btnRegeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double lat = Double.parseDouble(etLat.getText().toString());
                    double lng = Double.parseDouble(etLng.getText().toString());
                    globalPoiSearch.queryDetail(lat, lng);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入不合法", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    double lat = Double.parseDouble(etLat.getText().toString());
                    double lng = Double.parseDouble(etLng.getText().toString());
                    globalPoiSearch.queryLatLng(lat, lng, 0);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入不合法", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnInside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalPoiSearch.queryAMap(etKeyword.getText().toString(),etCity.getText().toString(), 0);
            }
        });

        btnOutside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalPoiSearch.queryFoursquare(etKeyword.getText().toString(), etCity.getText().toString());
            }
        });
    }

    @Override
    public void onPoiSearchSuccess(int totalCount, List<GlobalPoi> poiList) {
        mAdapter.setItems(poiList);
        lvPoi.setSelection(0);
    }

    @Override
    public void onPoiSearchFailed(int errCode, String errDesc) {
        Toast.makeText(MainActivity.this, errDesc, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPoiSearchFinish() {

    }

    @Override
    public void onPoiDetailSuccess(GlobalPoi itemPoi) {
        tvRegeo.setText("[" + itemPoi.getLocation().getCountry() + "]-"
                + "[" + itemPoi.getLocation().getState() + "]-"
                + "[" + itemPoi.getLocation().getCity() + "]" + itemPoi.getName() +"\n" + itemPoi.getLocation().getAddress());
    }

    @Override
    public void onPoiDetailFailed(int errCode, String errDesc) {

    }

    @Override
    public void onPoiDetailFinish() {

    }


    private class PoiListAdapter extends BaseAdapter {

        private List items;

        public void setItems(List items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_2, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            GlobalPoi itemPoi = (GlobalPoi) getItem(i);
            holder.title.setText("[" + itemPoi.getLocation().getCountry() + "]-"
                    + "[" + itemPoi.getLocation().getState() + "]-"
                    + "[" + itemPoi.getLocation().getCity() + "]" + itemPoi.getName());
            holder.address.setText(itemPoi.getLocation().getAddress());
            return view;
        }
    }

    private class ViewHolder {
        TextView title, address;

        ViewHolder(View view) {
            title = view.findViewById(android.R.id.text1);
            address = view.findViewById(android.R.id.text2);
        }
    }
}
