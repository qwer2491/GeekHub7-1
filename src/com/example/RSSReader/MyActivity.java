package com.example.RSSReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MyActivity extends ActionBarActivity {
    public static final String log = "createLog";
    private static final String OBJECT1 = "PREF";
    URL url = null;
    ArrayList<RssTags> arrayList = null;
    MenuItem tabletLike;
    private static final int TABLET_LIKE_ITEM = 100;
    int globalVariable;
    Menu menu;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public ArrayList<RssTags> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<RssTags> arrayList) {
        this.arrayList = arrayList;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        try {
            url = new URL(
                    "http://news.rambler.ru/rss/head/"

            );
        } catch (MalformedURLException e) {
            Log.d(log, e.toString());
            e.printStackTrace();
        }


        try {
            arrayList = new ParseXml().execute(url).get();
            Log.d(log, "Checking the size of an array: " + arrayList.size());


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        String titles[] = new String[arrayList.size()];

        for (int i = 0; i < arrayList.size(); i++) {
            titles[i] = arrayList.get(i).getTitle();
        }

        final ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        if (getResources().getConfiguration().orientation == 1 & getResources().getBoolean(R.bool.istablet)) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            listView.setLayoutParams(layoutParams);
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(arrayList.get(0).toStringWithOutTitle());
            textView.setTextSize(30);
        }
        if (getResources().getConfiguration().orientation == 2 & getResources().getBoolean(R.bool.istablet)) {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(arrayList.get(0).toStringWithOutTitle());
            textView.setTextSize(30);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String showWhatWeGot = (String) listView.getItemAtPosition(position);
                Toast.makeText(getBaseContext(), showWhatWeGot, Toast.LENGTH_SHORT).show();

                if (getResources().getConfiguration().orientation == 2 & getResources().getBoolean(R.bool.istablet)) {
                    globalVariable = position;
                    MenuItem tabletLike = menu.findItem(TABLET_LIKE_ITEM);
                    if (checkElement(getApplicationContext(), arrayList.get(position).getTitle())) {
                        tabletLike.setIcon(R.drawable.ic_action_rating_bad);
                        tabletLike.setTitle(R.string.item_item2);
                    } else {
                        tabletLike.setIcon(R.drawable.ic_action_rating_good);
                        tabletLike.setTitle(R.string.item2);
                    }
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText(arrayList.get(position).toStringWithOutTitle());
                } else {
                    Intent intent = new Intent(getApplicationContext(), NewActivity.class);
                    intent.putExtra("sendElement", getInfo(arrayList.get(position)));
                    startActivity(intent);
                }
            }
        });

        SharedPreferences settings = getSharedPreferences(OBJECT1, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastPubDate", arrayList.get(0).getPubDate());
        editor.putString("url", getUrl().toString());
        editor.commit();
        Log.d(log, "+ arrayList.get(0).getPubDate(): " + arrayList.get(0).getPubDate());

    }

    void deleteAllDatabase(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AnnouncementOfDatabase mDbContract = new AnnouncementOfDatabase();
                AnnouncementOfDatabase.FeedReaderDbHelper mDbHelper = mDbContract.new FeedReaderDbHelper(context);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                Log.d(log, "Checking delete the table");
                int clearCount = db.delete(AnnouncementOfDatabase.FeedEntry.TABLE_NAME, null, null);
                Log.d(log, "deleted rows count = " + clearCount);
                db.close();
            }
        });

    }

    public String[] getInfo(RssTags rssTags) {
        return new String[]{rssTags.getDescription(), rssTags.getLink(), rssTags.getPubDate(), rssTags.getTitle()};
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (getResources().getConfiguration().orientation == 2 & getResources().getBoolean(R.bool.istablet)) {
            if (checkElement(getApplicationContext(), arrayList.get(globalVariable).getTitle())) {
                menu.add(Menu.NONE, TABLET_LIKE_ITEM, Menu.FIRST, R.string.item_item2)
                        .setIcon(R.drawable.ic_action_rating_bad)
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                menu.add(Menu.NONE, TABLET_LIKE_ITEM, Menu.FIRST, R.string.item2)
                        .setIcon(R.drawable.ic_action_rating_good)
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
        this.menu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                stopService(new Intent(this, NotifService.class));
                Toast.makeText(this, "You have clicked on the button 'Stop Service'", Toast.LENGTH_SHORT).show();
                break;

            case R.id.item3:
                startActivity(new Intent(this, FavoriteElements.class));
                break;
            case R.id.item4:
                deleteAllDatabase(getApplicationContext());
                break;
            case TABLET_LIKE_ITEM:
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_action_rating_good).getConstantState())) {
                    Toast.makeText(this, "You put 'like'", Toast.LENGTH_SHORT).show();
                    addElement(getApplicationContext());
                    item.setIcon(R.drawable.ic_action_rating_bad);
                    item.setTitle(R.string.item_item2);
                    break;
                } else {
                    Toast.makeText(this, "You put 'dislike'", Toast.LENGTH_SHORT).show();
                    deleteElement(getApplicationContext());
                    item.setIcon(R.drawable.ic_action_rating_good);
                    item.setTitle(R.string.item2);
                    break;
                }
        }
        return true;
    }


    void addElement(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AnnouncementOfDatabase mDbContract = new AnnouncementOfDatabase();
                AnnouncementOfDatabase.FeedReaderDbHelper mDbHelper = mDbContract.new FeedReaderDbHelper(context);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(AnnouncementOfDatabase.FeedEntry.COLUMN_TITLE, arrayList.get(globalVariable).getTitle());
                values.put(AnnouncementOfDatabase.FeedEntry.COLUMN_DESCRIPTION, arrayList.get(globalVariable).getDescription());
                values.put(AnnouncementOfDatabase.FeedEntry.COLUMN_LINK, arrayList.get(globalVariable).getLink());
                values.put(AnnouncementOfDatabase.FeedEntry.COLUMN_PUBDATE, arrayList.get(globalVariable).getPubDate());


                long newRowId;
                newRowId = db.insert(
                        AnnouncementOfDatabase.FeedEntry.TABLE_NAME,
                        null,
                        values);
                Log.d(log, "Add a new line in DB : " + newRowId);
                db.close();
            }
        });

    }

    void deleteElement(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AnnouncementOfDatabase mDbContract = new AnnouncementOfDatabase();
                AnnouncementOfDatabase.FeedReaderDbHelper mDbHelper = mDbContract.new FeedReaderDbHelper(context);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                long newRowId;
                newRowId = db.delete(
                        AnnouncementOfDatabase.FeedEntry.TABLE_NAME,
                        AnnouncementOfDatabase.FeedEntry.COLUMN_TITLE + " = ?",
                        new String[]{arrayList.get(globalVariable).getTitle()});
                Log.d(log, "Add a new line in DB : " + newRowId);
                db.close();
            }
        });

    }

    boolean checkElement(final Context context, final String checkTitle) {
        Log.d(log, "Check Title: " + checkTitle);
        final boolean[] exists = new boolean[1];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exists[0] = false;
                AnnouncementOfDatabase mDbContract = new AnnouncementOfDatabase();
                AnnouncementOfDatabase.FeedReaderDbHelper mDbHelper = mDbContract.new FeedReaderDbHelper(context);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String[] projection = {
                        AnnouncementOfDatabase.FeedEntry._ID,
                        AnnouncementOfDatabase.FeedEntry.COLUMN_TITLE,
                        AnnouncementOfDatabase.FeedEntry.COLUMN_DESCRIPTION,
                        AnnouncementOfDatabase.FeedEntry.COLUMN_LINK,
                        AnnouncementOfDatabase.FeedEntry.COLUMN_PUBDATE
                };
                String selection = AnnouncementOfDatabase.FeedEntry.COLUMN_TITLE + "=?";
                String selectionArgs[] = {arrayList.get(globalVariable).getTitle()};
                Log.d(log,  "SelectionArgs massive: " + selectionArgs[0]);
                Cursor cursor = db.query(
                        AnnouncementOfDatabase.FeedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                if (cursor.moveToFirst()) {
                    do {
                        if (checkTitle.equals(cursor.getString(cursor.getColumnIndex(AnnouncementOfDatabase.FeedEntry.COLUMN_TITLE)))) {
                            exists[0] = true;
                            Log.d(log, "Exist: " + exists[0]);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                db.close();
            }
        });
        Log.d(log, "Exist: = " + exists[0]);
        return exists[0];
    }

}
