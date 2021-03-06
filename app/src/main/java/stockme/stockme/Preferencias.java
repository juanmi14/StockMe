package stockme.stockme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import stockme.stockme.util.Configuracion;
import stockme.stockme.util.OpcionesMenus;
import stockme.stockme.util.Util;

public class Preferencias extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static NavigationView nav_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferencias);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        nav_menu = (NavigationView) findViewById(R.id.nav_view);
        nav_menu.setNavigationItemSelectedListener(this);

        nav_menu.getMenu().getItem(3).setChecked(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PreferenciasFragment())
                .commit();
    }

    public void onBackPressed() {
        OpcionesMenus.onBackPressed(this);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id != R.id.nav_ajustes) {
            Intent i;
            if (id == R.id.nav_articulos)
                i = new Intent(this, CatalogoArticulos.class);
            else
                i = new Intent(this, Principal.class);

            if (id == R.id.nav_listas) {
                Configuracion.setPreferencia("anterior", "Listas");
            } else if (id == R.id.nav_stock) {
                Configuracion.setPreferencia("anterior", "Stock");
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
    }


}
