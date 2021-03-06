package stockme.stockme.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.Date;
import java.util.List;

import stockme.stockme.R;
import stockme.stockme.logica.ArticuloSupermercado;
import stockme.stockme.logica.Lista;
import stockme.stockme.logica.ListaArticulo;
import stockme.stockme.persistencia.BDHandler;
import stockme.stockme.util.Util;



public class AdaptadorListItemListas extends ArrayAdapter<Lista> {
    private DynamicListView listas;
    private List<Lista> datos;
    private ImageButton btn_delete;
    private TextView lblNombre;
    private TextView lblSupermercado;
    private TextView lblFecha;
    private TextView lblNProductos;
    private Lista lista;
    private String nuevoNombre;
    public AdaptadorListItemListas(Context context, List<Lista> datos) {
        super(context, R.layout.listitem_lista, datos);
//        this.context = context;
        this.datos = datos;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //comprobamos si existe una view que podamos reutilizar con el convertView para no tener que inflar de más

        View item = convertView;
        ViewHolder holder;
        if(item == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            item = inflater.inflate(R.layout.listitem_lista, null);

            holder = new ViewHolder();
            holder.lblNombre = (TextView)item.findViewById(R.id.listitem_lista_nombre);
            holder.lblSupermercado = (TextView)item.findViewById(R.id.listitem_lista_supermercado);
            holder.lblFecha = (TextView)item.findViewById(R.id.listitem_lista_fecha);
            holder.lblNProductos = (TextView)item.findViewById(R.id.listitem_lista_nproductos);
            holder.btn_delete = (ImageButton)item.findViewById(R.id.listitem_lista_btn_delete);

            item.setTag(holder);
        }else{
            holder = (ViewHolder)item.getTag();
        }

        //pintar los elementos de forma intercalada
        if ( position % 2 == 1) {
            item.setBackgroundResource(R.drawable.esquinas_impar);
        }
        else {
            item.setBackgroundResource(R.drawable.esquinas);
        }

        //se crea un elemento Lista que contiene los datos de la fila
        lista = new Lista();
        lista.setNombre(datos.get(position).getNombre());
        lista.setFechaCreacion(datos.get(position).getFechaCreacion());
        lista.setFechaModificacion(datos.get(position).getFechaModificacion());
        lista.setSupermercado(datos.get(position).getSupermercado());

        BDHandler manejador = new BDHandler(getContext());

        holder.lblNombre.setText(lista.getNombre());

        if(!lista.getSupermercado().equalsIgnoreCase("cualquiera"))
            holder.lblSupermercado.setText(lista.getSupermercado());
        else
            holder.lblSupermercado.setText("");

        holder.lblFecha.setText(lista.getFechaModificacion());

        holder.lblNProductos.setText(String.valueOf(manejador.numArticulosEnLista(lista.getNombre())));

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener borrarListaListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BDHandler manejador = new BDHandler(getContext());

                        if (!manejador.eliminarLista(datos.get(position)))
                            Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.No_se_ha_podido_eliminar_listas));
                        else {
                            Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.Lista_eliminada));
                            remove(datos.get(position));
                        }

                        manejador.cerrar();
                    }
                };
                Util.crearMensajeAlerta(getContext().getResources().getString(R.string.Quieres_eliminar_lista), borrarListaListener, v.getContext());
            }
        });

        listas = (DynamicListView) parent.findViewById(R.id.fragment_listas_listview);
        listas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final BDHandler manejador = new BDHandler(getContext());
                final Lista lista = ((Lista) parent.getItemAtPosition(position));

                //Diálogo para cambiar nombre
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(getContext().getResources().getString(R.string.Nuevo_nombre));

                final EditText input = new EditText(view.getContext());
                builder.setView(input);

                builder.setPositiveButton(getContext().getResources().getString(R.string.Aceptar), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nuevoNombre = input.getText().toString();
                        Lista nuevaLista = new Lista(nuevoNombre, lista.getFechaCreacion(), Util.diaMesAnyo.format(new Date()), lista.getSupermercado());

                        manejador.insertarLista(nuevaLista);

                        List<ArticuloSupermercado> articulos = manejador.obtenerArticulosEnLista(lista);
                        for (ArticuloSupermercado articulo : articulos) {
                            int cantidad = manejador.obtenerCantidadArticuloEnLista(articulo.getId(), lista);
                            manejador.insertarArticuloEnLista(new ListaArticulo(articulo.getId(), nuevoNombre, cantidad));
                            manejador.eliminarArticuloEnLista(new ListaArticulo(articulo.getId(), lista.getNombre(), cantidad));
                        }

                        manejador.eliminarLista(lista);
                        Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.Lista_renombrada));
                        remove(lista);
                        add(nuevaLista);

                    }
                });
                builder.setNegativeButton(getContext().getResources().getString(R.string.Cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                return true;
            }
        });

        listas.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        for (final int position : reverseSortedPositions) {
                            DialogInterface.OnClickListener borrarListaListener = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    BDHandler manejador = new BDHandler(getContext());

                                    if (!manejador.eliminarLista(datos.get(position)))
                                        Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.No_se_ha_podido_eliminar_listas));
                                    else {
                                        Util.mostrarToast(getContext(), getContext().getResources().getString(R.string.Lista_eliminada));
                                        remove(datos.get(position));
                                    }

                                    manejador.cerrar();
                                }
                            };
                            Util.crearMensajeAlerta(getContext().getResources().getString(R.string.Quieres_eliminar_lista), borrarListaListener, getContext());
                        }
                    }
                }
        );

        manejador.close();

        return(item);
    }

    //patrón ViewHolder para optimización de listas
    private static class ViewHolder {
        ImageButton btn_delete;
        TextView lblNombre;
        TextView lblSupermercado;
        TextView lblFecha;
        TextView lblNProductos;
    }
}
