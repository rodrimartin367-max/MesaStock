import { api } from "./api.js";
import { boton, confirmar, crearElemento, estado, limpiarErrores, mostrarError, notificar, prepararDialogos, vaciar } from "./ui.js";

const formulario = document.getElementById("categoria-form");
const dialogo = document.getElementById("categoria-dialog");
const cuerpo = document.getElementById("categorias-body");
const aviso = document.getElementById("categorias-status");
const vacio = document.getElementById("categorias-vacias");
let categorias = [];

function renderizar() {
    vaciar(cuerpo);
    vacio.hidden = categorias.length > 0;
    for (const categoria of categorias) {
        const fila = crearElemento("tr");
        const acciones = crearElemento("td", { clase: "row-actions" });
        const editar = boton("Editar", "button button-secondary", () => abrirFormulario(categoria));
        editar.setAttribute("aria-label", `Editar ${categoria.nombre}`);
        const borrar = boton("Borrar", "button button-danger", () => borrarCategoria(categoria));
        borrar.setAttribute("aria-label", `Borrar ${categoria.nombre}`);
        acciones.append(editar, borrar);
        fila.append(
            crearElemento("th", { texto: categoria.nombre, atributos: { scope: "row" } }),
            crearElemento("td", { texto: categoria.descripcion || "—" }),
            acciones
        );
        cuerpo.append(fila);
    }
}

async function cargarCategorias() {
    estado(aviso, "Cargando categorías…");
    try {
        categorias = await api.get("/categorias");
        renderizar();
        estado(aviso);
    } catch (error) {
        estado(aviso, error.message, { error: true });
    }
}

function abrirFormulario(categoria = null) {
    formulario.reset();
    limpiarErrores(formulario);
    formulario.elements.id.value = categoria?.id ?? "";
    formulario.elements.nombre.value = categoria?.nombre ?? "";
    formulario.elements.descripcion.value = categoria?.descripcion ?? "";
    document.getElementById("categoria-dialog-title").textContent = categoria ? "Editar categoría" : "Añadir categoría";
    dialogo.showModal();
    formulario.elements.nombre.focus();
}

async function borrarCategoria(categoria) {
    if (!await confirmar(`¿Quieres borrar la categoría «${categoria.nombre}»?`)) {
        return;
    }
    try {
        await api.delete(`/categorias/${categoria.id}`);
        notificar("Categoría eliminada.");
        await cargarCategorias();
    } catch (error) {
        mostrarError(error);
    }
}

document.getElementById("nueva-categoria").addEventListener("click", () => abrirFormulario());
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarErrores(formulario);
    if (!formulario.reportValidity()) {
        return;
    }
    const id = formulario.elements.id.value;
    const datos = {
        nombre: formulario.elements.nombre.value.trim(),
        descripcion: formulario.elements.descripcion.value.trim() || null
    };
    try {
        if (id) {
            await api.put(`/categorias/${id}`, datos);
            notificar("Categoría actualizada.");
        } else {
            await api.post("/categorias", datos);
            notificar("Categoría creada.");
        }
        dialogo.close();
        await cargarCategorias();
    } catch (error) {
        mostrarError(error, formulario);
    }
});
dialogo.addEventListener("close", () => {
    formulario.reset();
    limpiarErrores(formulario);
});
prepararDialogos();
cargarCategorias();