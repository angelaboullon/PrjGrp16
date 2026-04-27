package DAO;

import Entidades.Localizacion;
import java.util.ArrayList;
import java.util.List;

public class LocalizacionDAO {
    private List<Localizacion> listaUbics = new ArrayList<>();

    public void insertar(Localizacion l) {
        listaUbics.add(l);
    }

    public Localizacion buscarPorCoordenadas(double lat, double lon) {
        for (Localizacion l : listaUbics) {
            if (Double.compare(l.getLatitud(), lat) == 0 && 
                Double.compare(l.getLongitud(), lon) == 0) {
                return l;
            }
        }
        return null;
    }
}