package DAO;

import Entidades.Venta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO 
{
    private List<Venta> historialVentas = new ArrayList<>();

    // HU4 - Actualizar stock tras venta: almacena el RegistroVenta con la fecha y
    // hora exacta del sistema para su posterior análisis.
    public void registrar(Venta v) { historialVentas.add(v); }

    public List<Venta> buscarVentasPorProductoYMaquina(String idM, String idP) 
    {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) 
        {
            if (v.getIdMaquina().equals(idM) && v.getIdProducto().equals(idP)) 
                filtradas.add(v);
        }
        return filtradas;
    }

    // HU6 - Calcular fecha límite de reposición: recupera los movimientos desde la última
    // reposición para que el cálculo sea preciso y actualizado.
    public List<Venta> buscarDesdeFecha(String idM, String idP, LocalDateTime fecha) 
    {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) 
        {
            if (v.getIdMaquina().equals(idM) && 
                v.getIdProducto().equals(idP) && 
                v.getFechaVenta().isAfter(fecha)) 
            {
                filtradas.add(v);
            }
        }
        return filtradas;
    }
}