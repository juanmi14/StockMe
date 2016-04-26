package stockme.stockme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import java.util.List;

import stockme.stockme.adaptadores.AdaptadorGridItemCatalogo;
import stockme.stockme.adaptadores.AdaptadorListItemListas;
import stockme.stockme.logica.Articulo;
import stockme.stockme.logica.Lista;
import stockme.stockme.persistencia.BDHandler;
import stockme.stockme.util.Preferencias;
import stockme.stockme.util.Util;



public class Fragment_catalogo_todos extends Fragment {
    private OnFragmentInteractionListener mListener;
    private GridView articulos;

    public Fragment_catalogo_todos() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_catalogo_todos, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preferencias.addPreferencia("anterior", "Listas");

        articulos = (GridView)view.findViewById(R.id.gridView_catalogo_articulos);

        final BDHandler manejador = new BDHandler(view.getContext());
        List<Articulo> listaArticulos = manejador.obtenerArticulos();
        final AdaptadorGridItemCatalogo adaptador = new AdaptadorGridItemCatalogo(view.getContext(), listaArticulos);
        articulos.setAdapter(adaptador);

        manejador.cerrar();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1) && (resultCode == Activity.RESULT_OK)){
            //con esto refrescamos el fragment para actualizar la lista... mejora -> usar el onResume()
            Fragment fragmento = new Fragment_listas();
            FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.contenido_principal, fragmento);
            ft.attach(fragmento).commit();
        }
    }
}
