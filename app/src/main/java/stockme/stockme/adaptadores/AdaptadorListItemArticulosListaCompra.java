package stockme.stockme.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import stockme.stockme.R;
import stockme.stockme.logica.Articulo;
import stockme.stockme.logica.ArticuloSupermercado;
import stockme.stockme.logica.Lista;
import stockme.stockme.logica.ListaArticulo;
import stockme.stockme.persistencia.BDHandler;
import stockme.stockme.util.Util;

public class AdaptadorListItemArticulosListaCompra extends ArrayAdapter<ArticuloSupermercado> {
    //atributos static para gestionar el precio. Necesario al no pertenecer los controles a esta vista
    private static TextView tv_precio_total;
    private static TextView tv_precio_compra;
    private static Map<Integer, Float> costes = new TreeMap<>();
    //para el diálogo
    View vistaPrecio;
    private DynamicListView articulos;
    private List<ArticuloSupermercado> datos;
    private Lista lista;
    private Articulo articulo;
    private ArticuloSupermercado articuloSupermercado;
    private TextView lblNombre;
    private TextView lblSupermercado;
    private TextView lblMarca;
    private TextView lblPrecio;
    private TextView lblCantidad;
    private ImageButton btnMas;
    private ImageButton btnMenos;
    private CheckBox cbComprado;

    public AdaptadorListItemArticulosListaCompra(Context context, List<ArticuloSupermercado> datos, Lista lista) {
        super(context, R.layout.listitem_articulos_lista_compra, datos);
        this.datos = datos;
        this.lista = lista;
    }

    public static TextView getTv_precio_total() {
        return tv_precio_total;
    }

    public static void setTv_precio_total(TextView tv_precio_total) {
        AdaptadorListItemArticulosListaCompra.tv_precio_total = tv_precio_total;
    }

    public static TextView getTv_precio_compra() {
        return tv_precio_total;
    }
    public static void setTv_precio_compra(TextView tv_precio_compra) {
        AdaptadorListItemArticulosListaCompra.tv_precio_compra = tv_precio_compra;
    }

    private static void addCoste(Integer id, Float coste){
        if(isCoste(id)) {
            costes.put(id, getCoste(id) + coste);//actualiza su valor
        }else
            costes.put(id, coste);
    }

    private static void delCoste(Integer id){
        costes.remove(id);
    }

    private static Float getCoste(Integer id){
        return costes.get(id);
    }

    private static boolean isCoste(Integer id){
        return costes.containsKey(id);
    }

    public static void resetCostes(){
        costes.clear();
    }

    private static Float getTotalCostes(){
        Float total = 0.0f;
        for(Float f: costes.values())
            total += f;
        return round(total, 2);
//        return roundTwoDecimals(total);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private void actualizarPrecioTotal() {
        BDHandler manejador = new BDHandler(getContext());
        tv_precio_total.setText(String.valueOf(round(manejador.obtenerPrecioTotal(lista.getNombre()), 2)));
        manejador.cerrar();
    }

    private void actualizarPrecioCompra() {
        tv_precio_compra.setText(String.valueOf(getTotalCostes()));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View item = inflater.inflate(R.layout.listitem_articulos_lista_compra, null);
        BDHandler manejador = new BDHandler(getContext());

        //pintar los elementos de forma intercalada
        if ( position % 2 == 1) {
            item.setBackgroundColor((getContext().getResources().getColor(R.color.impar)));
        }

        //se crea un elemento ArticuloSupermercado que contiene los datos de la fila
        articuloSupermercado = manejador.obtenerArticuloSupermercado(datos.get(position).getId());
        articulo = manejador.obtenerArticulo(articuloSupermercado.getArticulo());

        lblNombre = (TextView)item.findViewById(R.id.listitem_articulos_nombre);
        lblNombre.setText(articulo.getNombre());

        lblMarca = (TextView)item.findViewById(R.id.listitem_articulos_marca);
        lblMarca.setText(articulo.getMarca());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        TextView moneda = (TextView) item.findViewById(R.id.listitem_articulos_moneda);
        String mon = prefs.getString("moneda", "€");
        moneda.setText(mon);

        lblSupermercado = (TextView)item.findViewById(R.id.listitem_articulos_superm);
        lblSupermercado.setText(articuloSupermercado.getSupermercado());

        lblPrecio = (TextView)item.findViewById(R.id.listitem_articulos_tv_precio);
        lblPrecio.setText(Float.toString(articuloSupermercado.getPrecio()));

        int cantidad = manejador.obtenerCantidadArticuloEnLista(articuloSupermercado.getId(), lista);

        lblCantidad = (TextView)item.findViewById(R.id.listitem_articulos_cantidad);
        lblCantidad.setText(Integer.toString(cantidad));

        cbComprado = (CheckBox)item.findViewById(R.id.listitem_articulos_cb_comprado);
        cbComprado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BDHandler manejador = new BDHandler(getContext());
                int idArticulo = datos.get(position).getId();
                ArticuloSupermercado articuloSuper = manejador.obtenerArticuloSupermercado(idArticulo);
                CheckBox cb = (CheckBox)v;
                if(cb.isChecked()){
                    ListaArticulo la = manejador.obtenerListaArticulo(articuloSuper.getId(), lista.getNombre());
                    int numArt = la.getCantidad();

                    manejador.modificarArticuloEnLista(new ListaArticulo(articuloSuper.getId(), lista.getNombre(), 0));

                    Articulo articulo = manejador.obtenerArticulo(articuloSuper.getArticulo());

                    Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.Comprado) + articulo.getNombre());

                    addCoste(articulo.getId(), articuloSuper.getPrecio() * numArt);
                }else{
                    manejador.modificarArticuloEnLista(new ListaArticulo(articuloSuper.getId(), lista.getNombre(), 1));
                    if(isCoste(idArticulo))
                        delCoste(idArticulo);
                }
                manejador.cerrar();
                actualizarPrecioCompra();
                actualizarPrecioTotal();
                notifyDataSetChanged();
            }
        });

        if (cantidad == 0) {
            item.setBackgroundColor((getContext().getResources().getColor(R.color.comprado)));
            cbComprado.setChecked(true);
        }

        btnMas = (ImageButton)item.findViewById(R.id.listitem_articulos_mas);
        btnMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BDHandler manejador = new BDHandler(getContext());
                int idArticulo = datos.get(position).getId();
                int cantidad = manejador.obtenerCantidadArticuloEnLista(idArticulo, lista);

                if (cantidad < 99) {
                    manejador.modificarArticuloEnLista(new ListaArticulo(idArticulo, lista.getNombre(), cantidad + 1));
                    if(isCoste(idArticulo))
                        delCoste(idArticulo);
                }

                manejador.cerrar();
                actualizarPrecioCompra();
                actualizarPrecioTotal();
                notifyDataSetChanged();
            }
        });

        btnMenos = (ImageButton)item.findViewById(R.id.listitem_articulos_menos);
        btnMenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BDHandler manejador = new BDHandler(getContext());
                int idArticulo = datos.get(position).getId();
                int cantidad = manejador.obtenerCantidadArticuloEnLista(idArticulo, lista);

                if (cantidad > 0) {
                    manejador.modificarArticuloEnLista(new ListaArticulo(idArticulo, lista.getNombre(), cantidad - 1));
                    addCoste(idArticulo, datos.get(position).getPrecio());
                }
                manejador.cerrar();
                actualizarPrecioTotal();
                actualizarPrecioCompra();
                notifyDataSetChanged();
            }
        });

        articulos = (DynamicListView)parent.findViewById(R.id.lista_compra_lista);

        articulos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {
                final BDHandler manejador = new BDHandler(getContext());
                final ArticuloSupermercado articulo = ((ArticuloSupermercado) parent.getItemAtPosition(pos));

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle(getContext().getResources().getString(R.string.Nuevo_precio));
                vistaPrecio = LayoutInflater.from(view.getContext()).inflate(R.layout.dialogo_cambiar_precio, parent,false);
                builder.setView(vistaPrecio);

                final TextView txActual = (TextView)vistaPrecio.findViewById(R.id.dialogo_cambiar_precio_tv_actual);
                final EditText input = (EditText)vistaPrecio.findViewById(R.id.dialogo_cambiar_precio_et_precio);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

                String actual = view.getContext().getResources().getString(R.string.Precio_actual) + ": " + String.valueOf(round(datos.get(pos).getPrecio(), 2)) + " " + prefs.getString("moneda", "€");
                txActual.setText(actual);

                builder.setPositiveButton(view.getContext().getResources().getString(R.string.Aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nPrecio = input.getText().toString();
                        float precio = 0;
                        if(!nPrecio.isEmpty()){
                            precio = Float.parseFloat(nPrecio);
                        }
                        manejador.modificarArticuloSupermercadoPrecio(articulo, precio);
                        datos.get(pos).setPrecio(precio);
                        actualizarPrecioTotal();
                        actualizarPrecioCompra();
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(view.getContext().getResources().getString(R.string.Cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog ad = builder.show();
                if(input.requestFocus())
                    ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                manejador.cerrar();

                return true;
            }
        });

        articulos.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        for (final int position : reverseSortedPositions) {
                            DialogInterface.OnClickListener borrarArticuloListener = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    BDHandler manejador = new BDHandler(getContext());

                                    if (!manejador.eliminarArticuloEnLista(new ListaArticulo(datos.get(position).getId(), lista.getNombre(),0)))
                                        Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.No_se_ha_podido_eliminar_articulo));
                                    else {
                                        Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.Articulo_eliminado));
                                        remove(datos.get(position));
                                        actualizarPrecioTotal();
                                        notifyDataSetChanged();
                                    }

                                    manejador.cerrar();
                                }
                            };
                            BDHandler manejador = new BDHandler(getContext());
                            Articulo art = manejador.obtenerArticulo(datos.get(position).getArticulo());
                            Util.crearMensajeAlerta(getContext().getResources().getString(R.string.Eliminar) + art.getNombre() + "?", borrarArticuloListener, getContext());
                            manejador.close();
                        }
                    }
                }
        );

        manejador.close();
        return item;
    }

}
