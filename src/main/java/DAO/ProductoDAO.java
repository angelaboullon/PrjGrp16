package DAO;

import Entidades.Producto;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private List<Producto> catalogoProductos = new ArrayList<>();

    public void insertar(Producto p) {
        catalogoProductos.add(p);
    }

    public Producto buscarPorId(String id) {
        for (Producto p : catalogoProductos) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    public Producto buscarPorNombre(String nombre) {
        for (Producto p : catalogoProductos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) return p;
        }
        return null;
    }

    public List<Producto> listarCatalogo() {
        return new ArrayList<>(catalogoProductos);
    }
}