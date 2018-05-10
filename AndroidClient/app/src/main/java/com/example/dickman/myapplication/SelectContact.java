package com.example.dickman.myapplication;

import android.app.Activity;
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
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.example.dickman.myapplication.Util.*;


public class SelectContact extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */


    /**
     * The {@link ViewPager} that will host the section contents.
     */
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Map<String, ?> data = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry : data.entrySet())
        {
            iconData.add(new Pair<String, String>(entry.getKey(), (String) entry.getValue()));
        }

        pas = getSharedPreferences("settings", MODE_PRIVATE).getString("password", null);
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
            bundle.putString("password", pas);
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
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            int i = mViewPager.getCurrentItem();
            Pair<String, String> data = iconData.remove(i);
             sharedPreferences.edit().remove(data.first).apply();
            (new File(data.second)).delete();
            mSectionsPagerAdapter.notifyDataSetChanged();
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_select_contact, container, false);
            ImageView imageView = rootView.findViewById(R.id.icon);
            Bitmap bmp = BitmapFactory.decodeFile(iconInfo.second);
            imageView.setImageBitmap(bmp);
            Button button = rootView.findViewById(R.id.callButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("password", iconInfo.first);
                    bundle.putString("Bitmap Path", iconInfo.second);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
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
