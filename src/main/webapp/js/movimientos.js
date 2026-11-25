import { api } from "./api.js";
import { crearElemento, estado, formatoFecha, formatoNumero, limpiarErrores, mostrarError, notificar, vaciar } from "./ui.js";

const formulario = document.getElementById("movimiento-form");
const selectorProducto = document.getElementById("movimiento-producto");
const filtroProducto = document.getElementById("filtro-producto");
const formularioFiltro = document.getElementById("filtro-movimientos");
const aviso = document.getElementById("movimientos-status");
const vacio = document.getElementById("movimientos-vacios");
const cuerpo = document.getElementById("movimientos-body");

function ponerProductos(select, productos, textoInicial) {
    const seleccion = select.value;
    select.replaceChildren(crearElemento("option", { texto: textoInicial, atributos: { value: "" } }));
    for (const producto of productos) {
        select.append(crearElemento("option", {
            texto: `${producto.nombre} (${formatoNumero(producto.stockActual)} ${producto.unidadMedida})`,
            atributos: { value: producto.id }
        }));
    }
    if (productos.some((producto) => String(producto.id) === seleccion)) {
        select.value = seleccion;
    }
}

async function cargarProductos() {
    const productos = await api.get("/productos");
    ponerProductos(selectorProducto, productos, productos.length ? "Selecciona un producto" : "No hay productos disponibles");
    ponerProductos(filtroProducto, productos, "Todos los productos");
    selectorProducto.disabled = productos.length === 0;
}

async function cargarMovimientos() {
    estado(aviso, "Cargando histórico…");
    const parametros = new URLSearchParams();
    if (filtroProducto.value) {
        parametros.set("productoId", filtroProducto.value);
    }
    try {
        const ruta = parametros.size ? `/movimientos?${parametros}` : "/movimientos";
        const movimientos = await api.get(ruta);
        vaciar(cuerpo);
        vacio.hidden = movimientos.length > 0;
        if (!movimientos.length) {
            vacio.textContent = "No hay movimientos registrados para este filtro.";
        }
        for (const movimiento of movimientos) {
            const tipo = { ENTRADA: "Entrada", SALIDA: "Salida", MERMA: "Merma" }[movimiento.tipo] || movimiento.tipo;
            const fila = crearElemento("tr");
            fila.append(
                crearElemento("td", { texto: formatoFecha(movimiento.fecha) }),
                crearElemento("td", { texto: movimiento.productoNombre }),
                crearElemento("td", { texto: tipo }),
                crearElemento("td", { texto: formatoNumero(movimiento.cantidad) }),
                crearElemento("td", { texto: movimiento.nota || "—" })
            );
            cuerpo.append(fila);
        }
        estado(aviso);
    } catch (error) {
        estado(aviso, error.message, { error: true });
    }
}

formularioFiltro.addEventListener("submit", (evento) => {
    evento.preventDefault();
    cargarMovimientos();
});
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    if (!formulario.reportValidity()) {
        return;
    }
    const botonGuardar = document.getElementById("guardar-movimiento");
    const datos = {
        productoId: Number(formulario.elements.productoId.value),
        tipo: formulario.elements.tipo.value,
        cantidad: Number(formulario.elements.cantidad.value),
        nota: formulario.elements.nota.value.trim() || null
    };
    botonGuardar.disabled = true;
    try {
        await api.post("/movimientos", datos);
        notificar("Movimiento registrado y stock actualizado.");
        formulario.reset();
        limpiarErrores(formulario);
        await Promise.all([cargarProductos(), cargarMovimientos()]);
    } catch (error) {
        mostrarError(error, formulario);
    } finally {
        botonGuardar.disabled = false;
    }
});

async function iniciar() {
    estado(aviso, "Cargando productos e histórico…");
    try {
        await cargarProductos();
        await cargarMovimientos();
    } catch (error) {
        estado(aviso, error.message, { error: true });
    }
}

iniciar();