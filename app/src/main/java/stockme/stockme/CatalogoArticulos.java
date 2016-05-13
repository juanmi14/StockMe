package stockme.stockme;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import stockme.stockme.logica.Articulo;
import stockme.stockme.util.OpcionesMenus;
import stockme.stockme.util.Preferencias;
import stockme.stockme.util.Util;

public class CatalogoArticulos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Fragment_listas.OnFragmentInteractionListener, Fragment_catalogo_todos.OnFragmentInteractionListener, Fragment_catalogo_tipos.OnFragmentInteractionListener {
    private GridView articulos;
    private Button aniadir;
    private ImageButton btn_reset;
    private EditText etBusqueda;

    private ViewPager vpPager;
    private MyPagerAdapter adapterViewPager;

    private Toolbar toolbar;

    private static NavigationView nav_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo_articulos);
        Preferencias.setPreferencia("anterior", "Artículos");

        //toolbar + navbar
        View vToolbar = findViewById(R.id.toolbar_catalog);

        vpPager = (ViewPager) findViewById(R.id.pager_catalogo);

        etBusqueda = (EditText) vToolbar.findViewById(R.id.et_busqueda);
        etBusqueda.setVisibility(View.GONE);
        etBusqueda.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                realizarBusqueda(event, false);
                return true;
            }
        });
//        etBusqueda.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(event.getAction() == KeyEvent.ACTION_UP)
//                    Util.mostrarToast(getApplicationContext(), ((EditText)v).getText().toString());
//                return false;
//            }
//        });

        toolbar = (Toolbar) vToolbar;
        setSupportActionBar(toolbar);
        this.setTitle("Catálogo");

//        querySearch = null;//para resetear la búsqueda al entrar en Artículos

        Intent i = getIntent();
        if (i != null){
            if (i.getExtras() != null){
                String viene = i.getExtras().getString("vieneDe");
                if(viene != null)
                    Util.vieneDe = viene;
            }
        }



        aniadir = (Button) findViewById(R.id.fragment_catalogo_btn_mas);

        aniadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ArticuloSimpleAdd.class);
                startActivityForResult(i,1);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        });

        btn_reset = (ImageButton)findViewById(R.id.fragment_catalogo_btn_reset_search);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarBusqueda(null, true);
            }
        });

        //handleIntent(getIntent());
    }

    private void realizarBusqueda(KeyEvent event, boolean reset) {
        if(event != null && event.getAction() == KeyEvent.ACTION_UP) {
            String texto = etBusqueda.getText().toString();
            texto = texto != null && !texto.isEmpty() ? texto : null;
            adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
            adapterViewPager.setQuerySearch(texto);
            vpPager.setAdapter(adapterViewPager);
            //etBusqueda.setText("");
        }
        if(reset){
            adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
            adapterViewPager.setQuerySearch(null);
            vpPager.setAdapter(adapterViewPager);
            etBusqueda.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if(etBusqueda.getVisibility() == View.GONE) {
                    etBusqueda.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_action_cancel_dark);
                    etBusqueda.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(findViewById(R.id.et_busqueda), 0);
                }
                else {
                    etBusqueda.setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_action_search_dark);
                    etBusqueda.setText(null);
                    realizarBusqueda(new KeyEvent(KeyEvent.ACTION_UP, 0), false);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etBusqueda.getWindowToken(), 0);
                }
                return true;
            case R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")) {
            int id = item.getItemId();

            if(id != R.id.nav_articulos) {
                Intent i = new Intent(this, Principal.class);

                if (id == R.id.nav_listas) {
                    i.putExtra("Opcion", "Listas");
                } else if (id == R.id.nav_stock) {
                    i.putExtra("Opcion", "Stock");
                } else if (id == R.id.nav_ajustes) {
                    i.putExtra("Opcion", "Ajustes");
                }
    //
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                startActivity(i);
                overridePendingTransition(0, 0);

                finish();
            }else{
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        nav_menu = (NavigationView) findViewById(R.id.nav_view);
        if(Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")){
            drawer.setDrawerListener(null);
            nav_menu.setNavigationItemSelectedListener(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else {
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            nav_menu.setNavigationItemSelectedListener(this);

            nav_menu.getMenu().getItem(2).setChecked(true);
        }

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
    }

    //TODO: quizá crear dos clases, la básica y la de búsqueda
    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        //Número de páginas
        private static int NUM_ITEMS = 2;

        private String querySearch;
        public void setQuerySearch(String querySearch){
            this.querySearch = querySearch;
        }

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Devolver el total de páginas
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Crear el fragment a devolver en cada página
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Fragment_catalogo_todos ft = new Fragment_catalogo_todos();
                    if(querySearch != null)
                        ft.setQuerySearch(querySearch);
                    else
                        ft.setQuerySearch(null);
                    return ft;
                case 1:
                    Fragment_catalogo_tipos fti = new Fragment_catalogo_tipos();
                    if(querySearch != null)
                        fti.setQuerySearch(querySearch);
                    else
                        fti.setQuerySearch(null);
                    return fti;
                default:
                    Fragment_catalogo_todos ftd = new Fragment_catalogo_todos();
                    if(querySearch != null)
                        ftd.setQuerySearch(querySearch);
                    else
                        ftd.setQuerySearch(null);
                    return ftd;
            }
        }

        // Nombre de cada página
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Todos";
                case 1:
                    return "Tipos";
                default:
                    return "";
            }
        }

    }

    @Override
    public void onArticuloSeleccionado(Articulo articulo) {
        if(Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")){
            Intent i = new Intent();
            i.putExtra("Articulo",articulo);
            setResult(1,i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")){
            finish();
        }else
            OpcionesMenus.onBackPressed(this);
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        handleIntent(intent);
//        finish();
//    }
//
//    private void handleIntent(Intent intent) {
//        String accion = intent.getAction();
//        if (Intent.ACTION_SEARCH.equals(accion)) {
//            querySearch = intent.getStringExtra(SearchManager.QUERY);
//            if(querySearch != null && querySearch.isEmpty())
//                querySearch = null;
//        }
//    }
}
