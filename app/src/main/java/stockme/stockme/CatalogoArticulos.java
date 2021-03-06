package stockme.stockme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import stockme.stockme.logica.Articulo;
import stockme.stockme.util.Configuracion;
import stockme.stockme.util.OpcionesMenus;
import stockme.stockme.util.Util;

public class CatalogoArticulos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Fragment_listas.OnFragmentInteractionListener, Fragment_catalogo_todos.OnFragmentInteractionListener, Fragment_catalogo_tipos.OnFragmentInteractionListener {
    private static NavigationView nav_menu;
    private GridView articulos;
    private Button aniadir;
    private ImageButton btn_reset;
    private EditText etBusqueda;
    private ViewPager vpPager;
    private MyPagerAdapter adapterViewPager;
    private Toolbar toolbar;

    private ShowcaseView showcaseView;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo_articulos);
//        Configuracion.setPreferencia("anterior", "Artículos");

        //toolbar + navbar
        final View vToolbar = findViewById(R.id.toolbar_catalog);

        vpPager = (ViewPager) findViewById(R.id.pager_catalogo);


        etBusqueda = (EditText) vToolbar.findViewById(R.id.et_busqueda);
        etBusqueda.setVisibility(View.GONE);
        etBusqueda.setSingleLine();
        etBusqueda.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                realizarBusqueda(event, false);
                return true;
            }
        });

        toolbar = (Toolbar) vToolbar;
        setSupportActionBar(toolbar);
        this.setTitle(getResources().getString(R.string.Catalogo));

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

        //TUTORIAL

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("tutoArticulo", false)) {

            showcaseView = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(vpPager))
                    .setContentText(getResources().getString(R.string.Tuto_articulo1))
                    .setStyle(R.style.ShowcaseTheme)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (counter < 5) {
                                switch (counter) {
                                    case 0:
                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                                        showcaseView.setButtonPosition(params);
                                        showcaseView.setShowcase(new ViewTarget(aniadir), true);
                                        showcaseView.setContentText(getResources().getString(R.string.Tuto_articulo2));
                                        break;

                                    case 1:
                                        showcaseView.setShowcase(new ViewTarget(vToolbar), true);
                                        showcaseView.setContentText(getResources().getString(R.string.Tuto_articulo3));
                                        break;

                                    case 2:
                                        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                        params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                                        showcaseView.setButtonPosition(params1);
                                        showcaseView.setShowcase(new ViewTarget(btn_reset), true);
                                        showcaseView.setContentText(getResources().getString(R.string.Tuto_articulo4));
                                        break;

                                    case 3:
                                        showcaseView.setTarget(Target.NONE);
                                        showcaseView.setContentText(getResources().getString(R.string.Tuto_articulo5));
                                        showcaseView.setButtonText(getString(R.string.Aceptar));
                                        break;

                                    case 4:
                                        showcaseView.hide();
                                        break;
                                }
                                counter++;
                            }
                        }
                    })
                    .build();
            showcaseView.setButtonText(getString(R.string.Aceptar));

            prefs.edit().putBoolean("tutoArticulo", true).apply();
        }
    }

    private void realizarBusqueda(KeyEvent event, boolean reset) {
        String texto = etBusqueda.getText().toString();
        texto = texto != null && !texto.isEmpty() ? texto : null;
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), this);
        adapterViewPager.setQuerySearch(texto);
        vpPager.setAdapter(adapterViewPager);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etBusqueda.getWindowToken(), 0);
        if(reset){
            adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), this);
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
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (!Util.vieneDe.equals("articulosAdd") && !Util.vieneDe.equals("stockAdd")) {
            Configuracion.setPreferencia("anterior", "Artículos");
            int id = item.getItemId();

            if(id != R.id.nav_articulos) {
                Intent i;
                if (id == R.id.nav_ajustes)
                    i = new Intent(this, Preferencias.class);
                else
                    i = new Intent(this, Principal.class);

                if (id == R.id.nav_listas) {
                    Configuracion.setPreferencia("anterior", "Listas");
                } else if (id == R.id.nav_stock) {
                    Configuracion.setPreferencia("anterior", "Stock");
                }

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
        ActionBarDrawerToggle toggle;
        nav_menu = (NavigationView) findViewById(R.id.nav_view);
        if(Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")){
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            nav_menu.setNavigationItemSelectedListener(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else {
            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//            toggle.setDrawerIndicatorEnabled(true);
            drawer.setDrawerListener(toggle);
//            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.syncState();

            nav_menu.setNavigationItemSelectedListener(this);

            nav_menu.getMenu().getItem(2).setChecked(true);
        }

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), this);
        vpPager.setAdapter(adapterViewPager);
    }

    @Override
    public void onArticuloSeleccionado(Articulo articulo) {
        if (Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")) {
            Intent i = new Intent();
            i.putExtra("Articulo", articulo);
            setResult(1, i);
            finish();
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        }
    }

    @Override
    public void onBackPressed() {
        if(Util.vieneDe.equals("articulosAdd") || Util.vieneDe.equals("stockAdd")){
            finish();
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        }else
            OpcionesMenus.onBackPressed(this);
    }

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        //Número de páginas
        private static int NUM_ITEMS = 2;
        private Context context;

        private String querySearch;

        public MyPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
        }

        public void setQuerySearch(String querySearch) {
            this.querySearch = querySearch;
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
                    if (querySearch != null)
                        ft.setQuerySearch(querySearch);
                    else
                        ft.setQuerySearch(null);
                    return ft;
                case 1:
                    Fragment_catalogo_tipos fti = new Fragment_catalogo_tipos();
                    if (querySearch != null)
                        fti.setQuerySearch(querySearch);
                    else
                        fti.setQuerySearch(null);
                    return fti;
                default:
                    Fragment_catalogo_todos ftd = new Fragment_catalogo_todos();
                    if (querySearch != null)
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
                    return context.getResources().getString(R.string.Todos);
                case 1:
                    return context.getResources().getString(R.string.Tipos);
                default:
                    return "";
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
