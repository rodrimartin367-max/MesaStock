import { api } from "./api.js";
import { crearElemento, estado, formatoMoneda, formatoNumero } from "./ui.js";

const estadoPanel = document.getElementById("dashboard-status");
const tarjetas = document.getElementById("dashboard-stats");
const tablaAlertas = document.getElementById("alertas-body");
const alertasVacias = document.getElementById("alertas-vacias");

async function cargarPanel() {
    estado(estadoPanel, "Cargando resumen del inventario…");
    tarjetas.setAttribute("aria-busy", "true");
    try {
        const resumen = await api.get("/dashboard");
        document.getElementById("total-productos").textContent = formatoNumero(resumen.totalProductos);
        document.getElementById("total-categorias").textContent = formatoNumero(resumen.totalCategorias);
        document.getElementById("total-proveedores").textContent = formatoNumero(resumen.totalProveedores);
        document.getElementById("valor-inventario").textContent = formatoMoneda(resumen.valorInventario);
        tablaAlertas.replaceChildren();
        const productos = resumen.productosBajoStock || [];
        alertasVacias.hidden = productos.length > 0;
        for (const producto of productos) {
            const fila = crearElemento("tr", { clase: "is-low-stock" });
            fila.append(
                crearElemento("td", { texto: producto.nombre }),
                crearElemento("td", { texto: producto.categoriaNombre || "Sin categoría" }),
                crearElemento("td", { texto: `${formatoNumero(producto.stockActual)} ${producto.unidadMedida}` }),
                crearElemento("td", { texto: `${formatoNumero(producto.stockMinimo)} ${producto.unidadMedida}` }),
                crearElemento("td", { hijos: [crearElemento("span", { texto: "Bajo mínimo", clase: "status-pill status-pill-warning" })] })
            );
            tablaAlertas.append(fila);
        }
        estado(estadoPanel);
    } catch (error) {
        estado(estadoPanel, error.message, { error: true });
    } finally {
        tarjetas.setAttribute("aria-busy", "false");
    }
}

cargarPanel();