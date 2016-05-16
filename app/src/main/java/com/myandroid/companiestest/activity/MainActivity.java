package com.myandroid.companiestest.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.myandroid.companiestest.adapter.CompanyAdapter;
import com.myandroid.companiestest.DBHelper;
import com.myandroid.companiestest.R;
import com.myandroid.companiestest.entity.Company;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*
    Каталог компаний с поиском : Название, описание, адресс, координаты.
    Добавление компании: - ввод деталий - адреса - поиск по гурлу коориднат
    - возмоноть пожправить результат - перенести маркер и сохранить.
    При открытии копании - показать мини карту - с возможностью открыть гугл карты для навигации к ней.
     */
    private static final String LOG_TAG = "MainActivity";
    private ListView listCompanies;
    private EditText editTextSearch;
    private Button buttonDeleteText;

    private CompanyAdapter adapter;
    private List<Company> companiesNames;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AddCompanyActivity.class));
            }
        });

        dbHelper = new DBHelper(this);
        initViews();

        //fillWithTestValues();
        editTextSearchListener();
        fillListCompanies();
        setOnNameClickListener();
        setOnNameLongClickListener();
        buttonDeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
            }
        });
    }

    private void initViews() {
        listCompanies = (ListView) findViewById(R.id.listView);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        buttonDeleteText = (Button) findViewById(R.id.delete_all_text);

    }

    private void fillWithTestValues() {
        dbHelper.insertCompany("Company1", "Description1");
        dbHelper.insertCompany("Company2", "Description2");
        dbHelper.insertCompanyAddress(1, "вул. Хрещатик, 29, Київ", 50.4432054, 30.519168);
        dbHelper.insertCompanyAddress(2, "вул. Академіка Янгеля, 20, Київ", 50.4481523,30.449395);

    }

    private void fillListCompanies() {
        Cursor c = dbHelper.getAllCompanies();
        companiesNames = new ArrayList<>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Log.v(LOG_TAG, "c.moveToNext()");
                    Company company = new Company();
                    company.setId(c.getInt(c.getColumnIndex(dbHelper.COMPANIES_UID)));
                    company.setName(c.getString(c.getColumnIndex(dbHelper.COMPANIES_NAME)));

                    companiesNames.add(company);
                } while (c.moveToNext());
            }
            c.close();
        }
        adapter = new CompanyAdapter(this, R.layout.list_company_item, companiesNames);
        listCompanies.setAdapter(adapter);
    }

    private void setOnNameClickListener() {
        listCompanies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Company company = (Company) parent.getItemAtPosition(position);
                Log.v(LOG_TAG, "nameCompany = " + company.getName());

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("idCompany", company.getId());
                startActivity(intent);
            }
        });
    }

    private void setOnNameLongClickListener(){
        listCompanies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Company company = (Company) parent.getItemAtPosition(position);
                                dbHelper.deleteCompanyById(company.getId());
                                companiesNames.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .show();
                return true;
            }
        });

    }

    private void editTextSearchListener() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isVisiblebuttonDeleteText(s);
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isVisiblebuttonDeleteText(CharSequence s) {
        if (s.toString().trim().length() == 0)
            buttonDeleteText.setVisibility(View.INVISIBLE);
        else
            buttonDeleteText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
