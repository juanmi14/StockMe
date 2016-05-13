package stockme.stockme;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import stockme.stockme.persistencia.BDHandler;
import stockme.stockme.util.OpcionesMenus;
import stockme.stockme.util.Preferencias;
import stockme.stockme.util.Util;

public class Principal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Fragment_listas.OnFragmentInteractionListener, Fragment_stock.OnFragmentInteractionListener {

    private static NavigationView nav_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //inicialziar preferencias
        Preferencias.inicializarPreferencias(this);
        //con esto ya podremos usar los métodos estáticos de Preferencias
        crearPreferenciasPorDefecto();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        nav_menu = (NavigationView) findViewById(R.id.nav_view);
        nav_menu.setNavigationItemSelectedListener(this);

        //esto es para obtener la primera instancia de la bd, si no, no se crea la primera vez..
        //seguro q hay una forma mejor de hacerla pero bueh
        BDHandler handler = new BDHandler(this);
        handler.abrir();
        handler.cerrar();

//        Fragment fragmento = new Fragment_listas();
//        getSupportFragmentManager().beginTransaction().replace(R.id.contenido_principal, fragmento).commit();
//        this.setTitle("Listas");
//
//        nav_menu.getMenu().getItem(0).setChecked(true);
    }

    private void crearPreferenciasPorDefecto() {
        Preferencias.setPreferencia("moneda", Util.moneda);
        Preferencias.setPreferencia("anterior", "Listas");
    }

    /*TODO: para la navegabilidad podemos utiliar la traza creada al guardar en Preferencias
    la activity o fragment anterior. En cada activity implementar este método para que regrese a la anterior*/
    @Override
    public void onBackPressed() {
        OpcionesMenus.onBackPressed(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.accion_opciones:
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        boolean fragTransact = false;
        Fragment fragmento = null;

        if (id == R.id.nav_listas) {
            fragmento = new Fragment_listas();
            fragTransact = true;
        } else if (id == R.id.nav_stock) {
            fragmento = new Fragment_stock();
            fragTransact = true;
        } else if (id == R.id.nav_articulos) {
            startActivity(new Intent(this, CatalogoArticulos.class));
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.nav_ajustes) {
            startActivity(new Intent(this, InfoBD.class));
        }


        if(fragTransact){
            getSupportFragmentManager().beginTransaction().replace(R.id.contenido_principal, fragmento).commit();
            item.setChecked(true);
            getSupportActionBar().setTitle(item.getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        if(i != null) {
            Bundle extras = i.getExtras();
            if(extras != null) {
                String opc = extras.getString("Opcion");
                if (opc != null) {
                    //Util.mostrarToast(this, "He seleccionado: " + opc);
                    switch (opc) {
                        case "Listas":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
                            break;
                        case "Stock":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_stock));
                            break;
                        case "Articulos":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_articulos));
                            break;
                        case "Ajustes":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_ajustes));
                            break;
                        default:
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
                            break;
                    }
                }else
                    onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
            }else {
                String anterior = Preferencias.getPreferenciaString("anterior");
                if(anterior != null){
                    switch (anterior) {
                        case "Listas":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
                            break;
                        case "Stock":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_stock));
                            break;
                        case "Articulos":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_articulos));
                            break;
                        case "Ajustes":
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_ajustes));
                            break;
                        default:
                            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
                            break;
                    }
                }else
                    onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
            }
        }else
            onNavigationItemSelected(nav_menu.getMenu().findItem(R.id.nav_listas));
    }
}
