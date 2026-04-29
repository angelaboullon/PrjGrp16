package DAO;

import Entidades.Venta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {
    private List<Venta> historialVentas = new ArrayList<>();

    public void registrar(Venta v) {
        historialVentas.add(v);
    }

    public List<Venta> buscarVentasPorProductoYMaquina(String idM, String idP) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) {
            if (v.getIdMaquina().equals(idM) && v.getIdProducto().equals(idP)) {
                filtradas.add(v);
            }
        }
        return filtradas;
    }

    public List<Venta> buscarDesdeFecha(String idM, String idP, LocalDateTime fecha) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) {
            if (v.getIdMaquina().equals(idM) && 
                v.getIdProducto().equals(idP) && 
                v.getFechaVenta().isAfter(fecha)) {
                filtradas.add(v);
            }
        }
        return filtradas;
    }
}