import { api } from "./api.js";
import { boton, confirmar, crearElemento, estado, formatoMoneda, formatoNumero, limpiarErrores, mostrarError, notificar, prepararDialogos, vaciar } from "./ui.js";

const formulario = document.getElementById("producto-form");
const dialogo = document.getElementById("producto-dialog");
const cuerpo = document.getElementById("productos-body");
const aviso = document.getElementById("productos-status");
const vacio = document.getElementById("productos-vacios");
const regionProductos = document.getElementById("productos-region");
const filtroForm = document.getElementById("filtros-productos");
const categoriaFiltro = document.getElementById("filtro-categoria");
const categoriaFormulario = document.getElementById("producto-categoria");
const dialogoProveedores = document.getElementById("proveedores-dialog");
const formularioAsociacion = document.getElementById("asociacion-form");
const cuerpoAsociaciones = document.getElementById("asociaciones-body");
const estadoAsociaciones = document.getElementById("asociaciones-status");
const asociacionesVacias = document.getElementById("asociaciones-vacias");
const selectorProveedor = document.getElementById("asociacion-proveedor");
let productos = [];
let categorias = [];
let productoActivo = null;

function reemplazarOpciones(select, opciones, textoInicial) {
    const seleccion = select.value;
    select.replaceChildren(crearElemento("option", { texto: textoInicial, atributos: { value: "" } }));
    for (const opcion of opciones) {
        select.append(crearElemento("option", { texto: opcion.nombre, atributos: { value: opcion.id } }));
    }
    if (opciones.some((opcion) => String(opcion.id) === seleccion)) {
        select.value = seleccion;
    }
}

function renderizarProductos() {
    vaciar(cuerpo);
    vacio.hidden = productos.length > 0;
    if (!productos.length) {
        vacio.textContent = "No hay productos que coincidan con los filtros.";
    }
    for (const producto of productos) {
        const fila = crearElemento("tr", { clase: producto.bajoStock ? "is-low-stock" : "" });
        const acciones = crearElemento("td", { clase: "row-actions" });
        const editar = boton("Editar", "button button-secondary", () => abrirFormulario(producto));
        editar.setAttribute("aria-label", `Editar ${producto.nombre}`);
        const proveedores = boton("Proveedores", "button button-secondary", () => abrirProveedores(producto));
        proveedores.setAttribute("aria-label", `Gestionar proveedores de ${producto.nombre}`);
        const borrar = boton("Borrar", "button button-danger", () => borrarProducto(producto));
        borrar.setAttribute("aria-label", `Borrar ${producto.nombre}`);
        acciones.append(editar, proveedores, borrar);
        fila.append(
            crearElemento("th", { texto: producto.nombre, atributos: { scope: "row" } }),
            crearElemento("td", { texto: producto.categoriaNombre || "—" }),
            crearElemento("td", { texto: `${formatoNumero(producto.stockActual)} ${producto.unidadMedida}` }),
            crearElemento("td", { texto: `${formatoNumero(producto.stockMinimo)} ${producto.unidadMedida}` }),
            crearElemento("td", { texto: formatoMoneda(producto.precioUnitario) }),
            crearElemento("td", { hijos: [crearElemento("span", {
                texto: producto.bajoStock ? "Bajo mínimo" : "Correcto",
                clase: producto.bajoStock ? "status-pill status-pill-warning" : "status-pill"
            })] }),
            acciones
        );
        cuerpo.append(fila);
    }
}

async function cargarProductos() {
    cargarEstado("Cargando productos…", true);
    const parametros = new URLSearchParams();
    if (categoriaFiltro.value) {
        parametros.set("categoriaId", categoriaFiltro.value);
    }
    if (document.getElementById("filtro-stock").checked) {
        parametros.set("bajoStock", "true");
    }
    const query = parametros.size ? `?${parametros}` : "";
    try {
        productos = await api.get(`/productos${query}`);
        renderizarProductos();
        estado(aviso);
    } catch (error) {
        estado(aviso, error.message, { error: true });
    } finally {
        regionProductos.setAttribute("aria-busy", "false");
    }
}

function cargarEstado(mensaje, ocupado) {
    estado(aviso, mensaje);
    regionProductos.setAttribute("aria-busy", String(ocupado));
}

async function cargarCatalogos() {
    cargarEstado("Cargando categorías y productos…", true);
    try {
        categorias = await api.get("/categorias");
        reemplazarOpciones(categoriaFiltro, categorias, "Todas");
        reemplazarOpciones(categoriaFormulario, categorias, "Selecciona una categoría");
        const parametros = new URLSearchParams(window.location.search);
        const bajoStock = parametros.get("bajoStock");
        if (bajoStock === "true") {
            document.getElementById("filtro-stock").checked = true;
        }
        await cargarProductos();
    } catch (error) {
        estado(aviso, error.message, { error: true });
        regionProductos.setAttribute("aria-busy", "false");
    }
}

function abrirFormulario(producto = null) {
    formulario.reset();
    limpiarErrores(formulario);
    formulario.elements.id.value = producto?.id ?? "";
    formulario.elements.nombre.value = producto?.nombre ?? "";
    formulario.elements.categoriaId.value = producto?.categoriaId ?? "";
    formulario.elements.unidadMedida.value = producto?.unidadMedida ?? "KG";
    formulario.elements.stockMinimo.value = producto?.stockMinimo ?? "";
    formulario.elements.precioUnitario.value = producto?.precioUnitario ?? "";
    document.getElementById("producto-dialog-title").textContent = producto ? "Editar producto" : "Añadir producto";
    dialogo.showModal();
    formulario.elements.nombre.focus();
}

async function borrarProducto(producto) {
    if (!await confirmar(`¿Quieres borrar «${producto.nombre}»? También se eliminarán sus movimientos y asociaciones de proveedores.`)) {
        return;
    }
    try {
        await api.delete(`/productos/${producto.id}`);
        notificar("Producto eliminado.");
        await cargarProductos();
    } catch (error) {
        mostrarError(error);
    }
}

async function abrirProveedores(producto) {
    productoActivo = producto;
    document.getElementById("proveedores-producto-nombre").textContent = producto.nombre;
    formularioAsociacion.reset();
    limpiarErrores(formularioAsociacion);
    dialogoProveedores.showModal();
    await cargarAsociaciones();
}

async function cargarAsociaciones() {
    estado(estadoAsociaciones, "Cargando proveedores asociados…");
    vaciar(cuerpoAsociaciones);
    asociacionesVacias.hidden = true;
    selectorProveedor.disabled = true;
    formularioAsociacion.querySelector("button[type='submit']").disabled = true;
    try {
        const [asociaciones, todosProveedores] = await Promise.all([
            api.get(`/productos/${productoActivo.id}/proveedores`),
            api.get("/proveedores")
        ]);
        vaciar(cuerpoAsociaciones);
        asociacionesVacias.hidden = asociaciones.length > 0;
        for (const asociacion of asociaciones) {
            const fila = crearElemento("tr");
            const quitar = boton("Quitar", "button button-danger", async () => {
                if (!await confirmar(`¿Quitar a «${asociacion.nombreProveedor}» de este producto?`)) {
                    return;
                }
                try {
                    await api.delete(`/productos/${productoActivo.id}/proveedores/${asociacion.proveedorId}`);
                    notificar("Proveedor quitado del producto.");
                    await cargarAsociaciones();
                } catch (error) {
                    mostrarError(error);
                }
            });
            fila.append(
                crearElemento("td", { texto: asociacion.nombreProveedor }),
                crearElemento("td", { texto: formatoMoneda(asociacion.precioCompra) }),
                crearElemento("td", { hijos: [quitar] })
            );
            cuerpoAsociaciones.append(fila);
        }
        const idsAsociados = new Set(asociaciones.map((asociacion) => String(asociacion.proveedorId)));
        const disponibles = todosProveedores.filter((proveedor) => !idsAsociados.has(String(proveedor.id)));
        reemplazarOpciones(selectorProveedor, disponibles, disponibles.length ? "Selecciona un proveedor" : "No hay proveedores disponibles");
        selectorProveedor.disabled = disponibles.length === 0;
        formularioAsociacion.querySelector("button[type='submit']").disabled = disponibles.length === 0;
        estado(estadoAsociaciones, disponibles.length ? "" : "Todos los proveedores ya están asociados.");
    } catch (error) {
        estado(estadoAsociaciones, error.message, { error: true });
    }
}

document.getElementById("nuevo-producto").addEventListener("click", () => abrirFormulario());
filtroForm.addEventListener("submit", (evento) => {
    evento.preventDefault();
    cargarProductos();
});
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarErrores(formulario);
    if (!formulario.reportValidity()) {
        return;
    }
    const id = formulario.elements.id.value;
    const datos = {
        nombre: formulario.elements.nombre.value.trim(),
        categoriaId: Number(formulario.elements.categoriaId.value),
        unidadMedida: formulario.elements.unidadMedida.value,
        stockMinimo: Number(formulario.elements.stockMinimo.value),
        precioUnitario: Number(formulario.elements.precioUnitario.value)
    };
    try {
        if (id) {
            await api.put(`/productos/${id}`, datos);
            notificar("Producto actualizado.");
        } else {
            await api.post("/productos", datos);
            notificar("Producto creado.");
        }
        dialogo.close();
        await cargarProductos();
    } catch (error) {
        mostrarError(error, formulario);
    }
});
formulario.addEventListener("reset", () => limpiarErrores(formulario));
dialogo.addEventListener("close", () => {
    formulario.reset();
    limpiarErrores(formulario);
});
formularioAsociacion.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarErrores(formularioAsociacion);
    if (!formularioAsociacion.reportValidity()) {
        return;
    }
    const datos = {
        proveedorId: Number(formularioAsociacion.elements.proveedorId.value),
        precioCompra: Number(formularioAsociacion.elements.precioCompra.value)
    };
    try {
        await api.post(`/productos/${productoActivo.id}/proveedores`, datos);
        formularioAsociacion.reset();
        limpiarErrores(formularioAsociacion);
        notificar("Proveedor asociado al producto.");
        await cargarAsociaciones();
    } catch (error) {
        mostrarError(error, formularioAsociacion);
    }
});
dialogoProveedores.addEventListener("close", () => {
    formularioAsociacion.reset();
    limpiarErrores(formularioAsociacion);
});
prepararDialogos();
cargarCatalogos();