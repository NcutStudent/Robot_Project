package com.example.dickman.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.dickman.myapplication.Util.*;

public class SelectContact extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    List<Pair<String, String>> iconData = new ArrayList<>();

    static PhoneAnswerListener.LocalBinder binder;
    String pas;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            binder = (PhoneAnswerListener.LocalBinder) service;
            if(pas != null) {
                binder.getService().setBitmapPath(getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).getString(
                        pas, null));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        Map<String, ?> data = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry : data.entrySet())
        {
            iconData.add(new Pair<String, String>(entry.getKey(), String.valueOf(entry.getValue())));
        }

        pas = getSharedPreferences(TEMP_FILE, MODE_PRIVATE).getString(USER_PASSWORD, null);
        if(binder == null) {
            Intent phoneListenerIntent = new Intent(this, PhoneAnswerListener.class);
            bindService(phoneListenerIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), iconData);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if(pas != null) {
            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(USER_PASSWORD, pas);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        binder = null;
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_contact, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_CODE && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String override = bundle.getString(OVERRIDE);
            if(override.equals(YES_OVERRIDE)){
                SharedPreferences sharedPreferences_remove = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                int i = iconData.indexOf(bundle.getString(USER_ID));
                Pair<String, String> data_remove = iconData.remove(i);
                sharedPreferences_remove.edit().remove(data_remove.first).apply();
                //(new File(data_remove.second)).delete();
                mSectionsPagerAdapter.notifyDataSetChanged();
            }

            String id = bundle.getString(USER_ID);
            String imgName = bundle.getString(IMAGE_PATH);
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(id, imgName).apply();
            iconData.add(new Pair<String, String>(id, imgName));
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_new_contact) {
            Intent intent = new Intent(this, AddContact.class);
            startActivityForResult(intent, ADD_CONTACT_CODE);
            return true;
        } else if(id == R.id.delete_contact) {
            int i = mViewPager.getCurrentItem();
            if(iconData.size() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.id_is_delete_warning);
                builder.setNegativeButton(R.string.ok,null);
                builder.show();
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                Pair<String, String> data = iconData.remove(i);
                sharedPreferences.edit().remove(data.first).apply();
                (new File(data.second)).delete();
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        Context context ;
        public PlaceholderFragment() {
        }

        public void setContext(Context context) {
            this.context = context;
        }

        private Pair<String, String> iconInfo;

        public static PlaceholderFragment newInstance(Context context, int sectionNumber, Pair<String, String> iconInfo) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setContext(context);
            fragment.iconInfo = iconInfo;
            return fragment;
        }

        View.OnClickListener callButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.getService().restartListening(v.getTag().toString());
                Intent intent = new Intent(context, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(USER_PASSWORD, iconInfo.first);
                bundle.putString(USER_ICON_PATH, iconInfo.second);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_select_contact, container, false);
            ImageView imageView = rootView.findViewById(R.id.icon);
            TextView textView = rootView.findViewById(R.id.tvID);

            Bitmap bmp = BitmapFactory.decodeFile(iconInfo.second);
            imageView.setImageBitmap(bmp);

            textView.setText(iconInfo.first);
            Button button = rootView.findViewById(R.id.callButton);
            button.setTag(iconInfo.first);
            button.setOnClickListener(callButtonListener);
            return rootView;
        }


        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<Pair<String, String>> iconList;
        Context context;
        public SectionsPagerAdapter(Context context, FragmentManager fm, List<Pair<String, String>> iconList) {
            super(fm);
            this.iconList = iconList;
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(context, position + 1, iconList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position); //TODO
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return iconList.size();
        }
    }
}
