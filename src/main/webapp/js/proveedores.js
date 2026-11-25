import { api } from "./api.js";
import { boton, confirmar, crearElemento, estado, limpiarErrores, mostrarError, notificar, prepararDialogos, vaciar } from "./ui.js";

const formulario = document.getElementById("proveedor-form");
const dialogo = document.getElementById("proveedor-dialog");
const cuerpo = document.getElementById("proveedores-body");
const aviso = document.getElementById("proveedores-status");
const vacio = document.getElementById("proveedores-vacias");
let proveedores = [];

function renderizar() {
    vaciar(cuerpo);
    vacio.hidden = proveedores.length > 0;
    for (const proveedor of proveedores) {
        const fila = crearElemento("tr");
        const acciones = crearElemento("td", { clase: "row-actions" });
        const editar = boton("Editar", "button button-secondary", () => abrirFormulario(proveedor));
        editar.setAttribute("aria-label", `Editar ${proveedor.nombre}`);
        const borrar = boton("Borrar", "button button-danger", () => borrarProveedor(proveedor));
        borrar.setAttribute("aria-label", `Borrar ${proveedor.nombre}`);
        acciones.append(editar, borrar);
        fila.append(
            crearElemento("th", { texto: proveedor.nombre, atributos: { scope: "row" } }),
            crearElemento("td", { texto: proveedor.cif }),
            crearElemento("td", { texto: proveedor.telefono || "—" }),
            crearElemento("td", { texto: proveedor.email || "—" }),
            acciones
        );
        cuerpo.append(fila);
    }
}

async function cargarProveedores() {
    estado(aviso, "Cargando proveedores…");
    try {
        proveedores = await api.get("/proveedores");
        renderizar();
        estado(aviso);
    } catch (error) {
        estado(aviso, error.message, { error: true });
    }
}

function abrirFormulario(proveedor = null) {
    formulario.reset();
    limpiarErrores(formulario);
    formulario.elements.id.value = proveedor?.id ?? "";
    formulario.elements.nombre.value = proveedor?.nombre ?? "";
    formulario.elements.cif.value = proveedor?.cif ?? "";
    formulario.elements.telefono.value = proveedor?.telefono ?? "";
    formulario.elements.email.value = proveedor?.email ?? "";
    document.getElementById("proveedor-dialog-title").textContent = proveedor ? "Editar proveedor" : "Añadir proveedor";
    dialogo.showModal();
    formulario.elements.nombre.focus();
}

async function borrarProveedor(proveedor) {
    if (!await confirmar(`¿Quieres borrar el proveedor «${proveedor.nombre}»?`)) {
        return;
    }
    try {
        await api.delete(`/proveedores/${proveedor.id}`);
        notificar("Proveedor eliminado.");
        await cargarProveedores();
    } catch (error) {
        mostrarError(error);
    }
}

document.getElementById("nuevo-proveedor").addEventListener("click", () => abrirFormulario());
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarErrores(formulario);
    if (!formulario.reportValidity()) {
        return;
    }
    const id = formulario.elements.id.value;
    const datos = {
        nombre: formulario.elements.nombre.value.trim(),
        cif: formulario.elements.cif.value.trim().toUpperCase(),
        telefono: formulario.elements.telefono.value.trim() || null,
        email: formulario.elements.email.value.trim() || null
    };
    try {
        if (id) {
            await api.put(`/proveedores/${id}`, datos);
            notificar("Proveedor actualizado.");
        } else {
            await api.post("/proveedores", datos);
            notificar("Proveedor creado.");
        }
        dialogo.close();
        await cargarProveedores();
    } catch (error) {
        mostrarError(error, formulario);
    }
});
dialogo.addEventListener("close", () => {
    formulario.reset();
    limpiarErrores(formulario);
});
prepararDialogos();
cargarProveedores();